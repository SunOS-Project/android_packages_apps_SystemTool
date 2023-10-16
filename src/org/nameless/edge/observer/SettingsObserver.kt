/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.observer

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.UserHandle
import android.provider.Settings
import android.provider.Settings.Secure.NAVIGATION_MODE
import android.provider.Settings.Secure.USER_SETUP_COMPLETE
import android.provider.Settings.System.DISPLAY_RESOLUTION_WIDTH
import android.provider.Settings.System.EDGE_TOOL_GESTURE_ENABLED
import android.provider.Settings.System.EDGE_TOOL_MINI_WINDOW_APPS

import com.android.internal.util.nameless.UserSwitchReceiver

import kotlin.math.min

import org.nameless.edge.EdgeService
import org.nameless.edge.PickerDataCache
import org.nameless.edge.util.Constants
import org.nameless.edge.util.IconLayoutAlgorithm
import org.nameless.edge.util.PackageInfoCache
import org.nameless.edge.util.ViewHolder

class SettingsObserver(
    private val service: EdgeService,
    private val handler: Handler
) : ContentObserver(handler) {

    private var gestureEnabled = true
    private var userSetuped = false

    private val userSwitchReceiver = object: UserSwitchReceiver(service) {
        override fun onUserSwitched() {
            PackageInfoCache.initPackageList(service)
            updateAll()
        }
    }

    override fun onChange(selfChange: Boolean, uri: Uri) {
        when (uri.lastPathSegment) {
            USER_SETUP_COMPLETE -> {
                updateUserSetuped()
            }
            EDGE_TOOL_GESTURE_ENABLED -> {
                updateGestureEnabled()
            }
            EDGE_TOOL_MINI_WINDOW_APPS -> {
                updateMiniWindowApps()
            }
            DISPLAY_RESOLUTION_WIDTH, NAVIGATION_MODE -> {
                handler.postDelayed({
                    updateNavbarHeight()
                    ViewHolder.relocateIconView(service)
                    service.updateGestureTouchRegion()
                }, 1000L)
            }
        }
    }

    private fun updateUserSetuped() {
        userSetuped = Settings.Secure.getIntForUser(
            service.contentResolver, USER_SETUP_COMPLETE,
            0, UserHandle.USER_CURRENT) == 1
    }

    private fun updateGestureEnabled() {
        gestureEnabled = Settings.System.getIntForUser(
            service.contentResolver, EDGE_TOOL_GESTURE_ENABLED,
            1, UserHandle.USER_CURRENT) == 1
    }

    private fun updateMiniWindowApps() {
        ViewHolder.safelyClearIconViews(service)

        val validAppList = getMiniWindowAppsSettings(service)
            ?.takeIf { it.isNotBlank() }?.split(";")?.filter {
                PackageInfoCache.isPackageAvailable(it) }?: emptyList()

        PickerDataCache.updatePinnedPackages(validAppList.toMutableSet())

        val total = min(validAppList.size + 1, Constants.circleMaxIcon)
        validAppList.forEachIndexed { i, v ->
            if (i >= Constants.circleMaxIcon - 1) {
                return@forEachIndexed
            }
            ViewHolder.addIconView(service, v, i + 1, total)
        }
        ViewHolder.addIconView(service, Constants.PACKAGE_NAME, total, total)
    }

    private fun updateNavbarHeight() {
        IconLayoutAlgorithm.gesturalMode = isGesturalMode(service)
        IconLayoutAlgorithm.updateNarbarHeight(service)
    }

    private fun updateAll() {
        updateUserSetuped()
        updateGestureEnabled()
        updateMiniWindowApps()
        updateNavbarHeight()
    }

    fun isUserSetuped() = userSetuped

    fun isGestureEnabled() = gestureEnabled

    fun register() {
        service.contentResolver.run {
            registerContentObserver(
                Settings.Secure.getUriFor(USER_SETUP_COMPLETE),
                false, this@SettingsObserver, UserHandle.USER_ALL)
            registerContentObserver(
                Settings.System.getUriFor(EDGE_TOOL_GESTURE_ENABLED),
                false, this@SettingsObserver, UserHandle.USER_ALL)
            registerContentObserver(
                Settings.System.getUriFor(EDGE_TOOL_MINI_WINDOW_APPS),
                false, this@SettingsObserver, UserHandle.USER_ALL)
            registerContentObserver(
                Settings.System.getUriFor(DISPLAY_RESOLUTION_WIDTH),
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
        private val GESTURAL_MODE = 2

        fun getMiniWindowAppsSettings(context: Context): String? {
            return Settings.System.getStringForUser(context.contentResolver,
                EDGE_TOOL_MINI_WINDOW_APPS, UserHandle.USER_CURRENT)
        }

        fun putMiniWindowAppsSettings(context: Context, apps: String) {
            Settings.System.putStringForUser(context.contentResolver,
                EDGE_TOOL_MINI_WINDOW_APPS, apps, UserHandle.USER_CURRENT)
        }

        fun getMiniWindowAppsSet(context: Context): MutableSet<String> {
            return mutableSetOf<String>().apply {
                (getMiniWindowAppsSettings(context)
                    ?.takeIf { it.isNotBlank() }?.split(";")?.filter {
                        PackageInfoCache.isPackageAvailable(it)
                    }?: emptyList()).forEach { app -> add(app) }
            }
        }

        private fun isGesturalMode(context: Context): Boolean {
            return Settings.Secure.getIntForUser(context.contentResolver,
                NAVIGATION_MODE, GESTURAL_MODE, UserHandle.USER_CURRENT
            ) == GESTURAL_MODE
        }
    }
}
