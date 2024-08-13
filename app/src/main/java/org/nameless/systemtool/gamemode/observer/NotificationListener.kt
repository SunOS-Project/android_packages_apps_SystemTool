/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.observer

import android.app.Notification
import android.content.ComponentName
import android.os.RemoteException
import android.os.UserHandle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

import org.nameless.systemtool.common.Utils.logE
import org.nameless.systemtool.gamemode.controller.DanmakuController
import org.nameless.systemtool.gamemode.util.Shared.currentGameInfo
import org.nameless.systemtool.gamemode.util.Shared.service

class NotificationListener : NotificationListenerService() {

    private val postedNotifications = mutableMapOf<String, Long>()

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            if (value) {
                try {
                    registerAsSystemService(
                        service,
                        ComponentName(service, javaClass),
                        UserHandle.USER_CURRENT
                    )
                } catch (e: RemoteException) {
                    logE(TAG, "Exception on registering notification listener")
                }
            } else {
                try {
                    unregisterAsSystemService()
                } catch (e: RemoteException) {
                    logE(TAG, "Exception on unregistering notification listener")
                }
            }
            field = value
        }
    var suspended = false

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (suspended) {
            return
        }
        if (!sbn.isClearable) {
            return
        }
        if (sbn.isOngoing) {
            return
        }
        if (sbn.isGroup) {
            return
        }
        if (sbn.packageName == currentGameInfo.packageName) {
            return
        }

        val title = sbn.notification.extras.getString(Notification.EXTRA_TITLE).takeIf {
            !it.isNullOrBlank()
        } ?: sbn.notification.extras.getString(Notification.EXTRA_TITLE_BIG)

        var danmakuText = String()
        title?.let {
            if (it.isNotBlank()) {
                danmakuText += "[$it] "
            }
        }
        sbn.notification.extras.getString(Notification.EXTRA_TEXT)?.let {
            if (it.isNotBlank()) {
                danmakuText += it
            }
        }
        danmakuText = danmakuText.trim()

        if (danmakuText.isBlank()) {
            return
        }

        val time = sbn.notification.`when`
        if (postedNotifications.containsKey(danmakuText) &&
                postedNotifications[danmakuText] == time) {
            return
        }

        if (postedNotifications.size >= POSTED_NOTIFICATION_MAX) {
            postedNotifications.clear()
        }
        postedNotifications[danmakuText] = time
        DanmakuController.postDanmaku(sbn.packageName, danmakuText)
    }

    companion object {
        private const val TAG = "SystemTool::GameMode::NotificationListener"

        private const val POSTED_NOTIFICATION_MAX = 233
    }
}
