/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import android.os.Handler

import org.nameless.edge.observer.PackageStateObserver
import org.nameless.edge.observer.ScreenStateReceiver
import org.nameless.edge.observer.SettingsObserver
import org.nameless.edge.observer.StartBroadcastReceiver
import org.nameless.edge.util.PackageInfoCache
import org.nameless.edge.util.ViewHolder
import org.nameless.edge.view.DimmerView

class EdgeService : Service() {

    private var packageStateObserver: PackageStateObserver? = null
    private var screenStateReceiver: ScreenStateReceiver? = null
    private var settingsObserver: SettingsObserver? = null
    private var startBroadcastReceiver: StartBroadcastReceiver? = null

    private val handler = Handler()

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        if (!DimmerView.addDimmerView(this)) {
            stopSelf()
        }

        handler.post { PackageInfoCache.initPackageList(this) }

        packageStateObserver = PackageStateObserver(this, handler)
        screenStateReceiver = ScreenStateReceiver(this, handler)
        settingsObserver = SettingsObserver(this, handler)
        startBroadcastReceiver = StartBroadcastReceiver(this, handler)

        settingsObserver?.register()
        screenStateReceiver?.register()
        packageStateObserver?.register()
        startBroadcastReceiver?.register()
    }

    override fun onDestroy() {
        startBroadcastReceiver?.unregister()
        packageStateObserver?.unregister()
        screenStateReceiver?.unregister()
        settingsObserver?.unregister()

        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        ViewHolder.onScreenOrientationChanged(this, newConfig.orientation)
    }
}
