/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.onlineconfig

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder

import org.nameless.systemtool.onlineconfig.observer.DebugModeReceiver
import org.nameless.systemtool.onlineconfig.observer.NetworkStateObserver
import org.nameless.systemtool.onlineconfig.observer.UpdateActionReceiver
import org.nameless.systemtool.onlineconfig.util.Constants.BOOT_COMPLETED_UPDATE_DELAY
import org.nameless.systemtool.onlineconfig.util.Shared

class OnlineConfigService : Service() {

    private val handlerThread by lazy {
        HandlerThread("SystemTool::OnlineConfigService").apply { start() }
    }
    private val handler by lazy { Handler(handlerThread.looper) }

    private val debugModeReceiver by lazy {
        DebugModeReceiver(handler)
    }
    private val networkStateObserver by lazy {
        NetworkStateObserver(handler)
    }
    private val updateActionReceiver by lazy {
        UpdateActionReceiver(handler)
    }
    private val updateScheduler by lazy {
        UpdateScheduler(handler)
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Shared.service = this
        Shared.scheduler = updateScheduler

        debugModeReceiver.register()
        networkStateObserver.register()
        updateActionReceiver.register()

        updateScheduler.setScheduler(BOOT_COMPLETED_UPDATE_DELAY)
    }

    override fun onDestroy() {
        super.onDestroy()
        updateScheduler.cancelScheduler()
        updateActionReceiver.unregister()
        networkStateObserver.unregister()
        debugModeReceiver.unregister()
    }
}
