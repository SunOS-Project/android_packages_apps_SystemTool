/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Choreographer
import android.view.InputEvent
import android.view.MotionEvent

import com.android.systemui.shared.system.InputChannelCompat.InputEventReceiver
import com.android.systemui.shared.system.InputMonitorCompat

import kotlin.math.max
import kotlin.math.min

import org.nameless.edge.consumer.AppCircleInputConsumer
import org.nameless.edge.consumer.InputConsumer
import org.nameless.edge.observer.GameStateReceiver
import org.nameless.edge.observer.PackageStateObserver
import org.nameless.edge.observer.RotationWatcher
import org.nameless.edge.observer.ScreenStateReceiver
import org.nameless.edge.observer.SettingsObserver
import org.nameless.edge.observer.SystemStateReceiver
import org.nameless.edge.util.Constants
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

    private var inputEventReceiver: InputEventReceiver? = null
    private var inputMonitorCompat: InputMonitorCompat? = null
    private var inputConsumer: InputConsumer? = null

    private var mainChoreographer: Choreographer? = null

    private val handler = Handler()

    private var touchRegionX = Pair(0f, 0f)
    private var touchRegionY = 0f

    private var fromDown = false
    private var fromLeft = false

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

        updateGestureTouchRegion()

        mainChoreographer = Choreographer.getInstance()
        initInputMonitor()
    }

    override fun onDestroy() {
        disposeEventHandlers()

        packageStateObserver?.unregister()
        systemStateReceiver?.unregister()
        screenStateReceiver?.unregister()
        gameStateReceiver?.unregister()
        settingsObserver?.unregister()
        rotationWatcher?.stopWatch()

        super.onDestroy()
    }

    fun onDisplayRotated() {
        ViewHolder.onScreenRotationChanged(this)
        updateGestureTouchRegion()
    }

    fun updateGestureTouchRegion() {
        var width = 0
        var height = 0
        ViewHolder.getWindowManager(this)?.currentWindowMetrics?.bounds?.let {
            width = it.width()
            height = it.height()
        } ?: return
        if (height > width) {
            touchRegionX = Pair(
                width * Constants.gestureRegionShort,
                width * (1f - Constants.gestureRegionShort)
            )
            touchRegionY = height * (1f - Constants.gestureRegionLong)
        } else {
            touchRegionX = Pair(
                width * Constants.gestureRegionLong,
                width * (1f - Constants.gestureRegionLong)
            )
            touchRegionY = height * (1f - Constants.gestureRegionShort)
        }
    }

    private fun initInputMonitor() {
        inputMonitorCompat = InputMonitorCompat("edge-tool-gesture", display.displayId)
        inputConsumer = AppCircleInputConsumer(this, inputMonitorCompat)
        inputEventReceiver = inputMonitorCompat?.getInputReceiver(Looper.getMainLooper(),
                mainChoreographer, { ev ->
                    onInputEvent(ev)
                })
    }

    private fun disposeEventHandlers() {
        inputEventReceiver?.dispose()
        inputEventReceiver = null

        inputMonitorCompat?.dispose()
        inputMonitorCompat = null
    }

    private fun onInputEvent(event: InputEvent) {
        if (event !is MotionEvent) {
            return
        }
        if (event.action == MotionEvent.ACTION_DOWN) {
            if ((event.x > touchRegionX.first && event.x < touchRegionX.second)
                    || event.y < touchRegionY) {
                fromDown = false
                return
            }
            if (!(settingsObserver?.isUserSetuped() ?: true)) {
                fromDown = false
                return
            }
            if (!(settingsObserver?.isGestureEnabled() ?: true)) {
                fromDown = false
                return
            }
            if (gameStateReceiver?.isInGameMode() ?: false) {
                fromDown = false
                return
            }
            if (!ViewHolder.allowVisible) {
                fromDown = false
                return
            }
            fromDown = true
            fromLeft = event.x <= touchRegionX.first
        }
        if (!fromDown) {
            return
        }
        inputConsumer?.onMotionEvent(event, fromLeft)
    }
}
