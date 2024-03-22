/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.util

object Config {

    const val ICON_SIZE_RATIO = 0.0986f  // 142 / 1440
    const val CIRCLE_SCALE_RATIO = 0.64f  // r = min(screenWidth, screenHeight) * circleRadiusRatio

    const val CIRCLE_CENTER_X_PORT = 1.04f
    const val CIRCLE_CENTER_X_LAND = 1.0f
    const val CIRCLE_CENTER_Y_PORT = 1.0f
    const val CIRCLE_CENTER_Y_LAND = 1.01f

    const val CIRCLE_MAX_ICON = 6  // Per app gap = 90° / (circleMaxIcon - 1)

    const val HIDE_ANIMATION_DURATION = 100L
    const val REBOUND_ANIMATION_DURATION = 250L
    const val SHOW_ANIMATION_DURATION = 130L
    const val FOCUS_ANIMATION_DURATION = 130L

    const val ROTATE_START_ANGLE = -270f
    const val ROTATE_REBOUND_ANGLE = 5f

    const val SCALE_START_VALUE = 0.8f
    const val SCALE_REBOUND_VALUE = 1.05f
    const val SCALE_FOCUS_VALUE = 1.25f

    val miniWindowSystemAppsWhitelist = setOf(
        "com.android.chrome",
        "com.android.settings",
        "com.android.vending",
        "com.google.android.apps.messaging",
        "com.google.android.apps.nbu.files",
        "com.google.android.apps.photos",
        "com.google.android.apps.recorder",
        "com.google.android.calculator",
        "com.google.android.calendar",
        "com.google.android.contacts",
        "com.google.android.deskclock",
        "com.google.android.dialer",
        "com.google.android.googlequicksearchbox",
        "com.oneplus.camera",
        "com.oneplus.gallery",
        "com.oplus.camera",
        "org.lineageos.aperture"
    )
}
