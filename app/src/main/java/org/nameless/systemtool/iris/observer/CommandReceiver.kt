/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler

import org.nameless.os.DebugConstants.DEBUG_SYSTEM_TOOL
import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.iris.util.Constants.INTENT_DEBUG_GET_COMMAND
import org.nameless.systemtool.iris.util.Constants.INTENT_DEBUG_SET_COMMAND
import org.nameless.systemtool.iris.util.Shared.service

/* Only used to debug command get/set status */
abstract class CommandReceiver(
    private val handler: Handler
) : BroadcastReceiver() {

    private var registered = false

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            INTENT_DEBUG_GET_COMMAND -> {
                val type = intent.getIntExtra("type", -1)
                if (type != -1) {
                    logD(TAG, "Received get type: $type, result: " + onGetCommand(type))
                }
            }
            INTENT_DEBUG_SET_COMMAND -> {
                val command = intent.getStringExtra("command")
                if (!command.isNullOrBlank()) {
                    logD(TAG, "Received set command: $command, result: " + onSetCommand(command))
                }
            }
        }
    }

    fun register() {
        if (registered) {
            return
        }
        if (!DEBUG_SYSTEM_TOOL) {
            return // Don't register receiver if debug is not enabled
        }
        registered = true
        service.registerReceiverForAllUsers(this, IntentFilter().apply {
            addAction(INTENT_DEBUG_GET_COMMAND)
            addAction(INTENT_DEBUG_SET_COMMAND)
        }, null, handler)
    }

    fun unregister() {
        if (!registered) {
            return
        }
        registered = false
        service.unregisterReceiver(this)
    }

    abstract fun onGetCommand(type: Int): Int
    abstract fun onSetCommand(command: String): Int

    companion object {
        private const val TAG = "SystemTool::Iris::CommandReceiver"
    }
}
