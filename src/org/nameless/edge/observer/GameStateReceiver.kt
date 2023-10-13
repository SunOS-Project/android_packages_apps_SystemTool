/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler

class GameStateReceiver(
    private val context: Context,
    private val handler: Handler
) : BroadcastReceiver() {

    private var gameActive = false

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            INTENT_GAME_SPACE_ENTER -> {
                gameActive = true
            }
            INTENT_GAME_SPACE_STOP -> {
                gameActive = false
            }
        }
    }

    fun register() {
        context.registerReceiver(this, IntentFilter().apply {
            addAction(INTENT_GAME_SPACE_ENTER)
            addAction(INTENT_GAME_SPACE_STOP)
        }, null, handler)
    }

    fun unregister() {
        context.unregisterReceiver(this)
    }

    fun isInGameMode() = gameActive

    companion object {
        private val INTENT_GAME_SPACE_ENTER = "org.nameless.gamespace.action.GAME_START"
        private val INTENT_GAME_SPACE_STOP = "org.nameless.gamespace.action.GAME_STOP"
    }
}
