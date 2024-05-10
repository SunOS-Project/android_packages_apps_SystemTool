/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.observer

import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Handler
import android.os.UserHandle
import android.os.UserManager.USER_TYPE_PROFILE_CLONE

import org.nameless.systemtool.common.IconDrawableHelper
import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.windowmode.util.Shared.launcherApps
import org.nameless.systemtool.windowmode.util.Shared.service
import org.nameless.systemtool.windowmode.util.Shared.userManager

class PackageStateObserver(
    private val handler: Handler
) : LauncherApps.Callback() {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                launcherApps.registerCallback(this, handler)
            } else {
                launcherApps.unregisterCallback(this)
            }
        }

    override fun onPackageAdded(packageName: String?, user: UserHandle?) {
        packageName ?: return
        user ?: return
        if (isClonedUser(user)) return

        logD(TAG, "onPackageAdded, packageName=$packageName")
    }

    override fun onPackageChanged(packageName: String?, user: UserHandle?) {
        packageName ?: return
        user ?: return
        if (isClonedUser(user)) return

        logD(TAG, "onPackageChanged, packageName=$packageName")
    }

    override fun onPackageRemoved(packageName: String?, user: UserHandle?) {
        packageName ?: return
        user ?: return
        if (isClonedUser(user)) return

        logD(TAG, "onPackageRemoved, packageName=$packageName")
        IconDrawableHelper.invalidatePackageCache(packageName)
        val oldSettings = SettingsObserver.getMiniWindowAppsSettings(service)
        (oldSettings?.takeIf { it.isNotBlank() }?.split(";")
                ?.filterNot { it.split(":")[0] == packageName }
                ?: emptyList()).joinToString(";").let { newSettings ->
            if (oldSettings != newSettings) {
                SettingsObserver.putMiniWindowAppsSettings(service, newSettings)
            }
        }
    }

    override fun onShortcutsChanged(
        packageName: String,
        shortcuts: MutableList<ShortcutInfo>,
        user: UserHandle
    ) {
        logD(TAG, "onShortcutsChanged, packageName=$packageName")
        IconDrawableHelper.invalidatePackageCache(packageName)
        val oldSettings = SettingsObserver.getMiniWindowAppsSettings(service)
        (oldSettings?.takeIf { it.isNotBlank() }?.split(";")
                ?.filter { it.split(":").let { splited ->
                    packageName != splited[0] || splited.size != 3 || shortcuts.find { info ->
                        info.id == splited[1] && info.userId == splited[2].toInt()
                    } != null
                }} ?: emptyList()).joinToString(";").let { newSettings ->
            if (oldSettings != newSettings) {
                SettingsObserver.putMiniWindowAppsSettings(service, newSettings)
            }
        }
    }

    override fun onPackagesAvailable(
        packageNames: Array<out String>?,
        user: UserHandle?,
        replacing: Boolean
    ) {}

    override fun onPackagesUnavailable(
        packageNames: Array<out String>?,
        user: UserHandle?,
        replacing: Boolean
    ) {}

    companion object {
        private const val TAG = "SystemTool::WindowMode::PackageStateObserver"

        private fun isClonedUser(user: UserHandle): Boolean {
            return userManager.getUserInfo(user.identifier)?.let {
                USER_TYPE_PROFILE_CLONE == it.userType
            } ?: false
        }
    }
}
