/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.observer

import android.os.RemoteException
import android.view.MotionEvent
import android.view.WindowManagerGlobal
import androidx.core.view.isVisible

import org.nameless.systemtool.common.Utils.PACKAGE_NAME
import org.nameless.systemtool.common.Utils.logE
import org.nameless.systemtool.gamemode.controller.GamePanelViewController
import org.nameless.systemtool.gamemode.util.Shared.screenShortWidth
import org.nameless.view.ISystemGestureListener

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

    override fun onGestureCanceled(gesture: Int) {
        GamePanelViewController.setContainerTouch(false)
    }

    override fun onGesturePreTrigger(gesture: Int, event: MotionEvent) {
        // Do nothing
    }

    override fun onGesturePreTriggerBefore(gesture: Int, event: MotionEvent): Boolean {
        if (gesture != GESTURE_GAME_MODE) {
            return false
        }
        if (!GamePanelViewController.animating &&
                !GamePanelViewController.isShowing()) {
            return true
        }
        return false
    }

    override fun onGestureTriggered(gesture: Int, event: MotionEvent) {
        if (gesture != GESTURE_GAME_MODE) {
            return
        }
        if (event.actionMasked == MotionEvent.ACTION_UP) {
            GamePanelViewController.onGestureUp()
        } else {
            GamePanelViewController.movePanelView(event.rawX)
        }
    }

    companion object {
        private const val TAG = "SystemTool::GameModeGestureListener"
    }
}
