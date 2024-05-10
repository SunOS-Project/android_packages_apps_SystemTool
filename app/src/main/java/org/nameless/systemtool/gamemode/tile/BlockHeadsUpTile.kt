/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.tile

import org.nameless.app.GameModeInfo
import org.nameless.provider.SettingsExt.System.GAME_MODE_DANMAKU_NOTIFICATION
import org.nameless.provider.SettingsExt.System.GAME_MODE_DISABLE_HEADS_UP
import org.nameless.systemtool.R

class BlockHeadsUpTile : BaseShortcutTile(
    R.string.game_tile_heads_up_title,
    R.drawable.ic_heads_up_off
) {

    override fun getInitialState(): Int {
        return if (SettingsHelper.System.getBoolean(GAME_MODE_DANMAKU_NOTIFICATION, true)
                || SettingsHelper.System.getBoolean(GAME_MODE_DISABLE_HEADS_UP, false)) {
            STATE_ACTIVE
        } else {
            STATE_INACTIVE
        }
    }

    override fun onClicked() {
        if (SettingsHelper.System.getBoolean(GAME_MODE_DANMAKU_NOTIFICATION, true)
                && state == STATE_ACTIVE) {
            SettingsHelper.System.putBoolean(GAME_MODE_DANMAKU_NOTIFICATION, false)
        }
        when (state) {
            STATE_ACTIVE -> false
            STATE_INACTIVE -> true
            else -> null
        }?.let {
            SettingsHelper.System.putBoolean(GAME_MODE_DISABLE_HEADS_UP, it)
        }
    }

    override fun onGameModeInfoChanged(info: GameModeInfo) {
        super.onGameModeInfoChanged(info)
        state = if (info.isDanmakuNotificationEnabled || info.shouldDisableHeadsUp()) {
            STATE_ACTIVE
        } else {
            STATE_INACTIVE
        }
    }

    override fun getIconRes(): Int {
        return when (state) {
            STATE_ACTIVE -> R.drawable.ic_heads_up_off
            else -> R.drawable.ic_heads_up_default
        }
    }

    override fun getSecondaryLabelRes(): Int {
        return when (state) {
            STATE_ACTIVE -> R.string.game_tile_secondary_label_disabled
            else -> R.string.game_tile_secondary_label_follow_system
        }
    }
}
