/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.util

import android.content.pm.LauncherApps
import android.hardware.display.DisplayManager
import android.media.AudioManager
import android.os.BatteryManager
import android.os.PowerManager
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import android.view.WindowManager

import org.nameless.app.GameModeManager
import org.nameless.systemtool.gamemode.GameAssistantService
import org.nameless.systemtool.gamemode.bean.GameInfo
import org.nameless.view.DisplayResolutionManager

object Shared {

    lateinit var service: GameAssistantService

    var portrait = true
    var screenShortWidth = 0
    var screenWidth = 0

    var currentGameInfo = GameInfo()
    var newGameLaunched = false

    val audioManager: AudioManager by lazy {
        service.getSystemService(AudioManager::class.java)!!
    }
    val batteryManager: BatteryManager by lazy {
        service.getSystemService(BatteryManager::class.java)!!
    }
    val displayManager: DisplayManager by lazy {
        service.getSystemService(DisplayManager::class.java)!!
    }
    val gameModeManager: GameModeManager by lazy {
        service.getSystemService(GameModeManager::class.java)!!
    }
    val launcherApps: LauncherApps by lazy {
        service.getSystemService(LauncherApps::class.java)!!
    }
    val powerManager: PowerManager by lazy {
        service.getSystemService(PowerManager::class.java)!!
    }
    val resolutionManager: DisplayResolutionManager by lazy {
        service.getSystemService(DisplayResolutionManager::class.java)!!
    }
    val telecomManager: TelecomManager by lazy {
        service.getSystemService(TelecomManager::class.java)!!
    }
    val telephonyManager: TelephonyManager by lazy {
        service.getSystemService(TelephonyManager::class.java)!!
    }
    val windowManager: WindowManager by lazy {
        service.getSystemService(WindowManager::class.java)!!
    }
}
