/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler

import org.nameless.edge.util.ViewHolder
import org.nameless.wm.PopUpBroadcastConstants.ACTION_START_EDGE_TOOL
import org.nameless.wm.PopUpBroadcastConstants.EXTRA_IS_LEFT

class StartBroadcastReceiver(
    private val context: Context,
    private val handler: Handler
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_START_EDGE_TOOL -> {
                val isLeft = intent.getBooleanExtra(EXTRA_IS_LEFT, false)
                ViewHolder.showForAll(isLeft)
            }
        }
    }

    fun register() {
        context.registerReceiver(this, IntentFilter().apply {
            addAction(ACTION_START_EDGE_TOOL)
        }, null, handler)
    }

    fun unregister() {
        context.unregisterReceiver(this)
    }
}
