/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.view

import android.content.Context
import android.graphics.PixelFormat
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager.LayoutParams

import androidx.core.view.isVisible

import org.nameless.edge.util.ViewHolder
import org.nameless.wm.PopUpDebugHelper.logE

class DimmerView(context: Context) : View(context) {

    var offsetX = 0

    init {
        ViewHolder.dimmerView = this
    }

    private val gestureDector = GestureDetector(context, object: SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            ViewHolder.hideForAll()
            return super.onSingleTapUp(e)
        }
    })

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?: return true
        ViewHolder.iconViewsShowing.forEach {
            if (it.inTouchRegion(event.x - offsetX, event.y)) {
                return it.onTouchEvent(event)
            }
            it.resetState()
        }
        return gestureDector.onTouchEvent(event)
    }

    companion object {
        private val TAG = "EdgeTool::DimmerView"

        private val dimmerLayoutParams = LayoutParams().apply {
            type = LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.RGBA_8888
            flags = LayoutParams.FLAG_DIM_BEHIND or
                    LayoutParams.FLAG_HARDWARE_ACCELERATED or
                    LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    LayoutParams.FLAG_NOT_FOCUSABLE
            privateFlags = LayoutParams.SYSTEM_FLAG_SHOW_FOR_ALL_USERS
            layoutInDisplayCutoutMode =
                    LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            dimAmount = 0.4f
            width = LayoutParams.FILL_PARENT
            height = LayoutParams.FILL_PARENT
        }

        fun addDimmerView(context: Context): Boolean {
            if (ViewHolder.getWindowManager(context) == null) {
                logE(TAG, "Failed to addDimmerView: WindowManger is null")
                return false
            }
            try {
                ViewHolder.getWindowManager(context)?.addView(
                    DimmerView(context.createWindowContext(
                        LayoutParams.TYPE_APPLICATION_OVERLAY, null)).apply {
                            isVisible = false
                        }, dimmerLayoutParams)
                return true
            } catch (e: Exception) {
                logE(TAG, "Exception on addDimmerView")
                return false
            }
        }
    }
}
