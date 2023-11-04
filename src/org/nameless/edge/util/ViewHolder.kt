/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Rect
import android.view.WindowManager
import android.view.WindowManager.LayoutParams

import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible

import org.nameless.edge.view.DimmerView
import org.nameless.edge.view.IconView
import org.nameless.wm.PopUpDebugHelper.logD
import org.nameless.wm.PopUpDebugHelper.logE

object ViewHolder {

    private val TAG = "EdgeTool::ViewHolder"

    private val HIDE_ANIMATION_DURATION = 250L
    private val REBOUND_ANIMATION_DURATION = 300L
    private val SHOW_ANIMATION_DURATION = 120L

    private val ROTATE_REBOUND_ANGLE = 30f

    private var wm: WindowManager? = null

    var dimmerView: DimmerView? = null
    private val iconViewsLeft: MutableList<IconView> = mutableListOf()
    private val iconViewsRight: MutableList<IconView> = mutableListOf()
    val iconViewsShowing: MutableList<IconView> = mutableListOf()

    var currentlyVisible = false
    var allowVisible = true

    fun getWindowManager(context: Context): WindowManager? {
        if (wm == null) {
            wm = context.getSystemService(WindowManager::class.java)
        }
        return wm
    }

    private fun addView(
        context: Context,
        view: IconView?,
        params: LayoutParams,
        isLeft: Boolean
    ): Boolean {
        if (getWindowManager(context) == null) {
            logE(TAG, "Failed to addView: WindowManager is null")
            return false
        }
        if (view == null) {
            return false
        }
        try {
            wm?.addView(view, params)
            logD(TAG, "addView: packageName=${view.packageName}, isLeft=$isLeft")
            return true
        } catch (e: Exception) {
            logE(TAG, "Exception on addView for packageName=${view.packageName}, isLeft=$isLeft")
            return false
        }
    }

    private fun removeView(
        context: Context,
        view: IconView,
        isLeft: Boolean
    ) {
        if (getWindowManager(context) == null) {
            logE(TAG, "Failed to removeView: WindowManager is null")
            return
        }
        try {
            wm?.removeView(view)
            logD(TAG, "removeView: packageName=${view.packageName}, isLeft=$isLeft")
        } catch (e: Exception) {
            logE(TAG, "Exception on removeView for packageName=${view.packageName}, isLeft=$isLeft")
        }
    }

    fun removeDimmerView(context: Context) {
        if (dimmerView == null) {
            return
        }
        if (getWindowManager(context) == null) {
            logE(TAG, "Failed to removeDimmerView: WindowManager is null")
            return
        }
        try {
            wm?.removeView(dimmerView)
            logD(TAG, "removeDimmerView")
        } catch (e: Exception) {
            logE(TAG, "Exception on removeDimmerView")
        }
    }

    fun showForAll(isLeft: Boolean, wm: WindowManager, displayBounds: Rect) {
        if (!allowVisible) {
            logD(TAG, "showForAll: Set visible is disabled, return early")
            return
        }

        if (currentlyVisible) {
            logD(TAG, "showForAll: Already showing, return early")
            return
        }

        val views = if (isLeft) iconViewsLeft else iconViewsRight
        if (views.size == 0) {
            logD(TAG, "showForAll: No icons, return early")
            return
        }

        logD(TAG, "showForAll: isLeft=$isLeft")
        currentlyVisible = true

        views.forEach {
            iconViewsShowing.add(it)
            it.postOnAnimation {
                val params = it.layoutParams as LayoutParams
                val prevX = params.x
                val prevY = params.y
                params.x = if (isLeft) (-it.radius * 2) else (displayBounds.width() + it.radius * 2)
                params.y = displayBounds.height() + it.radius * 2
                params.alpha = 0f
                val transX = params.x - prevX
                val transY = params.y - prevY

                wm.updateViewLayout(it, params)
                it.isVisible = true

                val rotateClockwise = ObjectAnimator.ofFloat(
                        it, "rotation", -360f, ROTATE_REBOUND_ANGLE)
                    .setDuration(SHOW_ANIMATION_DURATION)
                    .apply {
                        addUpdateListener { a ->
                            params.x = (prevX + (1f - a.animatedFraction) * transX).toInt()
                            params.y = (prevY + (1f - a.animatedFraction) * transY).toInt()
                            params.alpha = a.animatedFraction
                            wm.updateViewLayout(it, params)
                        }
                    }
                val rotateRebound = ObjectAnimator.ofFloat(
                        it, "rotation", ROTATE_REBOUND_ANGLE, 0f)
                    .setDuration(REBOUND_ANIMATION_DURATION)
                    .apply {
                        startDelay = SHOW_ANIMATION_DURATION
                    }
                AnimatorSet().run {
                    playTogether(rotateClockwise, rotateRebound)
                    start()
                }
            }
        }
        dimmerView?.postOnAnimation {
            dimmerView?.isVisible = true
        }
    }

    fun hideForAll() {
        if (!currentlyVisible) {
            logD(TAG, "hideForAll: Already invisible, return early")
            return
        }

        if (iconViewsShowing.size == 0) {
            logD(TAG, "hideForAll: No icons, return early")
            return
        }

        logD(TAG, "hideForAll")
        currentlyVisible = false

        iconViewsShowing.forEach { icon ->
            icon.postOnAnimation {
                val hide = ObjectAnimator.ofFloat(
                        icon, "alpha", 1f, 0f)
                    .setDuration(HIDE_ANIMATION_DURATION)
                val rotateCounter = ObjectAnimator.ofFloat(
                        icon, "rotation", 0f, -ROTATE_REBOUND_ANGLE)
                    .setDuration(HIDE_ANIMATION_DURATION / 2)
                val rotateClockwise = ObjectAnimator.ofFloat(
                        icon, "rotation", -ROTATE_REBOUND_ANGLE, 0f)
                    .setDuration(HIDE_ANIMATION_DURATION / 2)
                    .apply {
                        startDelay = HIDE_ANIMATION_DURATION / 2
                    }
                AnimatorSet().run {
                    playTogether(hide, rotateCounter, rotateClockwise)
                    doOnEnd {
                        icon.isVisible = false
                        icon.alpha = 1f
                        icon.rotation = -360f
                        icon.resetState()
                    }
                    start()
                }
            }
        }
        dimmerView?.postOnAnimation {
            dimmerView?.isVisible = false
        }

        iconViewsShowing.clear()
    }

    fun safelyClearIconViews(context: Context) {
        hideForAll()

        iconViewsLeft.forEach { removeView(context, it, true) }
        iconViewsRight.forEach { removeView(context, it, false) }
        iconViewsLeft.clear()
        iconViewsRight.clear()
    }

    fun addIconView(context: Context, packageName: String, idx: Int, total: Int) {
        val iconRadius = IconLayoutAlgorithm.getIconRadius(context)

        var viewLeft: IconView?
        IconLayoutAlgorithm.getIconCenterPos(context, true, idx, total).let {
            val params = IconLayoutAlgorithm.getDefaultIconLayoutParams()
            params.width = iconRadius * 2
            params.height = iconRadius * 2
            params.x = it.first - iconRadius
            params.y = it.second - iconRadius
            params.alpha = 0f
            viewLeft = IconView(context.createWindowContext(
                    LayoutParams.TYPE_APPLICATION_OVERLAY, null),
                    packageName, it.first, it.second, iconRadius)
            viewLeft?.isVisible = false
            viewLeft?.rotation = -360f
            if (!addView(context, viewLeft, params, true)) {
                viewLeft = null
            }
        }

        var viewRight: IconView?
        IconLayoutAlgorithm.getIconCenterPos(context, false, idx, total).let {
            val params = IconLayoutAlgorithm.getDefaultIconLayoutParams()
            params.width = iconRadius * 2
            params.height = iconRadius * 2
            params.x = it.first - iconRadius
            params.y = it.second - iconRadius
            params.alpha = 0f
            viewRight = IconView(context.createWindowContext(
                    LayoutParams.TYPE_APPLICATION_OVERLAY, null),
                    packageName, it.first, it.second, iconRadius)
            viewRight?.isVisible = false
            viewRight?.rotation = -360f
            if (!addView(context, viewRight, params, false)) {
                viewRight = null
            }
        }

        if (viewLeft != null && viewRight != null) {
            iconViewsLeft.add(viewLeft!!)
            iconViewsRight.add(viewRight!!)
        }
    }

    fun onScreenRotationChanged(context: Context) {
        relocateIconView(context)
    }

    fun relocateIconView(context: Context) {
        hideForAll()

        if (getWindowManager(context) == null) {
            logE(TAG, "Failed to relocateIconView, WindowManager is null")
            return
        }

        val iconRadius = IconLayoutAlgorithm.getIconRadius(context)
        iconViewsLeft.forEachIndexed { i, v ->
            IconLayoutAlgorithm.getIconCenterPos(context, true, i + 1, iconViewsLeft.size).let {
                val params = IconLayoutAlgorithm.getDefaultIconLayoutParams()
                params.width = iconRadius * 2
                params.height = iconRadius * 2
                params.x = it.first - iconRadius
                params.y = it.second - iconRadius
                params.alpha = 0f
                v.centerPosX = it.first
                v.centerPosY = it.second
                v.radius = iconRadius
                try {
                    wm?.updateViewLayout(v, params)
                    logD(TAG, "relocateIconView: packageName=${v.packageName}, isLeft=true")
                } catch (e: Exception) {
                    logE(TAG, "Exception on relocateIconView for packageName=${v.packageName}, isLeft=true")
                }
            }
        }
        iconViewsRight.forEachIndexed { i, v ->
            IconLayoutAlgorithm.getIconCenterPos(context, false, i + 1, iconViewsRight.size).let {
                val params = IconLayoutAlgorithm.getDefaultIconLayoutParams()
                params.width = iconRadius * 2
                params.height = iconRadius * 2
                params.x = it.first - iconRadius
                params.y = it.second - iconRadius
                params.alpha = 0f
                v.centerPosX = it.first
                v.centerPosY = it.second
                v.radius = iconRadius
                try {
                    wm?.updateViewLayout(v, params)
                    logD(TAG, "relocateIconView: packageName=${v.packageName}, isLeft=false")
                } catch (e: Exception) {
                    logE(TAG, "Exception on relocateIconView for packageName=${v.packageName}, isLeft=false")
                }
            }
        }
    }
}
