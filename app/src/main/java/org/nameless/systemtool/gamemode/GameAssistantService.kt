/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper

import org.nameless.systemtool.gamemode.controller.GamePanelViewController
import org.nameless.systemtool.gamemode.observer.DisplayResolutionChangeListener
import org.nameless.systemtool.gamemode.observer.GameModeGestureListener
import org.nameless.systemtool.gamemode.util.GameModeListenerProxy
import org.nameless.systemtool.gamemode.util.Shared

class GameAssistantService : Service() {

    private val handlerThread by lazy {
        HandlerThread("SystemTool::GameAssistantService").apply { start() }
    }
    val handler by lazy { Handler(handlerThread.looper) }
    val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private val resolutionListener by lazy { DisplayResolutionChangeListener() }
    val gameModeGestureListener by lazy { GameModeGestureListener() }

    private var orientation = Configuration.ORIENTATION_PORTRAIT

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        Shared.service = this

        resolutionListener.registered = true
        GameModeListenerProxy.registered = true
    }

    override fun onDestroy() {
        GameModeListenerProxy.registered = false
        gameModeGestureListener.registered = false
        resolutionListener.registered = false

        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        newConfig.orientation.takeIf { orientation != it }?.let {
            orientation = it
            if (Shared.inGame) {
                GamePanelViewController.onConfigurationChanged()
            }
        }
    }
}
