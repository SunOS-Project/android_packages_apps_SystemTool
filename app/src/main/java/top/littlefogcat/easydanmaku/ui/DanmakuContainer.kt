package top.littlefogcat.easydanmaku.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import top.littlefogcat.easydanmaku.danmakus.DanmakuItem
import top.littlefogcat.easydanmaku.danmakus.DanmakuPools
import top.littlefogcat.easydanmaku.danmakus.views.Danmaku
import top.littlefogcat.esus.view.View
import top.littlefogcat.esus.view.ViewGroup
import top.littlefogcat.esus.view.ViewParent

/**
 * @author littlefogcat
 * @email littlefogcat@foxmail.com
 */
class DanmakuContainer : ViewGroup() {
    private val resolver: DanmakuResolver = DanmakuResolverImpl()
    private val locator: DanmakuLocator<Danmaku> = DanmakuLocatorImpl()

    private var onDanmakuClickListener: ((Danmaku) -> Boolean)? = null

    private var discarded = 0

    fun setDanmakus(danmakus: Collection<DanmakuItem>) {
        resolver.setData(danmakus)
    }

    fun addDanmaku(danmaku: Danmaku) {
        addView(danmaku)
    }

    fun setOnDanmakuClickListener(l: (Danmaku) -> Boolean) {
        onDanmakuClickListener = l
    }

    /* ===================== Override functions ===================== */

    override fun onViewAdded(view: View) {
        if (onDanmakuClickListener != null) {
            view.setOnClickListener(object : OnClickListener {
                override fun onClick(view: View) {
                    onDanmakuClickListener?.invoke(view as Danmaku)
                }
            })
        }
    }

    override fun onViewRemoved(view: View) {
        view.removeOnClickListener()
        if (view is Danmaku) {
            locator.release(view)
            DanmakuPools.ofType(view.type).release(view)
        }
    }

    override fun onLayout(l: Int, t: Int, r: Int, b: Int) {
        if (needLayout || attachInfo?.forceLayout == true) {
            allChildren { child ->
                if (child is Danmaku) {
                    if (child.needLayout) {
                        val success = locator.locate(this, child)
                        if (!success) {
                            child.needLayout = false
                            removeView(child)
                            discarded++
                        } else {
                            child.setVisibility(VISIBLE)
                        }
                    }
                } else {
                    child.layout(0, 0, child.measuredWidth, child.measuredHeight)
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas, parent: ViewParent?, time: Long) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }

    override fun afterDraw(canvas: Canvas, parent: ViewParent?, time: Long) {
        val newDanmakus = resolver.retrieve(time)
        // TODO: 把Locate的步骤放在这里做，放不下的不要添加
        newDanmakus.forEach {
            // 从池中取一个view
            val pool = DanmakuPools.ofType(it.type)
            val view = pool.acquire()
            view.setVisibility(GONE)
            view.item = it
            addView(view)
        }
    }
}
