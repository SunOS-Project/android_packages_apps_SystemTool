/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.observer

import android.os.RemoteException
import android.view.MotionEvent
import android.view.WindowManagerGlobal

import org.nameless.systemtool.EdgeService
import org.nameless.systemtool.util.Constants
import org.nameless.systemtool.util.IconLayoutAlgorithm
import org.nameless.systemtool.util.ViewHolder
import org.nameless.wm.ISystemGestureListener
import org.nameless.wm.ISystemGestureListener.GESTURE_WINDOW_MODE
import org.nameless.wm.PopUpDebugHelper.logE

class WindowModeGestureListener(
    private val service: EdgeService
) : ISystemGestureListener.Stub() {

    private var triggered = false

    fun register() {
        try {
            WindowManagerGlobal.getWindowManagerService().registerSystemGestureListener(
                    Constants.PACKAGE_NAME, GESTURE_WINDOW_MODE, this)
        } catch (e: RemoteException) {
            logE(TAG, "Failed to register system gesture listener")
        }
    }

    fun unregister() {
        try {
            WindowManagerGlobal.getWindowManagerService().unregisterSystemGestureListener(
                    Constants.PACKAGE_NAME, GESTURE_WINDOW_MODE, this)
        } catch (e: RemoteException) {
            logE(TAG, "Failed to unregister system gesture listener")
        }
    }

    override fun onGestureCanceled(gesture: Int) {
        if (gesture != GESTURE_WINDOW_MODE) {
            return
        }
        triggered = false
        ViewHolder.dimmerView?.offsetX = 0
    }

    override fun onGesturePreTrigger(gesture: Int, event: MotionEvent) {
        if (gesture != GESTURE_WINDOW_MODE) {
            return
        }
        triggered = false
        ViewHolder.dimmerView?.offsetX = IconLayoutAlgorithm.navbarHeight
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
            ViewHolder.getWindowManager(service)?.let {
                it.currentWindowMetrics.bounds.let { bound ->
                    ViewHolder.showForAll(event.x <= bound.width() / 2, it, bound)
                }
            }
        } else if (ViewHolder.currentlyVisible) {
            ViewHolder.dimmerView?.post {
                ViewHolder.dimmerView?.onTouchEvent(event)
            }
        }
    }

    companion object {
        private const val TAG = "SystemTool::WindowModeGestureListener"
    }
}
