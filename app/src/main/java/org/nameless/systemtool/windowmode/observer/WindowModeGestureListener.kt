/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.observer

import android.os.RemoteException
import android.view.MotionEvent
import android.view.WindowManagerGlobal

import org.nameless.systemtool.common.Utils.PACKAGE_NAME
import org.nameless.systemtool.common.Utils.logE
import org.nameless.systemtool.windowmode.ViewAnimator
import org.nameless.systemtool.windowmode.util.Shared.dimmerView
import org.nameless.systemtool.windowmode.util.Shared.navbarHeight
import org.nameless.systemtool.windowmode.util.Shared.service
import org.nameless.systemtool.windowmode.util.Shared.windowManager
import org.nameless.view.ISystemGestureListener

class WindowModeGestureListener : ISystemGestureListener.Stub() {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            if (value) {
                try {
                    WindowManagerGlobal.getWindowManagerService()?.registerSystemGestureListener(
                            PACKAGE_NAME, GESTURE_WINDOW_MODE, this)
                    field = true
                } catch (e: RemoteException) {
                    logE(TAG, "Failed to register system gesture listener")
                }
            } else {
                try {
                    WindowManagerGlobal.getWindowManagerService()?.unregisterSystemGestureListener(
                            PACKAGE_NAME, GESTURE_WINDOW_MODE, this)
                    field = false
                } catch (e: RemoteException) {
                    logE(TAG, "Failed to unregister system gesture listener")
                }
            }
        }

    private var triggered = false

    override fun onGestureCanceled(gesture: Int, evennt: MotionEvent) {
        if (gesture != GESTURE_WINDOW_MODE) {
            return
        }
        triggered = false
    }

    override fun onGesturePreTrigger(gesture: Int, event: MotionEvent) {
        if (gesture != GESTURE_WINDOW_MODE) {
            return
        }
        triggered = false
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
            dimmerView.offsetX = navbarHeight
            windowManager.let {
                it.currentWindowMetrics.bounds.let { bound ->
                    ViewAnimator.showCircle(event.x <= bound.width() / 2, bound)
                }
            }
        } else if (ViewAnimator.currentlyVisible) {
            dimmerView.post {
                dimmerView.onTouchEvent(event)
            }
        }
        if (event.actionMasked == MotionEvent.ACTION_UP ||
                event.actionMasked == MotionEvent.ACTION_CANCEL) {
            triggered = false
            dimmerView.offsetX = 0
        }
    }

    companion object {
        private const val TAG = "SystemTool::WindowModeGestureListener"
    }
}
