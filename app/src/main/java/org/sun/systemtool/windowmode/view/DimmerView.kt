/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.windowmode.view

import android.content.Context
import android.graphics.PixelFormat
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager.LayoutParams

import androidx.core.view.children
import androidx.core.view.isVisible

import org.sun.systemtool.common.Utils.logE
import org.sun.systemtool.windowmode.ViewAnimator
import org.sun.systemtool.windowmode.util.Shared.dimmerView
import org.sun.systemtool.windowmode.util.Shared.service
import org.sun.systemtool.windowmode.util.Shared.windowManager

class DimmerView(context: Context) : View(context) {

    var offsetX = 0

    init {
        dimmerView = this
    }

    private val gestureDetector = GestureDetector(context, object: SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            ViewAnimator.hideCircle()
            return super.onSingleTapUp(e)
        }
    })

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (ViewAnimator.currentlyVisible &&
            event.keyCode == KeyEvent.KEYCODE_BACK &&
            event.action == KeyEvent.ACTION_UP) {
            ViewAnimator.hideCircle()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!ViewAnimator.animating) {
            ViewAnimator.circleViewGroup?.children?.map { it as CircleIconView }?.forEach {
                if (it.inTouchRegion(event.x - offsetX, event.y)) {
                    return it.onTouchEvent(event)
                }
                it.resetState()
            }
        }
        return gestureDetector.onTouchEvent(event)
    }

    companion object {
        private const val TAG = "SystemTool::DimmerView"

        @Suppress("DEPRECATION")
        private val dimmerLayoutParams = LayoutParams().apply {
            type = LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.RGBA_8888
            flags = LayoutParams.FLAG_DIM_BEHIND or
                    LayoutParams.FLAG_LAYOUT_IN_SCREEN
            privateFlags = LayoutParams.SYSTEM_FLAG_SHOW_FOR_ALL_USERS
            layoutInDisplayCutoutMode =
                    LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            dimAmount = 0.4f
            width = LayoutParams.FILL_PARENT
            height = LayoutParams.FILL_PARENT
        }

        fun addDimmerView(): Boolean {
            try {
                windowManager.addView(
                    DimmerView(service.createWindowContext(
                        LayoutParams.TYPE_APPLICATION_OVERLAY, null)).apply {
                            isVisible = false
                        }, dimmerLayoutParams)
                return true
            } catch (e: Exception) {
                logE(TAG, "Exception on addDimmerView")
            }
            return false
        }

        fun removeDimmerView() {
            try {
                windowManager.removeView(dimmerView)
            } catch (e: Exception) {
                logE(TAG, "Exception on removeDimmerView")
            }
        }
    }
}
