/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_PACKAGE_FULLY_REMOVED
import android.content.Intent.ACTION_PACKAGE_REMOVED
import android.content.IntentFilter
import android.os.Handler

import org.nameless.wm.PopUpDebugHelper.logD

class PackageStateObserver(
    private val context: Context,
    private val handler: Handler
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ACTION_PACKAGE_FULLY_REMOVED, ACTION_PACKAGE_REMOVED -> {
                onPackageRemoved(intent.data?.schemeSpecificPart)
            }
        }
    }

    fun register() {
        context.registerReceiver(this, IntentFilter().apply {
            addAction(ACTION_PACKAGE_FULLY_REMOVED)
            addAction(ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }, null, handler)
    }

    fun unregister() {
        context.unregisterReceiver(this)
    }

    private fun onPackageRemoved(packageName: String?) {
        logD(TAG, "onPackageRemoved, packageName=$packageName")
        SettingsObserver.putMiniWindowAppsSettings(context,
            (SettingsObserver.getMiniWindowAppsSettings(context)
                ?.takeIf { it.isNotBlank() }?.split(";")
                ?.filterNot { it.equals(packageName) }
                ?: emptyList()).joinToString(";"))
    }

    companion object {
        private val TAG = "EdgeTool::PackageStateObserver";
    }
}
