/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.util

import android.app.KeyguardManager
import android.view.WindowManager

import org.nameless.systemtool.windowmode.WmGestureService
import org.nameless.systemtool.windowmode.observer.SettingsObserver
import org.nameless.systemtool.windowmode.view.AppLeftCircleViewGroup
import org.nameless.systemtool.windowmode.view.AppRightCircleViewGroup
import org.nameless.systemtool.windowmode.view.DimmerView
import org.nameless.view.AppFocusManager
import org.nameless.view.DisplayResolutionManager

object Shared {

    lateinit var service: WmGestureService
    lateinit var dimmerView: DimmerView
    lateinit var leftCircle: AppLeftCircleViewGroup
    lateinit var rightCircle: AppRightCircleViewGroup

    val appFocusManager: AppFocusManager by lazy {
        service.getSystemService(AppFocusManager::class.java)!!
    }
    val keyguardManager: KeyguardManager by lazy {
        service.getSystemService(KeyguardManager::class.java)!!
    }
    val resolutionManager: DisplayResolutionManager by lazy {
        service.getSystemService(DisplayResolutionManager::class.java)!!
    }
    val windowManager: WindowManager by lazy {
        service.getSystemService(WindowManager::class.java)!!
    }

    var isEditing = false

    var gesturalMode = false
    var rotationNeedsConsumeNavbar = false
    var navbarHeight = 0

    fun clearCircleView() {
        leftCircle.post { leftCircle.removeAllViews() }
        rightCircle.post { rightCircle.removeAllViews() }
    }

    fun updateCircleViewGroup() {
        leftCircle.post { leftCircle.invalidate() }
        rightCircle.post { rightCircle.invalidate() }
    }

    fun updateNavbarHeight() {
        gesturalMode = SettingsObserver.isGesturalMode(service)
        navbarHeight = if (rotationNeedsConsumeNavbar) {
            if (gesturalMode) {
                0
            } else {
                service.resources.getDimensionPixelSize(
                    com.android.internal.R.dimen.navigation_bar_height_landscape
                )
            }
        } else {
            0
        }
    }
}
