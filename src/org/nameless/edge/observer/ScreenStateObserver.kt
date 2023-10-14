/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.observer

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SCREEN_OFF
import android.content.Intent.ACTION_SCREEN_ON
import android.content.Intent.ACTION_USER_PRESENT
import android.content.IntentFilter
import android.os.Handler

import org.nameless.edge.util.ViewHolder

class ScreenStateReceiver(
    private val context: Context,
    private val handler: Handler
) : BroadcastReceiver() {

    private val keyguardManager = context.getSystemService(KeyguardManager::class.java)

    private var handledUnlock = false

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_SCREEN_OFF -> {
                handledUnlock = false
                ViewHolder.allowVisible = false
                ViewHolder.hideForAll()
            }
            ACTION_SCREEN_ON -> {
                if (!handledUnlock && !keyguardManager.isKeyguardLocked()) {
                    handledUnlock = true
                    ViewHolder.allowVisible = true
                }
            }
            ACTION_USER_PRESENT -> {
                handledUnlock = true
                ViewHolder.allowVisible = true
            }
        }
    }

    fun register() {
        context.registerReceiver(this, IntentFilter().apply {
            addAction(ACTION_SCREEN_OFF)
            addAction(ACTION_SCREEN_ON)
            addAction(ACTION_USER_PRESENT)
        }, null, handler)
    }

    fun unregister() {
        context.unregisterReceiver(this)
    }
}
