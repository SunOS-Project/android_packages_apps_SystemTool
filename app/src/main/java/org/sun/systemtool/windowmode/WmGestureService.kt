/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.windowmode

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder

import org.sun.systemtool.windowmode.observer.DisplayResolutionChangeListener
import org.sun.systemtool.windowmode.observer.PackageStateObserver
import org.sun.systemtool.windowmode.observer.RotationWatcher
import org.sun.systemtool.windowmode.observer.ScreenStateObserver
import org.sun.systemtool.windowmode.observer.SettingsObserver
import org.sun.systemtool.windowmode.observer.SystemStateReceiver
import org.sun.systemtool.windowmode.observer.WindowModeGestureListener
import org.sun.systemtool.windowmode.util.Shared
import org.sun.systemtool.windowmode.view.AppLeftCircleViewGroup
import org.sun.systemtool.windowmode.view.AppRightCircleViewGroup
import org.sun.systemtool.windowmode.view.DimmerView

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
    private val screenStateObserver by lazy {
        ScreenStateObserver(handler)
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

        displayResolutionChangeListener.registered = true
        rotationWatcher.registered = true
        settingsObserver.registered = true
        screenStateObserver.registered = true
        systemStateReceiver.registered = true
        packageStateObserver.registered = true
        windowModeGestureListener.registered = true
    }

    override fun onDestroy() {
        windowModeGestureListener.registered = false
        packageStateObserver.registered = false
        systemStateReceiver.registered = false
        screenStateObserver.registered = false
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
