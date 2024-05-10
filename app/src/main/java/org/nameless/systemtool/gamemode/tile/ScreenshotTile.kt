/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.tile

import com.android.internal.util.nameless.CustomUtils

import org.nameless.systemtool.R
import org.nameless.systemtool.gamemode.controller.GamePanelViewController

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
