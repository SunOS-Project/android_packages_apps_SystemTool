/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.onlineconfig.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler

import org.sun.os.DebugConstants.DEBUG_SYSTEM_TOOL
import org.sun.systemtool.common.Utils.logD
import org.sun.systemtool.onlineconfig.OnlineConfigUpdater
import org.sun.systemtool.onlineconfig.util.Constants.EXTRA_DEBUG_MODE_STATE
import org.sun.systemtool.onlineconfig.util.Constants.INTENT_DEBUG_MODE
import org.sun.systemtool.onlineconfig.util.Shared.debugMode
import org.sun.systemtool.onlineconfig.util.Shared.service
import org.sun.systemtool.onlineconfig.util.Shared.updatePendingWifi
import org.sun.systemtool.onlineconfig.util.Shared.updateScheduler
import org.sun.systemtool.onlineconfig.util.Shared.wifiAvailable

class DebugModeReceiver(
    private val handler: Handler
) : BroadcastReceiver() {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            if (!DEBUG_SYSTEM_TOOL) {
                return
            }
            field = value
            if (value) {
                service.registerReceiverForAllUsers(
                    this, IntentFilter(INTENT_DEBUG_MODE), null, handler)
            } else {
                service.unregisterReceiver(this)
            }
        }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            INTENT_DEBUG_MODE -> {
                val state = intent.getBooleanExtra(EXTRA_DEBUG_MODE_STATE, false)
                debugMode = state
                logD(TAG, "debug mode ${if (debugMode) "on" else "off"}!")
                updateScheduler.scheduler = -1
                if (wifiAvailable) {
                    OnlineConfigUpdater.update()
                } else {
                    updatePendingWifi = true
                }
            }
        }
    }

    companion object {
        private const val TAG = "SystemTool::DebugModeReceiver"
    }
}
