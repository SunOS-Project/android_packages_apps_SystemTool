/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.gamemode.observer

import android.os.RemoteException
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.WindowManagerGlobal

import org.sun.systemtool.common.Utils.PACKAGE_NAME
import org.sun.systemtool.common.Utils.logE
import org.sun.systemtool.gamemode.controller.GamePanelViewController
import org.sun.systemtool.gamemode.view.GamePanelView
import org.sun.view.ISystemGestureListener

class GameModeGestureListener : ISystemGestureListener.Stub() {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            if (value) {
                try {
                    WindowManagerGlobal.getWindowManagerService()?.registerSystemGestureListener(
                            PACKAGE_NAME, GESTURE_GAME_MODE, this)
                    field = true
                } catch (e: RemoteException) {
                    logE(TAG, "Failed to register system gesture listener")
                }
            } else {
                try {
                    WindowManagerGlobal.getWindowManagerService()?.unregisterSystemGestureListener(
                            PACKAGE_NAME, GESTURE_GAME_MODE, this)
                    field = false
                } catch (e: RemoteException) {
                    logE(TAG, "Failed to unregister system gesture listener")
                }
            }
        }

    private var velocityTracker: VelocityTracker? = null

    override fun onGestureCanceled(gesture: Int, event: MotionEvent) {
        GamePanelViewController.setContainerTouch(false)
    }

    override fun onGesturePreTrigger(gesture: Int, event: MotionEvent) {
        if (gesture != GESTURE_GAME_MODE) {
            return
        }
        velocityTracker = VelocityTracker.obtain()
    }

    override fun onGesturePreTriggerBefore(gesture: Int, event: MotionEvent): Boolean {
        if (gesture != GESTURE_GAME_MODE) {
            return false
        }
        if (!GamePanelViewController.animating &&
                !GamePanelViewController.isShowing() &&
                GamePanelViewController.sideViewFullyShowing) {
            return true
        }
        return false
    }

    override fun onGestureTriggered(gesture: Int, event: MotionEvent) {
        if (gesture != GESTURE_GAME_MODE) {
            return
        }
        if (event.actionMasked != MotionEvent.ACTION_UP &&
                event.actionMasked != MotionEvent.ACTION_CANCEL) {
            velocityTracker?.apply {
                addMovement(event)
                computeCurrentVelocity(GamePanelView.VELOCITY_UNIT_MS)
            }
            GamePanelViewController.expandPanelView(event)
        } else {
            velocityTracker?.apply {
                computeCurrentVelocity(GamePanelView.VELOCITY_UNIT_MS)
                GamePanelViewController.onGestureUp(xVelocity)
                clear()
                recycle()
            }
            velocityTracker = null
        }
    }

    companion object {
        private const val TAG = "SystemTool::GameModeGestureListener"
    }
}
