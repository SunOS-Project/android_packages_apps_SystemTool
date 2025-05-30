/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.iris.util

import android.os.PowerManager
import android.view.LayoutInflater

import org.sun.content.OnlineConfigManager
import org.sun.display.RefreshRateManager
import org.sun.systemtool.iris.IrisService
import org.sun.view.DisplayResolutionManager

object Shared {

    lateinit var service: IrisService

    var powerSaveMode = false

    val displayResolutionManager: DisplayResolutionManager by lazy {
        service.getSystemService(DisplayResolutionManager::class.java)!!
    }
    val layoutInflater: LayoutInflater by lazy {
        service.getSystemService(LayoutInflater::class.java)!!
    }
    val onlineConfigManager: OnlineConfigManager by lazy {
        service.getSystemService(OnlineConfigManager::class.java)!!
    }
    val powerManager: PowerManager by lazy {
        service.getSystemService(PowerManager::class.java)!!
    }
    val refreshRateManager: RefreshRateManager by lazy {
        service.getSystemService(RefreshRateManager::class.java)!!
    }
}
