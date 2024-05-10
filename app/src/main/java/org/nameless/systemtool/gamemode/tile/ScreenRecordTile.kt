/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.tile

import android.graphics.Color

import com.android.systemui.screenrecord.IRecordingCallback

import org.nameless.systemtool.R
import org.nameless.systemtool.gamemode.controller.GamePanelViewController
import org.nameless.systemtool.gamemode.util.ScreenRecordHelper.recorder

class ScreenRecordTile : BaseShortcutTile(
    R.string.game_tile_screenrecord_title,
    R.drawable.ic_screen_record
) {

    private val callback = object : IRecordingCallback.Stub() {
        override fun onRecordingStart() {
            state = STATE_ACTIVE
        }

        override fun onRecordingEnd() {
            state = STATE_INACTIVE
        }
    }

    override fun onAttach() {
        super.onAttach()
        recorder?.addRecordingCallback(callback)
    }

    override fun onDetach() {
        recorder?.removeRecordingCallback(callback)
        super.onDetach()
    }

    override fun onClicked() {
        GamePanelViewController.animateHide {
            recorder?.let {
                if (it.isStarting) {
                    return@animateHide
                }
                if (it.isRecording) {
                    it.stopRecording()
                } else {
                    it.startRecording()
                }
            }
        }
    }

    override fun getInitialState(): Int {
        return if (recorder?.isRecording == true) {
            STATE_ACTIVE
        } else {
            STATE_INACTIVE
        }
    }

    override fun getBackgroundColor(): Int {
        return when (state) {
            STATE_ACTIVE -> Color.parseColor(COLOR_ACTIVE_BACKGROUND)
            else -> Color.parseColor(DEFAULT_COLOR_INACTIVE_BACKGROUND)
        }
    }

    override fun getSecondaryLabelRes(): Int {
        return when (state) {
            STATE_ACTIVE -> R.string.game_tile_screenrecord_stop
            else -> R.string.game_tile_screenrecord_start
        }
    }

    companion object {
        private const val COLOR_ACTIVE_BACKGROUND = "#ef4040"
    }
}
