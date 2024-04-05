/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.util

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.UserHandle

import org.nameless.systemtool.windowmode.view.CircleIconView

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

    fun startShortcut(context: Context, shortcutInfo: ShortcutInfo) {
        BroadcastSender.sendStartShortcutBroadcast(
            context,
            shortcutInfo.`package`,
            shortcutInfo.id,
            shortcutInfo.userId
        )
    }

    fun startShortcut(context: Context, iconView: CircleIconView) {
        BroadcastSender.sendStartShortcutBroadcast(
            context,
            iconView.packageName,
            iconView.shortcutId,
            iconView.shortcutUserId
        )
    }
}
