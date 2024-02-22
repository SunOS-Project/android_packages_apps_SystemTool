/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.UserHandle

import org.nameless.systemtool.windowmode.WmGestureService

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        context.startServiceAsUser(Intent(context, WmGestureService::class.java), UserHandle.CURRENT)
    }
}
