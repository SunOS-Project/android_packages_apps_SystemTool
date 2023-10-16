/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_CLOSE_SYSTEM_DIALOGS
import android.content.IntentFilter
import android.os.Handler

import org.nameless.edge.util.ViewHolder

class SystemStateReceiver(
    private val context: Context,
    private val handler: Handler
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_CLOSE_SYSTEM_DIALOGS -> {
                ViewHolder.hideForAll()
            }
        }
    }

    fun register() {
        context.registerReceiverForAllUsers(this, IntentFilter().apply {
            addAction(ACTION_CLOSE_SYSTEM_DIALOGS)
        }, null, handler)
    }

    fun unregister() {
        context.unregisterReceiver(this)
    }
}
