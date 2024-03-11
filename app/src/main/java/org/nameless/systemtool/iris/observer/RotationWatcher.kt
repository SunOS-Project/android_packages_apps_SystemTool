/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.observer

import android.os.Handler
import android.os.RemoteException
import android.view.Display.DEFAULT_DISPLAY
import android.view.IRotationWatcher
import android.view.Surface
import android.view.WindowManagerGlobal

import org.nameless.systemtool.common.Utils.logE

abstract class RotationWatcher(
    private val handler: Handler
) : IRotationWatcher.Stub() {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            if (value) {
                try {
                    WindowManagerGlobal.getWindowManagerService().watchRotation(
                            this, DEFAULT_DISPLAY)
                    field = true
                } catch (e: RemoteException) {
                    logE(TAG, "Failed to register rotation watcher")
                }
            } else {
                try {
                    WindowManagerGlobal.getWindowManagerService().removeRotationWatcher(this)
                    field = false
                } catch (e: RemoteException) {
                    logE(TAG, "Failed to unregister rotation watcher")
                }
            }
        }

    private var displayRotation = Surface.ROTATION_0
        set(value) {
            field = value
            onDisplayRotated()
        }
    var isLandscape = false
        get() {
            return displayRotation == Surface.ROTATION_90 || displayRotation == Surface.ROTATION_270
        }

    override fun onRotationChanged(rotation: Int) {
        handler.post {
            if (rotation != displayRotation) {
                displayRotation = rotation
            }
        }
    }

    abstract fun onDisplayRotated()

    companion object {
        private const val TAG = "SystemTool::Iris::RotationWatcher"
    }
}
