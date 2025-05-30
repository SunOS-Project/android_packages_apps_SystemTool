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

import org.sun.content.OnlineConfigManager.ACTION_UPDATE_CONFIG
import org.sun.systemtool.common.Utils.logD
import org.sun.systemtool.onlineconfig.OnlineConfigUpdater
import org.sun.systemtool.onlineconfig.util.Shared.service
import org.sun.systemtool.onlineconfig.util.Shared.updatePendingWifi
import org.sun.systemtool.onlineconfig.util.Shared.updateScheduler
import org.sun.systemtool.onlineconfig.util.Shared.wifiAvailable

class UpdateActionReceiver(
    private val handler: Handler
) : BroadcastReceiver() {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                service.registerReceiverForAllUsers(
                    this, IntentFilter(ACTION_UPDATE_CONFIG), null, handler)
            } else {
                service.unregisterReceiver(this)
            }
        }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_UPDATE_CONFIG -> {
                logD(TAG, "Force update action received!")
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
        private const val TAG = "SystemTool::UpdateActionReceiver"
    }
}
