/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder

import org.nameless.systemtool.windowmode.observer.DisplayResolutionChangeListener
import org.nameless.systemtool.windowmode.observer.PackageStateObserver
import org.nameless.systemtool.windowmode.observer.RotationWatcher
import org.nameless.systemtool.windowmode.observer.ScreenStateReceiver
import org.nameless.systemtool.windowmode.observer.SettingsObserver
import org.nameless.systemtool.windowmode.observer.SystemStateReceiver
import org.nameless.systemtool.windowmode.observer.WindowModeGestureListener
import org.nameless.systemtool.windowmode.util.PackageInfoCache
import org.nameless.systemtool.windowmode.util.Shared
import org.nameless.systemtool.windowmode.view.AppLeftCircleViewGroup
import org.nameless.systemtool.windowmode.view.AppRightCircleViewGroup
import org.nameless.systemtool.windowmode.view.DimmerView

class WmGestureService : Service() {

    private val handlerThread by lazy {
        HandlerThread("SystemTool::WmGestureService").apply { start() }
    }
    private val handler by lazy { Handler(handlerThread.looper) }

    private val displayResolutionChangeListener by lazy {
        DisplayResolutionChangeListener(handler)
    }
    private val packageStateObserver by lazy {
        PackageStateObserver(handler)
    }
    private val rotationWatcher by lazy {
        RotationWatcher(handler)
    }
    private val screenStateReceiver by lazy {
        ScreenStateReceiver(handler)
    }
    private val settingsObserver by lazy {
        SettingsObserver(handler)
    }
    private val systemStateReceiver by lazy {
        SystemStateReceiver(handler)
    }
    private val windowModeGestureListener by lazy {
        WindowModeGestureListener()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        Shared.service = this

        if (!DimmerView.addDimmerView()) {
            stopSelf()
        }
        if (!AppLeftCircleViewGroup.addCircleViewGroup()) {
            stopSelf()
        }
        if (!AppRightCircleViewGroup.addCircleViewGroup()) {
            stopSelf()
        }

        PackageInfoCache.initPackageList()

        displayResolutionChangeListener.registered = true
        rotationWatcher.registered = true
        settingsObserver.registered = true
        screenStateReceiver.registered = true
        systemStateReceiver.registered = true
        packageStateObserver.registered = true
        windowModeGestureListener.registered = true
    }

    override fun onDestroy() {
        windowModeGestureListener.registered = false
        packageStateObserver.registered = false
        systemStateReceiver.registered = false
        screenStateReceiver.registered = false
        settingsObserver.registered = false
        rotationWatcher.registered = false
        displayResolutionChangeListener.registered = false

        ViewAnimator.hideCircle()
        DimmerView.removeDimmerView()

        super.onDestroy()
    }

    fun isGestureEnabled(): Boolean {
        if (!settingsObserver.userSetupCompleted) {
            return false
        }
        if (!settingsObserver.gestureEnabled) {
            return false
        }
        if (!ViewAnimator.allowVisible) {
            return false
        }
        return true
    }
}
