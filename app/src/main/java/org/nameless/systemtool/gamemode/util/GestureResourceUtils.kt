/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */
package org.nameless.systemtool.gamemode.util

import android.content.res.Resources

import org.nameless.systemtool.gamemode.util.Shared.screenShortWidth

object GestureResourceUtils {

    fun getGameModeGestureValidDistance(res: Resources): Int {
        return (res.getFloat(
            com.android.internal.R.dimen.game_mode_gesture_valid_distance
        ) * screenShortWidth).toInt()
    }

    fun getGameModePortraitAreaBottom(res: Resources): Int {
        return (res.getFloat(
            com.android.internal.R.dimen.game_mode_gesture_portrait_area_bottom
        ) * screenShortWidth).toInt()
    }

    fun getGameModeLandscapeAreaBottom(res: Resources): Int {
        return (res.getFloat(
            com.android.internal.R.dimen.game_mode_gesture_landscape_area_bottom
        ) * screenShortWidth).toInt()
    }
}
