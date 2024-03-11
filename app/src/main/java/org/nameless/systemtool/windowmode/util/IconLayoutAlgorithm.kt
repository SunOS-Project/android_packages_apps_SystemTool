/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.util

import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager.LayoutParams

import org.nameless.systemtool.windowmode.util.Config.circleCenterXLand
import org.nameless.systemtool.windowmode.util.Config.circleCenterXPort
import org.nameless.systemtool.windowmode.util.Config.circleCenterYLand
import org.nameless.systemtool.windowmode.util.Config.circleCenterYPort
import org.nameless.systemtool.windowmode.util.Config.circleRadiusRatio
import org.nameless.systemtool.windowmode.util.Config.iconSizeRatio
import org.nameless.systemtool.windowmode.util.Shared.service
import org.nameless.systemtool.windowmode.util.Shared.windowManager

import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object IconLayoutAlgorithm {

    private val iconLayoutParams = LayoutParams().apply {
        type = LayoutParams.TYPE_APPLICATION_OVERLAY
        format = PixelFormat.RGBA_8888
        flags = LayoutParams.FLAG_HARDWARE_ACCELERATED or
                LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                LayoutParams.FLAG_NOT_TOUCHABLE
        privateFlags = LayoutParams.PRIVATE_FLAG_NO_MOVE_ANIMATION or
                       LayoutParams.PRIVATE_FLAG_TRUSTED_OVERLAY or
                       LayoutParams.SYSTEM_FLAG_SHOW_FOR_ALL_USERS
        gravity = Gravity.TOP or Gravity.LEFT
        layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
    }

    var gesturalMode = false
    var rotationNeedsConsumeNavbar = false
    var navbarHeight = 0

    var iconRadius = 0
        get() {
            var width: Int
            var height: Int
            windowManager.currentWindowMetrics.bounds.let {
                width = it.width()
                height = it.height()
            }
            var iconRadius = min(width, height) / 2 * iconSizeRatio
            return iconRadius.toInt()
        }

    var defaultIconLayoutParams = LayoutParams()
        get() = LayoutParams().apply {
            copyFrom(iconLayoutParams)
        }

    fun getIconCenterPos(isLeft: Boolean, idx: Int, total: Int): Pair<Int, Int> {
        if (idx <= 0) {
            throw Exception("getIconCenterPos, index starts from 1!")
        }
        if (idx > total) {
            throw Exception("getIconCenterPos, index of icon cannot be larger than total icons!")
        }
        var width: Int
        var height: Int
        windowManager.currentWindowMetrics.bounds.let {
            width = it.width()
            height = it.height()
        }
        val radius = min(width, height) * circleRadiusRatio
        val iconRadius = min(width, height) * iconSizeRatio / 2
        var circleCenterX = if (width > height) circleCenterXLand else circleCenterXPort
        val circleCenterY = if (width > height) circleCenterYLand else circleCenterYPort
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

    fun updateNavbarHeight() {
        navbarHeight = if (rotationNeedsConsumeNavbar) {
            if (gesturalMode) {
                0
            } else {
                service.resources.getDimensionPixelSize(
                    com.android.internal.R.dimen.navigation_bar_height_landscape
                )
            }
        } else {
            0
        }
    }
}
