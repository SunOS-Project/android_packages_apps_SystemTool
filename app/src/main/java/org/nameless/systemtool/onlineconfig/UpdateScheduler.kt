/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.onlineconfig

import android.app.AlarmManager
import android.icu.util.Calendar
import android.os.Handler

import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.onlineconfig.util.Shared.alarmManager
import org.nameless.systemtool.onlineconfig.util.Shared.updatePendingWifi
import org.nameless.systemtool.onlineconfig.util.Shared.wifiAvailable

class UpdateScheduler(
    private val handler: Handler
) : AlarmManager.OnAlarmListener {

    override fun onAlarm() {
        if (wifiAvailable) {
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

    companion object {
        private const val TAG = "SystemTool::UpdateScheduler"
    }
}
