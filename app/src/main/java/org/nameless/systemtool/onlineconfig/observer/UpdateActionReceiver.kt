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

import org.nameless.content.OnlineConfigManager.ACTION_UPDATE_CONFIG
import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.onlineconfig.OnlineConfigUpdater
import org.nameless.systemtool.onlineconfig.util.Shared.scheduler
import org.nameless.systemtool.onlineconfig.util.Shared.service
import org.nameless.systemtool.onlineconfig.util.Shared.updatePendingWifi
import org.nameless.systemtool.onlineconfig.util.Shared.wifiAvailable

class UpdateActionReceiver(
    private val handler: Handler
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_UPDATE_CONFIG -> {
                logD(TAG, "Force update action received!")
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
        service.registerReceiverForAllUsers(this, IntentFilter(ACTION_UPDATE_CONFIG), null, handler)
    }

    fun unregister() {
        service.unregisterReceiver(this)
    }

    companion object {
        private const val TAG = "SystemTool::UpdateActionReceiver"
    }
}
