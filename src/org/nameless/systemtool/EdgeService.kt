/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder

import org.nameless.systemtool.observer.GameStateReceiver
import org.nameless.systemtool.observer.PackageStateObserver
import org.nameless.systemtool.observer.RotationWatcher
import org.nameless.systemtool.observer.ScreenStateReceiver
import org.nameless.systemtool.observer.SettingsObserver
import org.nameless.systemtool.observer.SystemStateReceiver
import org.nameless.systemtool.observer.WindowModeGestureListener
import org.nameless.systemtool.util.PackageInfoCache
import org.nameless.systemtool.util.ViewHolder
import org.nameless.systemtool.view.DimmerView

class EdgeService : Service() {

    private var gameStateReceiver: GameStateReceiver? = null
    private var packageStateObserver: PackageStateObserver? = null
    private var rotationWatcher: RotationWatcher? = null
    private var screenStateReceiver: ScreenStateReceiver? = null
    private var settingsObserver: SettingsObserver? = null
    private var systemStateReceiver: SystemStateReceiver? = null
    private var windowModeGestureListener: WindowModeGestureListener? = null

    private val handler = Handler()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        if (!DimmerView.addDimmerView(this)) {
            stopSelf()
        }

        PackageInfoCache.initPackageList(this)

        gameStateReceiver = GameStateReceiver(this, handler)
        packageStateObserver = PackageStateObserver(this, handler)
        rotationWatcher = RotationWatcher(this, handler)
        screenStateReceiver = ScreenStateReceiver(this, handler)
        settingsObserver = SettingsObserver(this, handler)
        systemStateReceiver = SystemStateReceiver(this, handler)
        windowModeGestureListener = WindowModeGestureListener(this)

        rotationWatcher?.startWatch()
        settingsObserver?.register()
        gameStateReceiver?.register()
        screenStateReceiver?.register()
        systemStateReceiver?.register()
        packageStateObserver?.register()
        windowModeGestureListener?.register()
    }

    override fun onDestroy() {
        windowModeGestureListener?.unregister()
        packageStateObserver?.unregister()
        systemStateReceiver?.unregister()
        screenStateReceiver?.unregister()
        gameStateReceiver?.unregister()
        settingsObserver?.unregister()
        rotationWatcher?.stopWatch()

        ViewHolder.safelyClearIconViews(this)
        ViewHolder.removeDimmerView(this)

        super.onDestroy()
    }

    fun isGestureEnabled(): Boolean {
        if (!(settingsObserver?.isUserSetuped() ?: true)) {
            return false
        }
        if (!(settingsObserver?.isGestureEnabled() ?: true)) {
            return false
        }
        if (gameStateReceiver?.isInGameMode() ?: false) {
            return false
        }
        if (!ViewHolder.allowVisible) {
            return false
        }
        return true
    }
}
