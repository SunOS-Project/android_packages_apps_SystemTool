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
import org.nameless.systemtool.windowmode.util.IconLayoutAlgorithm
import org.nameless.systemtool.windowmode.ViewHolder

class RotationWatcher(
    private val handler: Handler
) : IRotationWatcher.Stub() {

    var displayRotation = Surface.ROTATION_0

    override fun onRotationChanged(rotation: Int) {
        ViewHolder.hideForAll()
        handler.postDelayed({
            if (rotation != displayRotation) {
                displayRotation = rotation
                onDisplayRotated()
            }
        }, 500L)
    }

    fun register() {
        try {
            WindowManagerGlobal.getWindowManagerService()
                    .watchRotation(this, DEFAULT_DISPLAY)
        } catch (e: RemoteException) {
            logE(TAG, "Failed to register rotation watcher")
        }
    }

    fun unregister() {
        try {
            WindowManagerGlobal.getWindowManagerService().removeRotationWatcher(this)
        } catch (e: RemoteException) {
            logE(TAG, "Failed to unregister rotation watcher")
        }
    }

    private fun onDisplayRotated() {
        IconLayoutAlgorithm.rotationNeedsConsumeNavbar =
                displayRotation == Surface.ROTATION_270
        IconLayoutAlgorithm.updateNavbarHeight()
        ViewHolder.onScreenRotationChanged()
    }

    companion object {
        private const val TAG = "SystemTool::RotationWatcher"
    }
}
