/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.onlineconfig

import android.app.AlarmManager
import android.icu.util.Calendar
import android.os.Handler
import android.os.UserHandle
import android.provider.Settings

import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.onlineconfig.util.Constants.UPDATE_INTERCEPTED_INTERVAL
import org.nameless.systemtool.onlineconfig.util.Shared.alarmManager
import org.nameless.systemtool.onlineconfig.util.Shared.gameModeManager
import org.nameless.systemtool.onlineconfig.util.Shared.service
import org.nameless.systemtool.onlineconfig.util.Shared.updatePendingWifi
import org.nameless.systemtool.onlineconfig.util.Shared.wifiAvailable

class UpdateScheduler(
    private val handler: Handler
) : AlarmManager.OnAlarmListener {

    override fun onAlarm() {
        if (shouldInterceptUpdate()) {
            cancelScheduler()
            setScheduler(UPDATE_INTERCEPTED_INTERVAL)
        } else if (wifiAvailable) {
            OnlineConfigUpdater.update()
        } else {
            cancelScheduler()
            updatePendingWifi = true
        }
    }

    fun setScheduler(interval: Int) {
        logD(TAG, "setScheduler, interval=$interval")
        cancelScheduler()
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            Calendar.getInstance().apply {
                add(Calendar.SECOND, interval)
            }.timeInMillis,
            TAG, this, handler
        )
    }

    fun cancelScheduler() {
        alarmManager.cancel(this)
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
