/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.util

import android.app.KeyguardManager
import android.os.PowerManager
import android.view.LayoutInflater

import org.nameless.content.OnlineConfigManager
import org.nameless.display.RefreshRateManager
import org.nameless.systemtool.iris.IrisService
import org.nameless.view.AppFocusManager
import org.nameless.view.DisplayResolutionManager

object Shared {

    lateinit var service: IrisService

    var powerSaveMode = false

    val appFocusManager: AppFocusManager by lazy {
        service.getSystemService(AppFocusManager::class.java)
    }
    val displayResolutionManager: DisplayResolutionManager by lazy {
        service.getSystemService(DisplayResolutionManager::class.java)
    }
    val keyguardManager: KeyguardManager by lazy {
        service.getSystemService(KeyguardManager::class.java)
    }
    val layoutInflater: LayoutInflater by lazy {
        service.getSystemService(LayoutInflater::class.java)
    }
    val onlineConfigManager: OnlineConfigManager by lazy {
        service.getSystemService(OnlineConfigManager::class.java)
    }
    val powerManager: PowerManager by lazy {
        service.getSystemService(PowerManager::class.java)
    }
    val refreshRateManager: RefreshRateManager by lazy {
        service.getSystemService(RefreshRateManager::class.java)
    }
}
