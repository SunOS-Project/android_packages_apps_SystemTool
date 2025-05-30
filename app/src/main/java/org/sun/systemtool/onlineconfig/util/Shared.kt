/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.onlineconfig.util

import android.app.AlarmManager
import android.net.ConnectivityManager

import org.sun.app.GameModeManager
import org.sun.content.OnlineConfigManager
import org.sun.systemtool.onlineconfig.OnlineConfigService
import org.sun.systemtool.onlineconfig.UpdateScheduler

object Shared {

    lateinit var service: OnlineConfigService
    lateinit var updateScheduler: UpdateScheduler

    var debugMode = false

    var updatePendingWifi = false
    var wifiAvailable = false

    val alarmManager: AlarmManager by lazy {
        service.getSystemService(AlarmManager::class.java)!!
    }
    val connectivityManager: ConnectivityManager by lazy {
        service.getSystemService(ConnectivityManager::class.java)!!
    }
    val gameModeManager: GameModeManager by lazy {
        service.getSystemService(GameModeManager::class.java)!!
    }
    val onlineConfigManager: OnlineConfigManager by lazy {
        service.getSystemService(OnlineConfigManager::class.java)!!
    }
}
