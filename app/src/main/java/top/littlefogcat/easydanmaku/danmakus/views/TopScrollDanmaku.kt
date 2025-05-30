package top.littlefogcat.easydanmaku.danmakus.views

import top.littlefogcat.easydanmaku.Danmakus
import top.littlefogcat.easydanmaku.danmakus.DanmakuItem
import top.littlefogcat.esus.view.ViewGroup

/**
 * Top scrolling danmaku
 *
 * @author littlefogcat
 * @email littlefogcat@foxmail.com
 */
abstract class TopScrollDanmaku(item: DanmakuItem? = null) : Danmaku(item) {

    override val duration: Int
        get() = Danmakus.Options.scrollingDanmakuDuration

    /**
     * The moment the whole danmaku is displayed.
     */
    val entirelyDisplayTime: Int
        get() = (time + duration.toFloat() * width / (parent as ViewGroup).width).toInt()

    val reachingEdgeTime: Int
        get() = parent?.let {
            it as ViewGroup
            (time + duration * (it.width.toFloat() / (it.width + measuredWidth))).toInt()
        } ?: -1
}
