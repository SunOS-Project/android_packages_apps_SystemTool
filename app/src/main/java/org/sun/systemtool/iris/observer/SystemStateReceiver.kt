/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.iris.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_CLOSE_SYSTEM_DIALOGS
import android.content.IntentFilter
import android.os.Handler
import android.os.PowerManager.ACTION_POWER_SAVE_MODE_CHANGED

import org.sun.systemtool.common.Utils.logD
import org.sun.systemtool.iris.util.Shared.powerManager
import org.sun.systemtool.iris.util.Shared.powerSaveMode
import org.sun.systemtool.iris.util.Shared.service

@Suppress("DEPRECATION")
abstract class SystemStateReceiver(
    private val handler: Handler
) : BroadcastReceiver() {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                service.registerReceiverForAllUsers(this, IntentFilter().apply {
                    addAction(ACTION_CLOSE_SYSTEM_DIALOGS)
                    addAction(ACTION_POWER_SAVE_MODE_CHANGED)
                }, null, handler)
                powerManager.isPowerSaveMode.let {
                    powerSaveMode = it
                    logD(TAG, "Power save mode changed to $it")
                    onPowerSaveModeChanged()
                }
            } else {
                service.unregisterReceiver(this)
            }
        }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_CLOSE_SYSTEM_DIALOGS -> {
                val reason = intent.getStringExtra("reason") ?: return
                logD(TAG, "Close system dialogs, reason: $reason")
                onCloseSystemDialog(reason)
            }
            ACTION_POWER_SAVE_MODE_CHANGED -> {
                powerManager.isPowerSaveMode.let {
                    powerSaveMode = it
                    logD(TAG, "Power save mode changed to $it")
                    onPowerSaveModeChanged()
                }
            }
        }
    }

    abstract fun onCloseSystemDialog(reason: String)
    abstract fun onPowerSaveModeChanged()

    companion object {
        private const val TAG = "SystemTool::Iris::SystemStateReceiver"
    }
}
