/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.util

object Constants {

    val iconSizeRatio = 0.115f  // 132 / 1440 * iconFocusedScaleRatio

    val iconFocusedScaleRatio = 1.25f

    val circleRadiusRatio = 0.6f  // r = min(screenWidth, screenHeight) * circleRadiusRatio

    val circleCenterXPort = 1.04f
    val circleCenterXLand = 1.04f
    val circleCenterYPort = 1.01f
    val circleCenterYLand = 0.97f

    val circleMaxIcon = 6  // Per app gap = 90° / (circleMaxIcon - 1)
}
