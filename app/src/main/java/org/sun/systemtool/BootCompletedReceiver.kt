/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.UserHandle

import org.sun.systemtool.common.Utils.logD
import org.sun.systemtool.gamemode.GameAssistantService
import org.sun.systemtool.iris.IrisService
import org.sun.systemtool.iris.util.FeatureHelper
import org.sun.systemtool.onlineconfig.OnlineConfigService
import org.sun.systemtool.windowmode.WmGestureService
import org.sun.view.PopUpViewManager

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        // Game Mode
        logD(TAG, "Start GameAssistantService")
        context.startServiceAsUser(
            Intent(context, GameAssistantService::class.java),
            UserHandle.CURRENT
        )

        // Windowing Mode
        if (PopUpViewManager.FEATURE_SUPPORTED) {
            logD(TAG, "Start WmGestureService")
            context.startServiceAsUser(
                Intent(context, WmGestureService::class.java),
                UserHandle.CURRENT
            )
        }

        // Pixelworks Iris
        if (FeatureHelper.irisSupported) {
            logD(TAG, "Start IrisService")
            context.startServiceAsUser(
                Intent(context, IrisService::class.java),
                UserHandle.CURRENT
            )
        }

        // Online Config
        logD(TAG, "Start OnlineConfigService")
        context.startServiceAsUser(
            Intent(context, OnlineConfigService::class.java),
            UserHandle.CURRENT
        )
    }

    companion object {
        private const val TAG = "SystemTool::BootCompletedReceiver"
    }
}
