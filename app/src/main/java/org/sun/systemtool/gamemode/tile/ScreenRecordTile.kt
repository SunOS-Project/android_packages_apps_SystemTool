/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.gamemode.tile

import org.sun.systemtool.R
import org.sun.systemtool.gamemode.controller.GamePanelViewController
import org.sun.systemtool.gamemode.util.ScreenRecordHelper
import org.sun.systemtool.gamemode.util.Shared.service

class ScreenRecordTile : BaseShortcutTile(
    R.string.game_tile_screenrecord_title,
    R.drawable.ic_screen_record
), ScreenRecordHelper.ScreenRecordCallback {

    init {
        ScreenRecordHelper.addCallback(this)
    }

    override fun destroy() {
        ScreenRecordHelper.removeCallback(this)
        super.destroy()
    }

    override fun onStartRecording() {
        state = STATE_ACTIVE
    }

    override fun onStopRecording() {
        state = STATE_INACTIVE
    }

    override fun onClicked() {
        GamePanelViewController.animateHide {
            if (ScreenRecordHelper.isStarting()) {
                return@animateHide
            }
            if (ScreenRecordHelper.isRecording()) {
                ScreenRecordHelper.stopRecord()
            } else {
                ScreenRecordHelper.startRecord()
            }
        }
    }

    override fun getInitialState(): Int {
        return if (ScreenRecordHelper.isRecording()) {
            STATE_ACTIVE
        } else {
            STATE_INACTIVE
        }
    }

    override fun getBackgroundColor(): Int {
        return when (state) {
            STATE_ACTIVE -> service.getColor(R.color.game_panel_screenrecord_active)
            else -> service.getColor(R.color.game_panel_background_inactive_default)
        }
    }

    override fun getSecondaryLabelRes(): Int {
        return when (state) {
            STATE_ACTIVE -> R.string.game_tile_screenrecord_stop
            else -> R.string.game_tile_screenrecord_start
        }
    }
}
