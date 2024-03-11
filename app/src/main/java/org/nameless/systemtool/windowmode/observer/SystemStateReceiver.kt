/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_CLOSE_SYSTEM_DIALOGS
import android.content.IntentFilter
import android.os.Handler

import org.nameless.systemtool.windowmode.ViewHolder
import org.nameless.systemtool.windowmode.util.Shared.service

@Suppress("DEPRECATION")
class SystemStateReceiver(
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
                }, null, handler)
            } else {
                service.unregisterReceiver(this)
            }
        }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_CLOSE_SYSTEM_DIALOGS -> {
                ViewHolder.hideForAll()
            }
        }
    }
}
