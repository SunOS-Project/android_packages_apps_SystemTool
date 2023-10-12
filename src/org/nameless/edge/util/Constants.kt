/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.util

object Constants {

    val PACKAGE_NAME = "org.nameless.edge"

    val gestureRegionShort = 0.135f  // 1440 * gestureRegionShort
    val gestureRegionLong = 0.042f  // 3168 * gestureRegionLong

    val iconSizeRatio = 0.115f  // 132 / 1440 * iconFocusedScaleRatio

    val iconFocusedScaleRatio = 1.25f

    val circleRadiusRatio = 0.6f  // r = min(screenWidth, screenHeight) * circleRadiusRatio

    val circleCenterXPort = 1.04f
    val circleCenterXLand = 1.04f
    val circleCenterYPort = 1.01f
    val circleCenterYLand = 0.97f

    val circleMaxIcon = 6  // Per app gap = 90° / (circleMaxIcon - 1)

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
}
