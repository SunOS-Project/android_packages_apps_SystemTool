/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.gamemode.tile

import org.sun.app.GameModeInfo
import org.sun.provider.SettingsExt.System.GAME_MODE_SILENT_NOTIFICATION
import org.sun.systemtool.R

class MuteNotificationTile : BaseShortcutTile(
    R.string.game_tile_mute_notification_title,
    R.drawable.ic_mute_notification_off
) {

    override fun getInitialState(): Int {
        return if (SettingsHelper.System.getBoolean(GAME_MODE_SILENT_NOTIFICATION, false)) {
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
            SettingsHelper.System.putBoolean(GAME_MODE_SILENT_NOTIFICATION, it)
        }
    }

    override fun onGameModeInfoChanged(info: GameModeInfo) {
        super.onGameModeInfoChanged(info)
        state = if (info.shouldMuteNotification()) {
            STATE_ACTIVE
        } else {
            STATE_INACTIVE
        }
    }

    override fun getIconRes(): Int {
        return when (state) {
            STATE_ACTIVE -> R.drawable.ic_mute_notification_on
            else -> R.drawable.ic_mute_notification_off
        }
    }
}
