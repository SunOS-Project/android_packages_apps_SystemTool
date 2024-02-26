/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.UserHandle

import org.nameless.systemtool.onlineconfig.OnlineConfigService
import org.nameless.systemtool.windowmode.WmGestureService
import org.nameless.view.PopUpViewManager

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (PopUpViewManager.FEATURE_SUPPORTED) {
            context.startServiceAsUser(
                Intent(context, WmGestureService::class.java),
                UserHandle.CURRENT
            )
        }

        context.startServiceAsUser(
            Intent(context, OnlineConfigService::class.java),
            UserHandle.CURRENT
        )
    }
}
