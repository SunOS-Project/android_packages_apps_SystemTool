/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Rect
import android.view.WindowManager.LayoutParams

import androidx.core.animation.doOnEnd
import androidx.core.view.isVisible

import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.common.Utils.logE
import org.nameless.systemtool.windowmode.util.IconLayoutAlgorithm
import org.nameless.systemtool.windowmode.util.Shared.appFocusManager
import org.nameless.systemtool.windowmode.util.Shared.dimmerView
import org.nameless.systemtool.windowmode.util.Shared.service
import org.nameless.systemtool.windowmode.util.Shared.windowManager
import org.nameless.systemtool.windowmode.view.IconView

object ViewHolder {

    private const val TAG = "SystemTool::ViewHolder"

    private const val HIDE_ANIMATION_DURATION = 250L
    private const val REBOUND_ANIMATION_DURATION = 300L
    private const val SHOW_ANIMATION_DURATION = 100L

    private const val ROTATE_REBOUND_ANGLE = 30f

    private val iconViewsLeft: MutableList<IconView> = mutableListOf()
    private val iconViewsRight: MutableList<IconView> = mutableListOf()
    val iconViewsShowing: MutableList<IconView> = mutableListOf()

    var currentlyVisible = false
    var allowVisible = true

    private fun addView(view: IconView?, params: LayoutParams, isLeft: Boolean): Boolean {
        if (view == null) {
            return false
        }
        try {
            windowManager.addView(view, params)
            logD(TAG, "addView: packageName=${view.packageName}, isLeft=$isLeft")
            return true
        } catch (e: Exception) {
            logE(TAG, "Exception on addView for packageName=${view.packageName}, isLeft=$isLeft", e)
        }
        return false
    }

    private fun removeView(view: IconView, isLeft: Boolean) {
        try {
            windowManager.removeView(view)
            logD(TAG, "removeView: packageName=${view.packageName}, isLeft=$isLeft")
        } catch (e: Exception) {
            logE(TAG, "Exception on removeView for packageName=${view.packageName}, isLeft=$isLeft", e)
        }
    }

    fun removeDimmerView() {
        try {
            windowManager.removeView(dimmerView)
            logD(TAG, "removeDimmerView")
        } catch (e: Exception) {
            logE(TAG, "Exception on removeDimmerView", e)
        }
    }

    fun showForAll(isLeft: Boolean, displayBounds: Rect) {
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

        dimmerView.postOnAnimation {
            dimmerView.layoutParams =
                (dimmerView.layoutParams as LayoutParams).apply {
                    if (appFocusManager.hasMiniWindowFocus()) {
                        flags = flags and LayoutParams.FLAG_DIM_BEHIND.inv()
                    } else {
                        flags = flags or LayoutParams.FLAG_DIM_BEHIND
                    }
                }
            windowManager.updateViewLayout(dimmerView, dimmerView.layoutParams)
            dimmerView.isVisible = true
        }

        views.forEach {
            iconViewsShowing.add(it)
            val params = it.layoutParams as LayoutParams
            val prevX = params.x
            val prevY = params.y
            params.x = if (isLeft) (-it.radius * 2) else (displayBounds.width() + it.radius * 2)
            params.y = displayBounds.height() + it.radius * 2
            params.alpha = 0f
            val transX = params.x - prevX
            val transY = params.y - prevY

            it.postOnAnimation {
                windowManager.updateViewLayout(it, params)
                it.isVisible = true

                val rotateClockwise = ObjectAnimator.ofFloat(
                        it, "rotation", -360f, ROTATE_REBOUND_ANGLE)
                    .setDuration(SHOW_ANIMATION_DURATION)
                    .apply {
                        addUpdateListener { a ->
                            params.x = (prevX + (1f - a.animatedFraction) * transX).toInt()
                            params.y = (prevY + (1f - a.animatedFraction) * transY).toInt()
                            params.alpha = a.animatedFraction
                            windowManager.updateViewLayout(it, params)
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

        dimmerView.postOnAnimationDelayed({
            dimmerView.isVisible = false
        }, HIDE_ANIMATION_DURATION)

        iconViewsShowing.clear()
    }

    fun safelyClearIconViews() {
        hideForAll()

        iconViewsLeft.forEach { removeView(it, true) }
        iconViewsRight.forEach { removeView(it, false) }
        iconViewsLeft.clear()
        iconViewsRight.clear()
    }

    fun addIconView(packageName: String, idx: Int, total: Int) {
        val iconRadius = IconLayoutAlgorithm.getIconRadius()

        var viewLeft: IconView?
        IconLayoutAlgorithm.getIconCenterPos(true, idx, total).let {
            val params = IconLayoutAlgorithm.getDefaultIconLayoutParams()
            params.width = iconRadius * 2
            params.height = iconRadius * 2
            params.x = it.first - iconRadius
            params.y = it.second - iconRadius
            params.alpha = 0f
            viewLeft = IconView(
                    service.createWindowContext(
                    LayoutParams.TYPE_APPLICATION_OVERLAY, null),
                    packageName, it.first, it.second, iconRadius)
            viewLeft?.isVisible = false
            viewLeft?.rotation = -360f
            if (!addView(viewLeft, params, true)) {
                viewLeft = null
            }
        }

        var viewRight: IconView?
        IconLayoutAlgorithm.getIconCenterPos(false, idx, total).let {
            val params = IconLayoutAlgorithm.getDefaultIconLayoutParams()
            params.width = iconRadius * 2
            params.height = iconRadius * 2
            params.x = it.first - iconRadius
            params.y = it.second - iconRadius
            params.alpha = 0f
            viewRight = IconView(
                    service.createWindowContext(
                    LayoutParams.TYPE_APPLICATION_OVERLAY, null),
                    packageName, it.first, it.second, iconRadius)
            viewRight?.isVisible = false
            viewRight?.rotation = -360f
            if (!addView(viewRight, params, false)) {
                viewRight = null
            }
        }

        if (viewLeft != null && viewRight != null) {
            iconViewsLeft.add(viewLeft!!)
            iconViewsRight.add(viewRight!!)
        }
    }

    fun onScreenRotationChanged() {
        relocateIconView()
    }

    fun relocateIconView() {
        hideForAll()

        val iconRadius = IconLayoutAlgorithm.getIconRadius()
        iconViewsLeft.forEachIndexed { i, v ->
            IconLayoutAlgorithm.getIconCenterPos(true, i + 1, iconViewsLeft.size).let {
                val params = IconLayoutAlgorithm.getDefaultIconLayoutParams()
                params.width = iconRadius * 2
                params.height = iconRadius * 2
                params.x = it.first - iconRadius
                params.y = it.second - iconRadius
                params.alpha = 0f
                v.centerPosX = it.first
                v.centerPosY = it.second
                v.radius = iconRadius
                v.post {
                    try {
                        windowManager.updateViewLayout(v, params)
                        logD(TAG, "relocateIconView: packageName=${v.packageName}, isLeft=true")
                    } catch (e: Exception) {
                        logE(TAG, "Exception on relocateIconView for packageName=${v.packageName}, isLeft=true", e)
                    }
                }
            }
        }
        iconViewsRight.forEachIndexed { i, v ->
            IconLayoutAlgorithm.getIconCenterPos(false, i + 1, iconViewsRight.size).let {
                val params = IconLayoutAlgorithm.getDefaultIconLayoutParams()
                params.width = iconRadius * 2
                params.height = iconRadius * 2
                params.x = it.first - iconRadius
                params.y = it.second - iconRadius
                params.alpha = 0f
                v.centerPosX = it.first
                v.centerPosY = it.second
                v.radius = iconRadius
                v.post {
                    try {
                        windowManager.updateViewLayout(v, params)
                        logD(TAG, "relocateIconView: packageName=${v.packageName}, isLeft=false")
                    } catch (e: Exception) {
                        logE(TAG, "Exception on relocateIconView for packageName=${v.packageName}, isLeft=false", e)
                    }
                }
            }
        }
    }
}
