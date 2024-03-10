/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SCREEN_OFF
import android.content.Intent.ACTION_SCREEN_ON
import android.content.Intent.ACTION_USER_PRESENT
import android.content.IntentFilter
import android.os.Handler

import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.iris.util.Shared.keyguardManager
import org.nameless.systemtool.iris.util.Shared.powerManager
import org.nameless.systemtool.iris.util.Shared.service

abstract class ScreenStateReceiver(
    private val handler: Handler
) : BroadcastReceiver() {

    private var registered = false

    private var handledUnlock = false

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SCREEN_OFF -> {
                logD(TAG, "Screen off")
                handledUnlock = false
                onScreenStateChanged(false)
            }
            ACTION_SCREEN_ON -> {
                logD(TAG, "Screen on")
                if (!handledUnlock && !keyguardManager.isKeyguardLocked) {
                    handledUnlock = true
                    onScreenUnlocked()
                } else {
                    onScreenStateChanged(true)
                }
            }
            ACTION_USER_PRESENT -> {
                logD(TAG, "Screen unlocked")
                handledUnlock = true
                onScreenUnlocked()
            }
        }
    }

    fun register() {
        if (registered) {
            return
        }
        registered = true
        service.registerReceiverForAllUsers(this, IntentFilter().apply {
            addAction(ACTION_SCREEN_OFF)
            addAction(ACTION_SCREEN_ON)
            addAction(ACTION_USER_PRESENT)
        }, null, handler)
        powerManager.isInteractive.let {
            logD(TAG, "Screen " + (if (it) "on" else "off"))
            onScreenStateChanged(it)
        }
    }

    fun unregister() {
        if (!registered) {
            return
        }
        registered = false
        service.unregisterReceiver(this)
    }

    abstract fun onScreenStateChanged(isScreenOn: Boolean)
    abstract fun onScreenUnlocked()

    companion object {
        private const val TAG = "SystemTool::Iris::ScreenStateReceiver"
    }
}
