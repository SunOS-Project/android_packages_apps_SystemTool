/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.util

import android.util.Log

import org.nameless.os.DebugConstants.DEBUG_SYSTEM_TOOL

object Constants {

    const val PACKAGE_NAME = "org.nameless.systemtool"

    const val iconSizeRatio = 0.123f  // 142 / 1440 * iconFocusedScaleRatio

    const val iconFocusedScaleRatio = 1.25f

    const val circleRadiusRatio = 0.64f  // r = min(screenWidth, screenHeight) * circleRadiusRatio

    const val circleCenterXPort = 1.04f
    const val circleCenterXLand = 1.0f
    const val circleCenterYPort = 1.0f
    const val circleCenterYLand = 1.01f

    const val circleMaxIcon = 6  // Per app gap = 90° / (circleMaxIcon - 1)

    val miniWindowSystemAppsWhitelist = setOf<String>(
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
        "org.lineageos.aperture",
        "org.nameless.gamespace"
    )

    fun logD(tag: String, msg: String) {
        if (DEBUG_SYSTEM_TOOL) {
            Log.d(tag, msg)
        }
    }

    fun logE(tag: String, msg: String) {
        Log.e(tag, msg)
    }
}
