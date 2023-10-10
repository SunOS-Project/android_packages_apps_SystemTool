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
import android.provider.Settings.System.EDGE_TOOL_MINI_WINDOW_APPS

import com.android.internal.util.nameless.CustomUtils
import com.android.internal.util.nameless.UserSwitchReceiver

import org.nameless.edge.util.Constants
import org.nameless.edge.util.ViewHolder

class SettingsObserver(
    private val context: Context,
    handler: Handler
) : ContentObserver(handler) {

    private val userSwitchReceiver = object: UserSwitchReceiver(context) {
        override fun onUserSwitched() {
            updateAll()
        }
    }

    override fun onChange(selfChange: Boolean, uri: Uri) {
        when (uri.lastPathSegment) {
            EDGE_TOOL_MINI_WINDOW_APPS -> {
                updateMiniWindowApps()
            }
        }
    }

    private fun updateMiniWindowApps() {
        ViewHolder.safelyClearIconViews(context)

        val validAppList = getMiniWindowAppsSettings(context)
            ?.takeIf { it.isNotBlank() }?.split(";")?.filter {
                CustomUtils.isPackageInstalled(context, it, false)
            }?.filterIndexed { i, _ -> i < Constants.circleMaxIcon }?: emptyList()
        validAppList.forEachIndexed {
            i, v -> ViewHolder.addIconView(context, v, i + 1, validAppList.size)
        }
    }

    private fun updateAll() {
        updateMiniWindowApps()
    }

    fun register() {
        context.contentResolver.run {
            registerContentObserver(
                Settings.System.getUriFor(EDGE_TOOL_MINI_WINDOW_APPS),
                false, this@SettingsObserver, UserHandle.USER_ALL)
        }
        userSwitchReceiver.setListening(true)
        updateAll()
    }

    fun unregister() {
        userSwitchReceiver.setListening(false)
        context.contentResolver.unregisterContentObserver(this)
    }

    companion object {
        fun getMiniWindowAppsSettings(context: Context): String? {
            return Settings.System.getStringForUser(context.contentResolver,
                EDGE_TOOL_MINI_WINDOW_APPS, UserHandle.USER_CURRENT)
        }

        fun putMiniWindowAppsSettings(context: Context, apps: String) {
            Settings.System.putStringForUser(context.contentResolver,
                EDGE_TOOL_MINI_WINDOW_APPS, apps, UserHandle.USER_CURRENT)
        }
    }
}
