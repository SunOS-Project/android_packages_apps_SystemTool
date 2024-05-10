/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.util

import android.os.PowerManager
import android.view.Display

import com.android.settingslib.display.BrightnessUtils.GAMMA_SPACE_MAX
import com.android.settingslib.display.BrightnessUtils.convertGammaToLinearFloat
import com.android.settingslib.display.BrightnessUtils.convertLinearToGammaFloat

import org.nameless.systemtool.gamemode.util.Shared.displayManager
import org.nameless.systemtool.gamemode.util.Shared.powerManager

import kotlin.math.roundToInt

object BrightnessHelper {

    private val minBacklight: Float by lazy {
        powerManager.getBrightnessConstraint(PowerManager.BRIGHTNESS_CONSTRAINT_TYPE_MINIMUM)
    }
    private val maxBacklight: Float by lazy {
        powerManager.getBrightnessConstraint(PowerManager.BRIGHTNESS_CONSTRAINT_TYPE_MAXIMUM)
    }

    fun getBrightness(): Int {
        return convertLinearToGammaFloat(
            displayManager.getBrightness(Display.DEFAULT_DISPLAY),
            minBacklight,
            maxBacklight
        )
    }

    fun setBrightnessPercentage(percentage: Float, temp: Boolean) {
        if (percentage < 0f || percentage > 1f) {
            return
        }
        val brightness = convertGammaToLinearFloat(
            (percentage * GAMMA_SPACE_MAX).roundToInt(),
            minBacklight,
            maxBacklight
        )
        if (temp) {
            displayManager.setTemporaryBrightness(Display.DEFAULT_DISPLAY, brightness)
        } else {
            displayManager.setBrightness(Display.DEFAULT_DISPLAY, brightness)
        }
    }
}
