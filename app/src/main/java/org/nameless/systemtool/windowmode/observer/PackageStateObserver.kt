/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.observer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_PACKAGE_ADDED
import android.content.Intent.ACTION_PACKAGE_CHANGED
import android.content.Intent.ACTION_PACKAGE_FULLY_REMOVED
import android.content.Intent.ACTION_PACKAGE_REMOVED
import android.content.Intent.EXTRA_REPLACING
import android.content.IntentFilter
import android.os.Handler

import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.windowmode.util.Shared.service

class PackageStateObserver(
    private val handler: Handler
) : BroadcastReceiver() {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                service.registerReceiverForAllUsers(this, IntentFilter().apply {
                    addAction(ACTION_PACKAGE_ADDED)
                    addAction(ACTION_PACKAGE_CHANGED)
                    addAction(ACTION_PACKAGE_FULLY_REMOVED)
                    addAction(ACTION_PACKAGE_REMOVED)
                    addDataScheme("package")
                }, null, handler)
            } else {
                service.unregisterReceiver(this)
            }
        }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PACKAGE_ADDED -> {
                if (!intent.getBooleanExtra(EXTRA_REPLACING, false)) {
                    onPackageAdded(intent.data?.schemeSpecificPart)
                }
            }
            ACTION_PACKAGE_CHANGED -> {
                onPackageStateChanged(intent.data?.schemeSpecificPart)
            }
            ACTION_PACKAGE_FULLY_REMOVED, ACTION_PACKAGE_REMOVED -> {
                if (!intent.getBooleanExtra(EXTRA_REPLACING, false)) {
                    onPackageRemoved(intent.data?.schemeSpecificPart)
                }
            }
        }
    }

    private fun onPackageAdded(packageName: String?) {
        packageName ?: return
        logD(TAG, "onPackageAdded, packageName=$packageName")
    }

    private fun onPackageStateChanged(packageName: String?) {
        packageName ?: return
        logD(TAG, "onPackageStateChanged, packageName=$packageName")
    }

    private fun onPackageRemoved(packageName: String?) {
        packageName ?: return
        logD(TAG, "onPackageRemoved, packageName=$packageName")
        SettingsObserver.putMiniWindowAppsSettings(service,
            (SettingsObserver.getMiniWindowAppsSettings(service)
                ?.takeIf { it.isNotBlank() }?.split(";")
                ?.filterNot { it == packageName }
                ?: emptyList()).joinToString(";"))
    }

    companion object {
        private const val TAG = "SystemTool::WindowMode::PackageStateObserver"
    }
}
