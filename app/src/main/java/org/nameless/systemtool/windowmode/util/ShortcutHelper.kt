/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.util

import android.app.ActivityOptions
import android.app.WindowConfiguration.WINDOWING_MODE_MINI_WINDOW_EXT
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.UserHandle

import org.nameless.systemtool.windowmode.view.CircleIconView
import org.nameless.view.AppFocusManager

object ShortcutHelper {

    fun getShortcuts(launcherApps: LauncherApps, packageName: String): List<ShortcutInfo>? {
        return try {
            launcherApps.getShortcuts(LauncherApps.ShortcutQuery().apply {
                setPackage(packageName)
                setQueryFlags(
                    LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED_BY_ANY_LAUNCHER
                )
            }, UserHandle.getUserHandleForUid(android.os.Process.myUid()))
        } catch (_: IllegalStateException) {
            null
        }
    }

    fun startShortcut(context: Context, launcherApps: LauncherApps, shortcutInfo: ShortcutInfo) {
        context.getSystemService(AppFocusManager::class.java)?.let {
            if (it.topFullscreenAppInfo?.packageName == shortcutInfo.`package`) {
                return
            }
        }
        try {
            launcherApps.startShortcut(
                shortcutInfo,
                null,
                ActivityOptions.makeBasic().apply {
                    setLaunchWindowingMode(WINDOWING_MODE_MINI_WINDOW_EXT)
                }.toBundle()
            )
        } catch (_: ActivityNotFoundException) {
        } catch (_: IllegalStateException) {
        }
    }

    fun startShortcut(context: Context, launcherApps: LauncherApps, iconView: CircleIconView) {
        context.getSystemService(AppFocusManager::class.java)?.let {
            if (it.topFullscreenAppInfo?.packageName == iconView.packageName) {
                return
            }
        }
        try {
            launcherApps.startShortcut(
                iconView.packageName,
                iconView.shortcutId,
                null,
                ActivityOptions.makeBasic().apply {
                    setLaunchWindowingMode(WINDOWING_MODE_MINI_WINDOW_EXT)
                }.toBundle(),
                UserHandle(iconView.shortcutUserId)
            )
        } catch (_: ActivityNotFoundException) {
        } catch (_: IllegalStateException) {
        }
    }
}
