/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.gamemode.tile

import org.sun.app.GameModeInfo
import org.sun.provider.SettingsExt.System.GAME_MODE_LOCK_GESTURES
import org.sun.provider.SettingsExt.System.GAME_MODE_LOCK_STATUS_BAR
import org.sun.systemtool.R

class PreventMistouchTile : BaseShortcutTile(
    R.string.game_tile_prevent_mistouch_title,
    R.drawable.ic_gesture_mistouch
) {

    override fun getInitialState(): Int {
        val lockGesture = SettingsHelper.System.getBoolean(GAME_MODE_LOCK_GESTURES, false)
        val lockStatusBar = SettingsHelper.System.getBoolean(GAME_MODE_LOCK_STATUS_BAR, false)
        return if (lockGesture && lockStatusBar) {
            STATE_LOCK_ALL
        } else if (lockGesture) {
            STATE_LOCK_GESTURE
        } else if (lockStatusBar) {
            STATE_LOCK_STATUS_BAR
        } else {
            STATE_INACTIVE
        }
    }

    override fun onClicked() {
        when (state) {
            STATE_INACTIVE -> {
                SettingsHelper.System.putBoolean(GAME_MODE_LOCK_GESTURES, true)
                SettingsHelper.System.putBoolean(GAME_MODE_LOCK_STATUS_BAR, false)
            }
            STATE_LOCK_GESTURE -> {
                SettingsHelper.System.putBoolean(GAME_MODE_LOCK_GESTURES, false)
                SettingsHelper.System.putBoolean(GAME_MODE_LOCK_STATUS_BAR, true)
            }
            STATE_LOCK_STATUS_BAR -> {
                SettingsHelper.System.putBoolean(GAME_MODE_LOCK_GESTURES, true)
                SettingsHelper.System.putBoolean(GAME_MODE_LOCK_STATUS_BAR, true)
            }
            STATE_LOCK_ALL -> {
                SettingsHelper.System.putBoolean(GAME_MODE_LOCK_GESTURES, false)
                SettingsHelper.System.putBoolean(GAME_MODE_LOCK_STATUS_BAR, false)
            }
            else -> {}
        }
    }

    override fun onGameModeInfoChanged(info: GameModeInfo) {
        super.onGameModeInfoChanged(info)
        val lockGesture = info.shouldLockGesture()
        val lockStatusBar = info.shouldLockStatusbar()
        state =  if (lockGesture && lockStatusBar) {
            STATE_LOCK_ALL
        } else if (lockGesture) {
            STATE_LOCK_GESTURE
        } else if (lockStatusBar) {
            STATE_LOCK_STATUS_BAR
        } else {
            STATE_INACTIVE
        }
    }

    override fun getSecondaryLabelRes(): Int {
        return when (state) {
            STATE_LOCK_GESTURE -> R.string.game_tile_prevent_mistouch_lock_gesture
            STATE_LOCK_STATUS_BAR -> R.string.game_tile_prevent_mistouch_lock_statusbar
            STATE_LOCK_ALL -> R.string.game_tile_prevent_mistouch_lock_all
            else -> R.string.game_tile_secondary_label_inactive
        }
    }

    companion object {
        private const val STATE_LOCK_GESTURE = 1
        private const val STATE_LOCK_STATUS_BAR = 2
        private const val STATE_LOCK_ALL = 3
    }
}
