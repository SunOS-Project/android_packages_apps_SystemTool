/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.observer

import android.os.Handler
import android.os.RemoteException
import android.view.Display.DEFAULT_DISPLAY
import android.view.IRotationWatcher
import android.view.Surface
import android.view.WindowManagerGlobal

import org.nameless.edge.EdgeService
import org.nameless.edge.util.IconLayoutAlgorithm
import org.nameless.edge.util.ViewHolder
import org.nameless.wm.PopUpDebugHelper.logE

class RotationWatcher(
    private val service: EdgeService,
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

    fun startWatch() {
        try {
            WindowManagerGlobal.getWindowManagerService()
                    .watchRotation(this, DEFAULT_DISPLAY)
        } catch (e: RemoteException) {
            logE(TAG, "Failed to register rotation watcher")
        }
    }

    fun stopWatch() {
        try {
            WindowManagerGlobal.getWindowManagerService().removeRotationWatcher(this)
        } catch (e: RemoteException) {
            logE(TAG, "Failed to unregister rotation watcher")
        }
    }

    private fun onDisplayRotated() {
        IconLayoutAlgorithm.rotationNeedsConsumeNavbar =
                displayRotation == Surface.ROTATION_270
        IconLayoutAlgorithm.updateNarbarHeight(service)
        service.onDisplayRotated()
    }

    companion object {
        private const val TAG = "EdgeTool::RotationWatcher"
    }
}
