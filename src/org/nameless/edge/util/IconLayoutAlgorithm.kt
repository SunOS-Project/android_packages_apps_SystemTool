/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.util

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager.LayoutParams

import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object IconLayoutAlgorithm {

    private val iconLayoutParams = LayoutParams().apply {
        type = LayoutParams.TYPE_APPLICATION_OVERLAY
        format = PixelFormat.RGBA_8888
        flags = LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                LayoutParams.FLAG_NOT_TOUCHABLE
        privateFlags = LayoutParams.PRIVATE_FLAG_TRUSTED_OVERLAY or
                       LayoutParams.SYSTEM_FLAG_SHOW_FOR_ALL_USERS
        gravity = Gravity.TOP or Gravity.LEFT
        layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

    var gesturalMode = false
    var rotationNeedsConsumeNavbar = false
    var navbarHeight = 0

    fun getIconCenterPos(context: Context, isLeft: Boolean, idx: Int, total: Int): Pair<Int, Int> {
        if (idx <= 0) {
            throw Exception("getIconCenterPos, index starts from 1!")
        }
        if (idx > total) {
            throw Exception("getIconCenterPos, index of icon cannot be larger than total icons!")
        }
        var width = 0
        var height = 0
        ViewHolder.getWindowManager(context)?.currentWindowMetrics?.bounds?.let {
            width = it.width()
            height = it.height()
        } ?: throw Exception("getIconCenterPos, WindowManager is null!")
        val radius = min(width, height) * Constants.circleRadiusRatio
        val iconRadius = min(width, height) * Constants.iconSizeRatio / 2
        var circleCenterX = if (width > height) Constants.circleCenterXLand else Constants.circleCenterXPort
        val circleCenterY = if (width > height) Constants.circleCenterYLand else Constants.circleCenterYPort
        val angle = 90f - 90f / (total + 1) * idx

        val x = (if (isLeft) {
            ((if (height > width) width * (1f - circleCenterX) else 0f) +
                    radius * cos(angle * Math.PI / 180) + iconRadius).toInt()
        } else {
            (width * circleCenterX - radius * cos(angle * Math.PI / 180) - iconRadius).toInt()
        }) - navbarHeight
        val y = (height * circleCenterY - radius * sin(angle * Math.PI /180) - iconRadius).toInt()

        return Pair(x, y)
    }

    fun getIconRadius(context: Context): Int {
        var width = 0
        var height = 0
        ViewHolder.getWindowManager(context)?.currentWindowMetrics?.bounds?.let {
            width = it.width()
            height = it.height()
        } ?: throw Exception("getIconRadius, WindowManager is null!")
        var iconRadius = min(width, height) / 2 * Constants.iconSizeRatio
        return iconRadius.toInt()
    }

    fun getDefaultIconLayoutParams(): LayoutParams {
        return LayoutParams().apply {
            copyFrom(iconLayoutParams)
        }
    }

    fun updateNarbarHeight(context: Context) {
        if (rotationNeedsConsumeNavbar) {
            if (gesturalMode) {
                navbarHeight = 0
            } else {
                navbarHeight = context.resources.getDimensionPixelSize(
                    com.android.internal.R.dimen.navigation_bar_height_landscape
                )
            }
        } else {
            navbarHeight = 0
        }
    }
}
