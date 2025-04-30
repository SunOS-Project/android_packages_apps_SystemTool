/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.iris.observer

import android.os.Handler

import org.sun.systemtool.iris.util.Shared.displayResolutionManager
import org.sun.view.IDisplayResolutionListener

abstract class DisplayResolutionChangeListener(
    private val handler: Handler
) : IDisplayResolutionListener.Stub() {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                displayResolutionManager.registerDisplayResolutionListener(this)
            } else {
                displayResolutionManager.unregisterDisplayResolutionListener(this)
            }
        }

    var displayWidth = -1
        set(value) {
            field = value
            onDisplayWidthChanged()
        }

    override fun onDisplayResolutionChanged(width: Int, height: Int) {
        handler.post {
            if (displayWidth != width) {
                displayWidth = width
            }
        }
    }

    abstract fun onDisplayWidthChanged()

    companion object {
        private const val TAG = "SystemTool::Iris::DisplayResolutionChangeListener"
    }
}
