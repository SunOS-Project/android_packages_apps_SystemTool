/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.view

import android.content.Context
import android.graphics.PixelFormat
import android.view.ViewGroup
import android.view.WindowManager

import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

import org.nameless.systemtool.windowmode.util.Config.CIRCLE_CENTER_X_LAND
import org.nameless.systemtool.windowmode.util.Config.CIRCLE_CENTER_X_PORT
import org.nameless.systemtool.windowmode.util.Config.CIRCLE_CENTER_Y_LAND
import org.nameless.systemtool.windowmode.util.Config.CIRCLE_CENTER_Y_PORT
import org.nameless.systemtool.windowmode.util.Config.CIRCLE_MAX_ICON
import org.nameless.systemtool.windowmode.util.Config.CIRCLE_SCALE_RATIO
import org.nameless.systemtool.windowmode.util.Config.ICON_SIZE_RATIO
import org.nameless.systemtool.windowmode.util.Shared.navbarHeight
import org.nameless.systemtool.windowmode.util.Shared.windowManager

abstract class AppCircleViewGroup(
    context: Context,
) : ViewGroup(context) {

    init {
        setWillNotDraw(false)
    }

    abstract fun isLeft(): Boolean

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount

        var screenWidth: Int
        var screenHeight: Int
        windowManager.currentWindowMetrics.bounds.let {
            screenWidth = it.width()
            screenHeight = it.height()
        }

        val iconRadius = min(screenWidth, screenHeight) * ICON_SIZE_RATIO / 2
        val circleCenterX = if (screenWidth > screenHeight) CIRCLE_CENTER_X_LAND else CIRCLE_CENTER_X_PORT
        val circleCenterY = if (screenWidth > screenHeight) CIRCLE_CENTER_Y_LAND else CIRCLE_CENTER_Y_PORT

        for (i in 0 until count) {
            val circleIdx = CIRCLE_MAX_ICON.indexOfFirst { i + 1 <= it }
            val circleCount = CIRCLE_MAX_ICON.indexOfFirst { count <= it }
            val currentCircleTotal = if (circleIdx < circleCount) {
                if (circleIdx == 0) {
                    CIRCLE_MAX_ICON[0]
                } else {
                    CIRCLE_MAX_ICON[circleIdx] - CIRCLE_MAX_ICON[circleIdx - 1]
                }
            } else {
                if (circleIdx == 0) {
                    count
                } else {
                    count - CIRCLE_MAX_ICON[circleIdx - 1]
                }
            }
            val currentCircleIdx = if (circleIdx == 0) {
                i + 1
            } else {
                i + 1 - CIRCLE_MAX_ICON[circleIdx - 1]
            }
            val radius = min(screenWidth, screenHeight) * CIRCLE_SCALE_RATIO[circleIdx]
            val angle = 90f - 90f / (currentCircleTotal + 1) * currentCircleIdx
            val x = (if (isLeft()) {
                ((if (screenHeight > screenWidth) screenWidth * (1f - circleCenterX) else 0f) +
                        radius * cos(angle * Math.PI / 180) + iconRadius).toInt()
            } else {
                (screenWidth * circleCenterX - radius * cos(angle * Math.PI / 180) - iconRadius).toInt()
            }) - navbarHeight
            val y = (screenHeight * circleCenterY - radius * sin(angle * Math.PI / 180) - iconRadius).toInt()

            getChildAt(i).layout(
                (x - iconRadius).toInt(),
                (y - iconRadius).toInt(),
                (x + iconRadius).toInt(),
                (y + iconRadius).toInt()
            )
        }
    }

    companion object {
        @Suppress("DEPRECATION")
        val groupLayoutParams = WindowManager.LayoutParams().apply {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            format = PixelFormat.RGBA_8888
            flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            privateFlags = WindowManager.LayoutParams.PRIVATE_FLAG_TRUSTED_OVERLAY or
                    WindowManager.LayoutParams.SYSTEM_FLAG_SHOW_FOR_ALL_USERS
            layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            width = WindowManager.LayoutParams.FILL_PARENT
            height = WindowManager.LayoutParams.FILL_PARENT
        }
    }
}
