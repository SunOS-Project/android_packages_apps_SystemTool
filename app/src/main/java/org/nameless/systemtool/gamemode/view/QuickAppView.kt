/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.view

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.UserHandle
import android.provider.Settings
import android.util.AttributeSet
import android.widget.LinearLayout

import org.nameless.provider.SettingsExt.System.SYSTEM_TOOL_MINI_WINDOW_APPS
import org.nameless.systemtool.common.Utils.PACKAGE_NAME
import org.nameless.systemtool.gamemode.tile.AppTile
import org.nameless.systemtool.gamemode.util.Shared.service
import org.nameless.systemtool.windowmode.observer.SettingsObserver

class QuickAppView(
    context: Context,
    attrs: AttributeSet
): LinearLayout(context, attrs) {

    private val settingsObserver by lazy { MiniWindowSettingsObserver() }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        updateApps()
        settingsObserver.register()
    }

    override fun onDetachedFromWindow() {
        settingsObserver.unregister()
        super.onDetachedFromWindow()
    }

    private fun updateApps() {
        removeAllViews()

        (SettingsObserver.getMiniWindowAppsSettings(service)
            ?.takeIf { it.isNotBlank() }?.split(";") ?: emptyList()).forEach { app ->
                app.split(":").let {
                    if (it.size == 3) {
                        addView(AppTile(service, it[0], it[1], it[2].toInt()))
                    } else {
                        addView(AppTile(service, it[0]))
                    }
                }
        }
        addView(AppTile(service, PACKAGE_NAME))
    }

    private inner class MiniWindowSettingsObserver : ContentObserver(service.mainHandler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            if (SYSTEM_TOOL_MINI_WINDOW_APPS == uri?.lastPathSegment) {
                updateApps()
            }
        }

        fun register() {
            context.contentResolver.registerContentObserver(
                Settings.System.getUriFor(SYSTEM_TOOL_MINI_WINDOW_APPS),
                false, this, UserHandle.USER_CURRENT)
        }

        fun unregister() {
            context.contentResolver.unregisterContentObserver(this)
        }
    }
}
