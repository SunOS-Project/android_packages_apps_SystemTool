/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.observer

import android.os.Handler
import android.os.RemoteException
import android.view.Display.DEFAULT_DISPLAY
import android.view.IRotationWatcher
import android.view.Surface
import android.view.WindowManagerGlobal

import org.nameless.systemtool.common.Utils.logE
import org.nameless.systemtool.windowmode.ViewHolder
import org.nameless.systemtool.windowmode.util.IconLayoutAlgorithm

class RotationWatcher(
    private val handler: Handler
) : IRotationWatcher.Stub() {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            if (value) {
                try {
                    WindowManagerGlobal.getWindowManagerService()
                        .watchRotation(this, DEFAULT_DISPLAY)
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
            handler.post {
                onDisplayRotated()
            }
        }

    override fun onRotationChanged(rotation: Int) {
        if (rotation != displayRotation) {
            displayRotation = rotation
        }
    }

    private fun onDisplayRotated() {
        IconLayoutAlgorithm.rotationNeedsConsumeNavbar =
                displayRotation == Surface.ROTATION_270
        IconLayoutAlgorithm.updateNavbarHeight()
        ViewHolder.onScreenRotationChanged()
    }

    companion object {
        private const val TAG = "SystemTool::WindowMode::RotationWatcher"
    }
}
