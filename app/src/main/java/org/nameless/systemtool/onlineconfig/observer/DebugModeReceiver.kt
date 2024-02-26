/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.onlineconfig.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler

import org.nameless.os.DebugConstants.DEBUG_SYSTEM_TOOL
import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.onlineconfig.OnlineConfigUpdater
import org.nameless.systemtool.onlineconfig.util.Constants.EXTRA_DEBUG_MODE_STATE
import org.nameless.systemtool.onlineconfig.util.Constants.INTENT_DEBUG_MODE
import org.nameless.systemtool.onlineconfig.util.Shared.debugMode
import org.nameless.systemtool.onlineconfig.util.Shared.scheduler
import org.nameless.systemtool.onlineconfig.util.Shared.service
import org.nameless.systemtool.onlineconfig.util.Shared.updatePendingWifi
import org.nameless.systemtool.onlineconfig.util.Shared.wifiAvailable

class DebugModeReceiver(
    private val handler: Handler
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            INTENT_DEBUG_MODE -> {
                val state = intent.getBooleanExtra(EXTRA_DEBUG_MODE_STATE, false)
                debugMode = state
                logD(TAG, "debug mode ${if (debugMode) "on" else "off"}!")
                scheduler.cancelScheduler()
                if (wifiAvailable) {
                    OnlineConfigUpdater.update()
                } else {
                    updatePendingWifi = true
                }
            }
        }
    }

    fun register() {
        if (!DEBUG_SYSTEM_TOOL) {
            return
        }
        service.registerReceiverForAllUsers(this, IntentFilter(INTENT_DEBUG_MODE), null, handler)
    }

    fun unregister() {
        if (!DEBUG_SYSTEM_TOOL) {
            return
        }
        service.unregisterReceiver(this)
    }

    companion object {
        private const val TAG = "SystemTool::DebugModeReceiver"
    }
}
