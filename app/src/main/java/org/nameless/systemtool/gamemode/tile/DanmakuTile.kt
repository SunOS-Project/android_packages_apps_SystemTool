/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.tile

import org.nameless.app.GameModeInfo
import org.nameless.provider.SettingsExt.System.GAME_MODE_DANMAKU_NOTIFICATION
import org.nameless.systemtool.R
import org.nameless.systemtool.gamemode.controller.DanmakuController

class DanmakuTile : BaseShortcutTile(
    R.string.game_tile_danmaku_notification_title,
    R.drawable.ic_danmaku
) {

    override fun getInitialState(): Int {
        return if (SettingsHelper.System.getBoolean(GAME_MODE_DANMAKU_NOTIFICATION, true)) {
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
            SettingsHelper.System.putBoolean(GAME_MODE_DANMAKU_NOTIFICATION, it)
        }
    }

    override fun onGameModeInfoChanged(info: GameModeInfo) {
        super.onGameModeInfoChanged(info)
        state = if (info.isDanmakuNotificationEnabled) {
            STATE_ACTIVE
        } else {
            STATE_INACTIVE
        }
    }

    override fun onStateChanged() {
        super.onStateChanged()
        if (state == STATE_ACTIVE) {
            DanmakuController.startListening()
        } else {
            DanmakuController.stopListening()
        }
    }
}
