/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.observer

import android.app.ActivityTaskManager.INVALID_TASK_ID
import android.os.Handler

import org.nameless.app.IGameModeInfoListener
import org.nameless.systemtool.gamemode.controller.GamePanelViewController
import org.nameless.systemtool.gamemode.util.Shared.gameModeManager
import org.nameless.systemtool.gamemode.util.Shared.newGameLaunched

class GameModeInfoListener(
    private val handler: Handler,
    private val gestureListener: GameModeGestureListener
) : IGameModeInfoListener.Stub() {

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
            handler.post {
                if (value) {
                    GamePanelViewController.addPanelView()
                    gestureListener.registered = true
                } else {
                    gestureListener.registered = false
                    GamePanelViewController.removePanelView()
                }
            }
        }

    data class GameInfo(
        val packageName: String = "",
        val taskId: Int = INVALID_TASK_ID
    )

    private var lastGameInfo = GameInfo()
    private var currentGameInfo = GameInfo()

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
