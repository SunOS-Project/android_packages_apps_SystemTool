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

import org.nameless.provider.SettingsExt.System.SYSTEM_TOOL_MINI_WINDOW_APPS
import org.nameless.provider.SettingsExt.System.SYSTEM_TOOL_WINDOWING_MODE_GESTURE
import org.nameless.systemtool.common.Utils
import org.nameless.systemtool.windowmode.ViewAnimator
import org.nameless.systemtool.windowmode.util.Config.CIRCLE_MAX_ICON
import org.nameless.systemtool.windowmode.util.Shared.clearCircleView
import org.nameless.systemtool.windowmode.util.Shared.leftCircle
import org.nameless.systemtool.windowmode.util.Shared.rightCircle
import org.nameless.systemtool.windowmode.util.Shared.service
import org.nameless.systemtool.windowmode.util.Shared.updateCircleViewGroup
import org.nameless.systemtool.windowmode.util.Shared.updateNavbarHeight
import org.nameless.systemtool.windowmode.view.IconView

class SettingsObserver(
    private val handler: Handler
) : ContentObserver(handler) {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
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
            } else {
                userSwitchReceiver.setListening(false)
                service.contentResolver.unregisterContentObserver(this)
            }
        }

    var gestureEnabled = true
    var userSetupCompleted = false

    private val userSwitchReceiver = object: UserSwitchReceiver(service) {
        override fun onUserSwitched() {
            updateAll()
        }
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        when (uri?.lastPathSegment) {
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
                    updateCircleViewGroup()
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
        ViewAnimator.hideCircle()
        clearCircleView()

        val appList = getMiniWindowAppsSettings(service)
            ?.takeIf { it.isNotBlank() }?.split(";") ?: emptyList()

        appList.forEachIndexed { i, v ->
            if (i >= CIRCLE_MAX_ICON - 1) {
                return@forEachIndexed
            }
            leftCircle.post { leftCircle.addView(IconView(service, v)) }
            rightCircle.post { rightCircle.addView(IconView(service, v)) }
        }
        leftCircle.post { leftCircle.addView(IconView(service, Utils.PACKAGE_NAME)) }
        rightCircle.post { rightCircle.addView(IconView(service, Utils.PACKAGE_NAME)) }
    }

    private fun updateAll() {
        updateUserSetupCompleted()
        updateGestureEnabled()
        updateMiniWindowApps()
        updateNavbarHeight()
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

        fun isGesturalMode(context: Context): Boolean {
            return Settings.Secure.getIntForUser(context.contentResolver,
                NAVIGATION_MODE, GESTURAL_MODE, UserHandle.USER_CURRENT
            ) == GESTURAL_MODE
        }
    }
}
