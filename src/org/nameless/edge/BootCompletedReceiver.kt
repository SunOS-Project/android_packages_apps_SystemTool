/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.UserHandle

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        context.startServiceAsUser(Intent(context, EdgeService::class.java), UserHandle.SYSTEM)
    }
}
