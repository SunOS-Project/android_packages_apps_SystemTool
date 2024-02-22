/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.observer

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.UserHandle
import android.provider.Settings
import android.provider.Settings.Secure.NAVIGATION_MODE
import android.provider.Settings.Secure.USER_SETUP_COMPLETE

import com.android.internal.util.nameless.UserSwitchReceiver

import kotlin.math.min

import org.nameless.provider.SettingsExt.System.SYSTEM_TOOL_MINI_WINDOW_APPS
import org.nameless.provider.SettingsExt.System.SYSTEM_TOOL_WINDOWING_MODE_GESTURE
import org.nameless.systemtool.common.Utils
import org.nameless.systemtool.windowmode.PickerDataCache
import org.nameless.systemtool.windowmode.util.Config
import org.nameless.systemtool.windowmode.util.Shared.service
import org.nameless.systemtool.windowmode.util.IconLayoutAlgorithm
import org.nameless.systemtool.windowmode.util.PackageInfoCache
import org.nameless.systemtool.windowmode.ViewHolder

class SettingsObserver(
    private val handler: Handler
) : ContentObserver(handler) {

    private var gestureEnabled = true
    private var userSetupCompleted = false

    private val userSwitchReceiver = object: UserSwitchReceiver(service) {
        override fun onUserSwitched() {
            PackageInfoCache.initPackageList()
            updateAll()
        }
    }

    override fun onChange(selfChange: Boolean, uri: Uri) {
        when (uri.lastPathSegment) {
            USER_SETUP_COMPLETE -> {
                updateUserSetupCompleted()
            }
            SYSTEM_TOOL_WINDOWING_MODE_GESTURE -> {
                updateGestureEnabled()
            }
            SYSTEM_TOOL_MINI_WINDOW_APPS -> {
                updateMiniWindowApps()
            }
            NAVIGATION_MODE -> {
                handler.postDelayed({
                    updateNavbarHeight()
                    ViewHolder.relocateIconView()
                }, 500L)
            }
        }
    }

    private fun updateUserSetupCompleted() {
        userSetupCompleted = Settings.Secure.getIntForUser(
            service.contentResolver, USER_SETUP_COMPLETE,
            0, UserHandle.USER_CURRENT) == 1
    }

    private fun updateGestureEnabled() {
        gestureEnabled = Settings.System.getIntForUser(
            service.contentResolver, SYSTEM_TOOL_WINDOWING_MODE_GESTURE,
            1, UserHandle.USER_CURRENT) == 1
    }

    private fun updateMiniWindowApps() {
        ViewHolder.safelyClearIconViews()

        val validAppList = getMiniWindowAppsSettings(service)
            ?.takeIf { it.isNotBlank() }?.split(";")?.filter {
                PackageInfoCache.isPackageAvailable(it) }?: emptyList()

        PickerDataCache.updatePinnedPackages(validAppList.toMutableSet())

        val total = min(validAppList.size + 1, Config.circleMaxIcon)
        validAppList.forEachIndexed { i, v ->
            if (i >= Config.circleMaxIcon - 1) {
                return@forEachIndexed
            }
            ViewHolder.addIconView(v, i + 1, total)
        }
        ViewHolder.addIconView(Utils.PACKAGE_NAME, total, total)
    }

    private fun updateNavbarHeight() {
        IconLayoutAlgorithm.gesturalMode = isGesturalMode(service)
        IconLayoutAlgorithm.updateNavbarHeight()
    }

    private fun updateAll() {
        updateUserSetupCompleted()
        updateGestureEnabled()
        updateMiniWindowApps()
        updateNavbarHeight()
    }

    fun isUserSetupCompleted() = userSetupCompleted

    fun isGestureEnabled() = gestureEnabled

    fun register() {
        service.contentResolver.run {
            registerContentObserver(
                Settings.Secure.getUriFor(USER_SETUP_COMPLETE),
                false, this@SettingsObserver, UserHandle.USER_ALL)
            registerContentObserver(
                Settings.System.getUriFor(SYSTEM_TOOL_WINDOWING_MODE_GESTURE),
                false, this@SettingsObserver, UserHandle.USER_ALL)
            registerContentObserver(
                Settings.System.getUriFor(SYSTEM_TOOL_MINI_WINDOW_APPS),
                false, this@SettingsObserver, UserHandle.USER_ALL)
            registerContentObserver(
                Settings.Secure.getUriFor(NAVIGATION_MODE),
                false, this@SettingsObserver, UserHandle.USER_ALL)
        }
        userSwitchReceiver.setListening(true)
        updateAll()
    }

    fun unregister() {
        userSwitchReceiver.setListening(false)
        service.contentResolver.unregisterContentObserver(this)
    }

    companion object {
        private const val GESTURAL_MODE = 2

        fun getMiniWindowAppsSettings(context: Context): String? {
            return Settings.System.getStringForUser(context.contentResolver,
                SYSTEM_TOOL_MINI_WINDOW_APPS, UserHandle.USER_CURRENT)
        }

        fun putMiniWindowAppsSettings(context: Context, apps: String) {
            Settings.System.putStringForUser(context.contentResolver,
            SYSTEM_TOOL_MINI_WINDOW_APPS, apps, UserHandle.USER_CURRENT)
        }

        private fun isGesturalMode(context: Context): Boolean {
            return Settings.Secure.getIntForUser(context.contentResolver,
                NAVIGATION_MODE, GESTURAL_MODE, UserHandle.USER_CURRENT
            ) == GESTURAL_MODE
        }
    }
}
