/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.gamemode.tile

import org.sun.app.GameModeInfo
import org.sun.app.GameModeManager
import org.sun.provider.SettingsExt.System.GAME_MODE_CALL_ACTION
import org.sun.systemtool.R
import org.sun.systemtool.gamemode.controller.AutoCallController

class AutoCallTile : BaseShortcutTile(
    R.string.game_tile_call_action_title,
    R.drawable.ic_danmaku
) {

    override fun getInitialState(): Int {
        return SettingsHelper.System.getInt(
            GAME_MODE_CALL_ACTION,
            GameModeManager.IN_GAME_CALL_NO_ACTION
        )
    }

    override fun onClicked() {
        when (state) {
            STATE_INACTIVE -> GameModeManager.IN_GAME_CALL_AUTO_ACCEPT
            STATE_CALL_ACCEPT -> GameModeManager.IN_GAME_CALL_AUTO_REJECT
            STATE_CALL_REJECT -> GameModeManager.IN_GAME_CALL_NO_ACTION
            else -> null
        }?.let {
            SettingsHelper.System.putInt(GAME_MODE_CALL_ACTION, it)
        }
    }

    override fun onGameModeInfoChanged(info: GameModeInfo) {
        super.onGameModeInfoChanged(info)
        state = when (info.callAction) {
            GameModeManager.IN_GAME_CALL_AUTO_ACCEPT -> STATE_CALL_ACCEPT
            GameModeManager.IN_GAME_CALL_AUTO_REJECT -> STATE_CALL_REJECT
            else -> STATE_INACTIVE
        }
    }

    override fun onStateChanged() {
        super.onStateChanged()
        AutoCallController.mode = when (state) {
            STATE_CALL_ACCEPT -> AutoCallController.Mode.AUTO_ACCEPT
            STATE_CALL_REJECT -> AutoCallController.Mode.AUTO_REJECT
            else -> AutoCallController.Mode.OFF
        }
    }

    override fun getIconRes(): Int {
        return when (state) {
            STATE_CALL_ACCEPT -> R.drawable.ic_call_action_accept
            STATE_CALL_REJECT -> R.drawable.ic_call_action_reject
            else -> R.drawable.ic_call_action_default
        }
    }

    override fun getSecondaryLabelRes(): Int {
        return when (state) {
            STATE_CALL_ACCEPT -> R.string.game_tile_call_action_accept
            STATE_CALL_REJECT -> R.string.game_tile_call_action_reject
            else -> R.string.game_tile_call_action_default
        }
    }

    companion object {
        private const val STATE_CALL_ACCEPT = 1
        private const val STATE_CALL_REJECT = 2
    }
}
