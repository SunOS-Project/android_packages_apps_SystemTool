/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.observer

import org.nameless.app.IGameModeInfoListener
import org.nameless.systemtool.gamemode.bean.GameInfo
import org.nameless.systemtool.gamemode.controller.GamePanelViewController
import org.nameless.systemtool.gamemode.util.ScreenRecordHelper
import org.nameless.systemtool.gamemode.util.Shared.currentGameInfo
import org.nameless.systemtool.gamemode.util.Shared.gameModeManager
import org.nameless.systemtool.gamemode.util.Shared.newGameLaunched
import org.nameless.systemtool.gamemode.util.Shared.service

class GameModeInfoListener : IGameModeInfoListener.Stub() {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                gameModeManager.registerGameModeInfoListener(this)
            } else {
                gameModeManager.unregisterGameModeInfoListener(this)
            }
        }

    var inGame = false
        set(value) {
            field = value
            if (value) {
                ScreenRecordHelper.bind()
                GamePanelViewController.onGameStart()
                service.gameModeGestureListener.registered = true
            } else {
                service.gameModeGestureListener.registered = false
                GamePanelViewController.onGameStop()
                ScreenRecordHelper.unbind()
            }
        }

    private var lastGameInfo = GameInfo()

    override fun onGameModeInfoChanged() {
        gameModeManager.gameModeInfo?.let {
            if (it.isInGame) {
                lastGameInfo = currentGameInfo.copy()
                currentGameInfo = GameInfo(
                    it.gamePackage,
                    it.gameTaskId
                )
                newGameLaunched = lastGameInfo != currentGameInfo
            }
            if (inGame != it.isInGame) {
                inGame = it.isInGame
            }
        }
    }
}
