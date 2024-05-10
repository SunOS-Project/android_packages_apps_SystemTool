/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder

import org.nameless.systemtool.gamemode.observer.DisplayResolutionChangeListener
import org.nameless.systemtool.gamemode.observer.GameModeGestureListener
import org.nameless.systemtool.gamemode.observer.GameModeInfoListener
import org.nameless.systemtool.gamemode.observer.RotationWatcher
import org.nameless.systemtool.gamemode.util.Shared

class GameAssistantService : Service() {

    private val handlerThread by lazy {
        HandlerThread("SystemTool::GameAssistantService").apply { start() }
    }
    val handler by lazy { Handler(handlerThread.looper) }

    private val resolutionListener by lazy {
        DisplayResolutionChangeListener(handler, gameModeInfoListener)
    }
    private val rotationWatcher by lazy {
        RotationWatcher(handler, gameModeInfoListener)
    }
    private val gameModeGestureListener by lazy {
        GameModeGestureListener()
    }
    private val gameModeInfoListener by lazy {
        GameModeInfoListener(handler, gameModeGestureListener)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        Shared.service = this

        resolutionListener.registered = true
        rotationWatcher.registered = true
        gameModeInfoListener.registered = true
    }

    override fun onDestroy() {
        gameModeInfoListener.registered = false
        gameModeGestureListener.registered = false
        rotationWatcher.registered = false
        resolutionListener.registered = false

        super.onDestroy()
    }
}
