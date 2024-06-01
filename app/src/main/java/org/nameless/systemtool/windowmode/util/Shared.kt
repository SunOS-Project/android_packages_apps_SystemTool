/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.util

import android.content.pm.LauncherApps
import android.os.UserManager
import android.view.WindowManager

import org.nameless.systemtool.windowmode.WmGestureService
import org.nameless.systemtool.windowmode.observer.SettingsObserver
import org.nameless.systemtool.windowmode.view.AppLeftCircleViewGroup
import org.nameless.systemtool.windowmode.view.AppRightCircleViewGroup
import org.nameless.systemtool.windowmode.view.DimmerView
import org.nameless.view.DisplayResolutionManager

object Shared {

    lateinit var service: WmGestureService
    lateinit var dimmerView: DimmerView
    lateinit var leftCircle: AppLeftCircleViewGroup
    lateinit var rightCircle: AppRightCircleViewGroup

    val launcherApps: LauncherApps by lazy {
        service.getSystemService(LauncherApps::class.java)!!
    }
    val resolutionManager: DisplayResolutionManager by lazy {
        service.getSystemService(DisplayResolutionManager::class.java)!!
    }
    val userManager: UserManager by lazy {
        service.getSystemService(UserManager::class.java)!!
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
