/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder

import org.nameless.edge.observer.GameStateReceiver
import org.nameless.edge.observer.PackageStateObserver
import org.nameless.edge.observer.RotationWatcher
import org.nameless.edge.observer.ScreenStateReceiver
import org.nameless.edge.observer.SettingsObserver
import org.nameless.edge.observer.SystemStateReceiver
import org.nameless.edge.util.PackageInfoCache
import org.nameless.edge.util.ViewHolder
import org.nameless.edge.view.DimmerView

class EdgeService : Service() {

    private var gameStateReceiver: GameStateReceiver? = null
    private var packageStateObserver: PackageStateObserver? = null
    private var rotationWatcher: RotationWatcher? = null
    private var screenStateReceiver: ScreenStateReceiver? = null
    private var settingsObserver: SettingsObserver? = null
    private var systemStateReceiver: SystemStateReceiver? = null

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

        rotationWatcher?.startWatch()
        settingsObserver?.register()
        gameStateReceiver?.register()
        screenStateReceiver?.register()
        systemStateReceiver?.register()
        packageStateObserver?.register()
    }

    override fun onDestroy() {
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
}
