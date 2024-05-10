/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.observer

import android.os.Handler
import android.os.RemoteException
import android.view.Display.DEFAULT_DISPLAY
import android.view.IRotationWatcher
import android.view.Surface
import android.view.WindowManagerGlobal

import org.nameless.systemtool.common.Utils.logE
import org.nameless.systemtool.gamemode.controller.GamePanelViewController

class RotationWatcher(
    private val handler: Handler,
    private val infoListener: GameModeInfoListener
) : IRotationWatcher.Stub() {

    private val displayRotatedCallback = Runnable {
        if (infoListener.inGame) {
            GamePanelViewController.resetPanelView()
        }
    }

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            if (value) {
                try {
                    WindowManagerGlobal.getWindowManagerService()?.watchRotation(this, DEFAULT_DISPLAY)
                    field = true
                } catch (e: RemoteException) {
                    logE(TAG, "Failed to register rotation watcher")
                }
            } else {
                try {
                    WindowManagerGlobal.getWindowManagerService()?.removeRotationWatcher(this)
                    field = false
                } catch (e: RemoteException) {
                    logE(TAG, "Failed to unregister rotation watcher")
                }
            }
        }

    private var displayRotation = Surface.ROTATION_0
        set(value) {
            field = value
            if (handler.hasCallbacks(displayRotatedCallback)) {
                handler.removeCallbacks(displayRotatedCallback)
            }
            handler.postDelayed(displayRotatedCallback, 500L)
        }

    override fun onRotationChanged(rotation: Int) {
        if (rotation != displayRotation) {
            displayRotation = rotation
        }
    }

    companion object {
        private const val TAG = "SystemTool::GameMode::RotationWatcher"
    }
}
