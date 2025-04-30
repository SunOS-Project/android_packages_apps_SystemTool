/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.gamemode.tile

import com.android.internal.util.sun.CustomUtils

import org.sun.systemtool.R
import org.sun.systemtool.gamemode.controller.GamePanelViewController

class ScreenshotTile : BaseShortcutTile(
    R.string.game_tile_screenshot_title,
    R.drawable.ic_screenshot,
    longClickable = true
) {

    override fun onClicked() {
        GamePanelViewController.animateHide {
            CustomUtils.takeScreenshot(true /* Fullscreen */)
        }
    }

    override fun onLongClicked() {
        GamePanelViewController.animateHide {
            CustomUtils.takeScreenshot(false /* Fullscreen */)
        }
    }

    override fun getSecondaryLabelRes() = R.string.game_tile_screenshot_long_click
}
