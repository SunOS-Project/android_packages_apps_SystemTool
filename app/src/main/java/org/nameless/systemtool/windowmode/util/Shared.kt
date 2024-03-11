/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.util

import android.app.KeyguardManager
import android.view.WindowManager

import org.nameless.systemtool.windowmode.WmGestureService
import org.nameless.systemtool.windowmode.view.DimmerView
import org.nameless.view.AppFocusManager
import org.nameless.view.DisplayResolutionManager

object Shared {

    lateinit var service: WmGestureService
    lateinit var dimmerView: DimmerView

    val appFocusManager: AppFocusManager by lazy {
        service.getSystemService(AppFocusManager::class.java)
    }
    val keyguardManager: KeyguardManager by lazy {
        service.getSystemService(KeyguardManager::class.java)
    }
    val resolutionManager: DisplayResolutionManager by lazy {
        service.getSystemService(DisplayResolutionManager::class.java)
    }
    val windowManager: WindowManager by lazy {
        service.getSystemService(WindowManager::class.java)
    }
}
