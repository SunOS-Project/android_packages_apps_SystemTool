/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.observer

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

import org.nameless.systemtool.util.PackageInfoCache
import org.nameless.wm.PopUpDebugHelper.logD

class PackageStateObserver(
    private val context: Context,
    private val handler: Handler
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
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

    fun register() {
        context.registerReceiverForAllUsers(this, IntentFilter().apply {
            addAction(ACTION_PACKAGE_ADDED)
            addAction(ACTION_PACKAGE_CHANGED)
            addAction(ACTION_PACKAGE_FULLY_REMOVED)
            addAction(ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }, null, handler)
    }

    fun unregister() {
        context.unregisterReceiver(this)
    }

    private fun onPackageAdded(packageName: String?) {
        packageName ?: return
        logD(TAG, "onPackageAdded, packageName=$packageName")
        PackageInfoCache.onPackageStateChanged(context, packageName, true)
    }

    private fun onPackageStateChanged(packageName: String?) {
        packageName ?: return
        logD(TAG, "onPackageStateChanged, packageName=$packageName")
        PackageInfoCache.onPackageStateChanged(context, packageName)
    }

    private fun onPackageRemoved(packageName: String?) {
        packageName ?: return
        logD(TAG, "onPackageRemoved, packageName=$packageName")
        PackageInfoCache.onPackageStateChanged(context, packageName)
        SettingsObserver.putMiniWindowAppsSettings(context,
            (SettingsObserver.getMiniWindowAppsSettings(context)
                ?.takeIf { it.isNotBlank() }?.split(";")
                ?.filterNot { it.equals(packageName) }
                ?: emptyList()).joinToString(";"))
    }

    companion object {
        private const val TAG = "SystemTool::PackageStateObserver"
    }
}
