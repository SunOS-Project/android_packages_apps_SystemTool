/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.observer

import android.os.RemoteException
import android.view.MotionEvent
import android.view.WindowManagerGlobal

import org.nameless.systemtool.common.Utils
import org.nameless.systemtool.common.Utils.logE
import org.nameless.systemtool.windowmode.util.IconLayoutAlgorithm
import org.nameless.systemtool.windowmode.util.Shared.dimmerView
import org.nameless.systemtool.windowmode.util.Shared.service
import org.nameless.systemtool.windowmode.util.Shared.windowManager
import org.nameless.systemtool.windowmode.ViewHolder
import org.nameless.view.ISystemGestureListener

class WindowModeGestureListener : ISystemGestureListener.Stub() {

    private var triggered = false

    fun register() {
        try {
            WindowManagerGlobal.getWindowManagerService().registerSystemGestureListener(
                    Utils.PACKAGE_NAME, GESTURE_WINDOW_MODE, this)
        } catch (e: RemoteException) {
            logE(TAG, "Failed to register system gesture listener")
        }
    }

    fun unregister() {
        try {
            WindowManagerGlobal.getWindowManagerService().unregisterSystemGestureListener(
                    Utils.PACKAGE_NAME, GESTURE_WINDOW_MODE, this)
        } catch (e: RemoteException) {
            logE(TAG, "Failed to unregister system gesture listener")
        }
    }

    override fun onGestureCanceled(gesture: Int) {
        if (gesture != GESTURE_WINDOW_MODE) {
            return
        }
        triggered = false
        dimmerView.offsetX = 0
    }

    override fun onGesturePreTrigger(gesture: Int, event: MotionEvent) {
        if (gesture != GESTURE_WINDOW_MODE) {
            return
        }
        triggered = false
        dimmerView.offsetX = IconLayoutAlgorithm.navbarHeight
    }

    override fun onGesturePreTriggerBefore(gesture: Int, event: MotionEvent): Boolean {
        if (gesture != GESTURE_WINDOW_MODE) {
            return false
        }
        triggered = false
        return service.isGestureEnabled()
    }

    override fun onGestureTriggered(gesture: Int, event: MotionEvent) {
        if (gesture != GESTURE_WINDOW_MODE) {
            return
        }
        if (!triggered) {
            triggered = true
            windowManager.let {
                it.currentWindowMetrics.bounds.let { bound ->
                    ViewHolder.showForAll(event.x <= bound.width() / 2, bound)
                }
            }
        } else if (ViewHolder.currentlyVisible) {
            dimmerView.post {
                dimmerView.onTouchEvent(event)
            }
        }
    }

    companion object {
        private const val TAG = "SystemTool::WindowModeGestureListener"
    }
}
