/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.util

import org.nameless.app.GameModeInfo
import org.nameless.app.IGameModeInfoListener
import org.nameless.systemtool.gamemode.bean.GameInfo
import org.nameless.systemtool.gamemode.controller.GamePanelViewController
import org.nameless.systemtool.gamemode.util.Shared.currentGameInfo
import org.nameless.systemtool.gamemode.util.Shared.gameModeManager
import org.nameless.systemtool.gamemode.util.Shared.inGame
import org.nameless.systemtool.gamemode.util.Shared.lastGameInfo
import org.nameless.systemtool.gamemode.util.Shared.newGameLaunched
import org.nameless.systemtool.gamemode.util.Shared.service

object GameModeListenerProxy : IGameModeInfoListener.Stub() {

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
    var gameModeInfo: GameModeInfo? = null
        get() {
            return if (field != null) {
                field
            } else {
                gameModeManager.gameModeInfo
            }
        }
        private set

    private val callbacks = mutableSetOf<Callback>()

    fun addCallback(callback: Callback) {
        callbacks.add(callback)
    }

    fun removeCallback(callback: Callback) {
        callbacks.remove(callback)
    }

    override fun onGameModeInfoChanged() {
        gameModeInfo = gameModeManager.gameModeInfo
        gameModeInfo?.let {
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
                handleInGameChanged()
            }
        }
        callbacks.forEach { it.onGameModeInfoChanged() }
    }

    private fun handleInGameChanged() {
        if (inGame) {
            ScreenRecordHelper.bind()
            GamePanelViewController.onGameStart()
            service.gameModeGestureListener.registered = true
        } else {
            service.gameModeGestureListener.registered = false
            GamePanelViewController.onGameStop()
            ScreenRecordHelper.unbind()
        }
    }

    interface Callback {
        fun onGameModeInfoChanged()
    }
}
