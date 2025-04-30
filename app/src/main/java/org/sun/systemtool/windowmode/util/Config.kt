/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.windowmode.util

object Config {

    const val ICON_SIZE_RATIO = 0.1f  // 144 / 1440

    const val CIRCLE_CENTER_X_PORT = 1.04f
    const val CIRCLE_CENTER_X_LAND = 1.0f
    const val CIRCLE_CENTER_Y_PORT = 1.0f
    const val CIRCLE_CENTER_Y_LAND = 1.01f

    val CIRCLE_MAX_ICON_NORMAL = intArrayOf(6, 10, 11)
    val CIRCLE_MAX_ICON_WIDER = intArrayOf(6, 10, 18, 19)
    // r = min(screenWidth, screenHeight) * circleRadiusRatio
    private val CIRCLE_RADIUS_RATIO_NORMAL = floatArrayOf(0.64f, 0.46f, 0.28f)
    private val CIRCLE_RADIUS_RATIO_WIDER = floatArrayOf(0.64f, 0.46f, 0.82f, 0.28f)

    fun getCircleMaxIconArray(iconCount: Int): IntArray {
        return if (iconCount > CIRCLE_MAX_ICON_NORMAL[CIRCLE_MAX_ICON_NORMAL.size - 1]) {
            CIRCLE_MAX_ICON_WIDER
        } else {
            CIRCLE_MAX_ICON_NORMAL
        }
    }

    fun getCircleRadiusRatioArray(iconCount: Int): FloatArray {
        return if (iconCount > CIRCLE_MAX_ICON_NORMAL[CIRCLE_MAX_ICON_NORMAL.size - 1]) {
            CIRCLE_RADIUS_RATIO_WIDER
        } else {
            CIRCLE_RADIUS_RATIO_NORMAL
        }
    }

    const val HIDE_ANIMATION_DURATION = 100L
    const val REBOUND_ANIMATION_DURATION = 250L
    const val SHOW_ANIMATION_DURATION = 120L
    const val FOCUS_ANIMATION_DURATION = 130L

    const val ROTATE_START_ANGLE = -270f
    const val ROTATE_REBOUND_ANGLE = 5f

    const val SCALE_START_VALUE = 0.8f
    const val SCALE_REBOUND_VALUE = 1.05f
    const val SCALE_FOCUS_VALUE = 1.16f

    const val ITEM_SCALE_VALUE = 0.85f
    const val ITEM_SCALE_DURATION = 200L

    val miniWindowSystemAppsWhitelist = setOf(
        "com.android.chrome",
        "com.android.settings",
        "com.android.vending",
        "com.coloros.gallery3d",
        "com.google.android.apps.messaging",
        "com.google.android.apps.nbu.files",
        "com.google.android.apps.photos",
        "com.google.android.apps.recorder",
        "com.google.android.apps.safetyhub",
        "com.google.android.calculator",
        "com.google.android.calendar",
        "com.google.android.contacts",
        "com.google.android.deskclock",
        "com.google.android.dialer",
        "com.google.android.googlequicksearchbox",
        "com.nothing.camera",
        "com.oneplus.camera",
        "com.oneplus.gallery",
        "com.oplus.camera",
        "org.lineageos.aperture"
    )

    val shortcutSystemAppsBlacklist = setOf(
        "com.android.settings",
        "com.android.vending",
        "com.google.android.apps.messaging",
        "com.google.android.apps.nbu.files",
        "com.google.android.apps.safetyhub",
        "com.google.android.calendar",
        "com.google.android.contacts",
        "com.google.android.dialer",
        "com.google.android.googlequicksearchbox"
    )
}
