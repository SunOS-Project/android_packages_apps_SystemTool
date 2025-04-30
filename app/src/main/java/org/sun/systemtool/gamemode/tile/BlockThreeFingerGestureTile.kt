/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.gamemode.tile

import org.sun.app.GameModeInfo
import org.sun.provider.SettingsExt.System.GAME_MODE_DISABLE_THREE_FINGER_GESTURES
import org.sun.systemtool.R

class BlockThreeFingerGestureTile : BaseShortcutTile(
    R.string.game_tile_three_finger_gesture_title,
    R.drawable.ic_three_finger_off
) {

    override fun getInitialState(): Int {
        return if (SettingsHelper.System.getBoolean(GAME_MODE_DISABLE_THREE_FINGER_GESTURES, true)) {
            STATE_ACTIVE
        } else {
            STATE_INACTIVE
        }
    }

    override fun onClicked() {
        when (state) {
            STATE_ACTIVE -> false
            STATE_INACTIVE -> true
            else -> null
        }?.let {
            SettingsHelper.System.putBoolean(GAME_MODE_DISABLE_THREE_FINGER_GESTURES, it)
        }
    }

    override fun onGameModeInfoChanged(info: GameModeInfo) {
        super.onGameModeInfoChanged(info)
        state = if (info.shouldDisableThreeFingerGesture()) {
            STATE_ACTIVE
        } else {
            STATE_INACTIVE
        }
    }

    override fun getIconRes(): Int {
        return when (state) {
            STATE_ACTIVE -> R.drawable.ic_three_finger_off
            else -> R.drawable.ic_three_finger_default
        }
    }

    override fun getSecondaryLabelRes(): Int {
        return when (state) {
            STATE_ACTIVE -> R.string.game_tile_secondary_label_disabled
            else -> R.string.game_tile_secondary_label_follow_system
        }
    }
}
