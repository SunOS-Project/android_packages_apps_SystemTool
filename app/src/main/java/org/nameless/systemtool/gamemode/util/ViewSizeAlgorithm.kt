/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.util

import android.os.RemoteException
import android.view.Display
import android.view.WindowManagerGlobal

import org.nameless.systemtool.gamemode.util.Shared.screenShortWidth
import org.nameless.view.DisplayResolutionManager

object ViewSizeAlgorithm {

    fun calculateFixedSize(origSize: Int, considerDensity: Boolean, considerRes: Boolean): Int {
        var size = origSize.toFloat()
        if (considerDensity) {
            size *= calculateScaleRatio()
        }
        if (considerRes) {
            size *= DisplayResolutionManager.getDensityScale(screenShortWidth)
        }
        return size.toInt()
    }

    private fun calculateScaleRatio(): Float {
        WindowManagerGlobal.getWindowManagerService()?.let { wm ->
            val initialDensity = try {
                wm.getInitialDisplayDensity(Display.DEFAULT_DISPLAY)
            } catch (e: RemoteException) {
                return 1.0f
            }
            val baseDensity = try {
                wm.getBaseDisplayDensity(Display.DEFAULT_DISPLAY)
            } catch (e: RemoteException) {
                return 1.0f
            }
            return initialDensity.toFloat() / baseDensity
        }
        return 1.0f
    }
}
