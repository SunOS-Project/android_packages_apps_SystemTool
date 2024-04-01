/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.UserHandle

import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.iris.IrisService
import org.nameless.systemtool.iris.util.FeatureHelper
import org.nameless.systemtool.onlineconfig.OnlineConfigService
import org.nameless.systemtool.windowmode.WmGestureService
import org.nameless.view.PopUpViewManager

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Windowing Mode
                if (PopUpViewManager.FEATURE_SUPPORTED) {
                    logD(TAG, "Start WmGestureService")
                    context.startServiceAsUser(
                        Intent(context, WmGestureService::class.java),
                        UserHandle.CURRENT
                    )
                }
            }
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
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
        }
    }

    companion object {
        private const val TAG = "SystemTool::BootCompletedReceiver"
    }
}
