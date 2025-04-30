/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.onlineconfig

import android.app.AlarmManager
import android.icu.util.Calendar
import android.os.Handler
import android.os.UserHandle
import android.provider.Settings

import org.sun.systemtool.common.Utils.logD
import org.sun.systemtool.onlineconfig.util.Constants.UPDATE_INTERCEPTED_INTERVAL
import org.sun.systemtool.onlineconfig.util.Shared.alarmManager
import org.sun.systemtool.onlineconfig.util.Shared.gameModeManager
import org.sun.systemtool.onlineconfig.util.Shared.service
import org.sun.systemtool.onlineconfig.util.Shared.updatePendingWifi
import org.sun.systemtool.onlineconfig.util.Shared.wifiAvailable

class UpdateScheduler(
    private val handler: Handler
) : AlarmManager.OnAlarmListener {

    var scheduler = -1
        set(value) {
            field = value
            if (value <= 0) {
                logD(TAG, "cancelScheduler")
                alarmManager.cancel(this)
            } else {
                logD(TAG, "setScheduler, interval=$value")
                alarmManager.cancel(this)
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    Calendar.getInstance().apply {
                        add(Calendar.SECOND, value)
                    }.timeInMillis,
                    TAG, this, handler
                )
            }
        }

    override fun onAlarm() {
        if (shouldInterceptUpdate()) {
            scheduler = UPDATE_INTERCEPTED_INTERVAL
        } else if (wifiAvailable) {
            OnlineConfigUpdater.update()
        } else {
            scheduler = -1
            updatePendingWifi = true
        }
    }

    private fun shouldInterceptUpdate(): Boolean {
        if (Settings.Secure.getIntForUser(service.contentResolver,
                Settings.Secure.USER_SETUP_COMPLETE, 0, UserHandle.USER_CURRENT) == 0) {
            return true
        }
        if (gameModeManager.gameModeInfo?.isInGame == true) {
            return true
        }
        return false
    }

    companion object {
        private const val TAG = "SystemTool::UpdateScheduler"
    }
}
