/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.common

import android.content.Context
import android.content.Intent
import android.os.UserHandle

import org.sun.systemtool.common.Utils.PACKAGE_NAME
import org.sun.systemtool.windowmode.AllAppsPickerActivity
import org.sun.view.PopUpViewManager.ACTION_START_MINI_WINDOW
import org.sun.view.PopUpViewManager.EXTRA_ACTIVITY_NAME
import org.sun.view.PopUpViewManager.EXTRA_PACKAGE_NAME
import org.sun.view.PopUpViewManager.EXTRA_SHORTCUT_ID
import org.sun.view.PopUpViewManager.EXTRA_SHORTCUT_USER_ID

object BroadcastSender {

    fun sendStartPackageBroadcast(
        context: Context,
        packageName: String
    ) {
        if (PACKAGE_NAME == packageName) {
            context.sendBroadcastAsUser(Intent().apply {
                action = ACTION_START_MINI_WINDOW
                putExtra(EXTRA_PACKAGE_NAME, PACKAGE_NAME)
                putExtra(EXTRA_ACTIVITY_NAME, AllAppsPickerActivity::class.java.name)
            }, UserHandle.SYSTEM)
            return
        }

        context.sendBroadcastAsUser(Intent().apply {
            action = ACTION_START_MINI_WINDOW
            putExtra(EXTRA_PACKAGE_NAME, packageName)
        }, UserHandle.SYSTEM)
    }

    fun sendStartShortcutBroadcast(
        context: Context,
        packageName: String,
        shortcutId: String,
        shortcutUserId: Int
    ) {
        if (PACKAGE_NAME == packageName) {
            context.sendBroadcastAsUser(Intent().apply {
                action = ACTION_START_MINI_WINDOW
                putExtra(EXTRA_PACKAGE_NAME, PACKAGE_NAME)
                putExtra(EXTRA_ACTIVITY_NAME, AllAppsPickerActivity::class.java.name)
            }, UserHandle.SYSTEM)
            return
        }

        context.sendBroadcastAsUser(Intent().apply {
            action = ACTION_START_MINI_WINDOW
            putExtra(EXTRA_PACKAGE_NAME, packageName)
            putExtra(EXTRA_SHORTCUT_ID, shortcutId)
            putExtra(EXTRA_SHORTCUT_USER_ID, shortcutUserId)
        }, UserHandle.SYSTEM)
    }
}
