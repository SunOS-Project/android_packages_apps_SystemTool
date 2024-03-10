/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.observer

import android.os.Handler

import org.nameless.systemtool.iris.util.Shared.displayResolutionManager
import org.nameless.view.IDisplayResolutionListener

abstract class DisplayResolutionChangeListener(
    private val handler: Handler
) : IDisplayResolutionListener.Stub() {

    private var registered = false

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

    fun register() {
        if (registered) {
            return
        }
        registered = true
        displayResolutionManager.registerDisplayResolutionListener(this)
    }

    fun unregister() {
        if (!registered) {
            return
        }
        registered = false
        displayResolutionManager.unregisterDisplayResolutionListener(this)
    }

    abstract fun onDisplayWidthChanged()

    companion object {
        private const val TAG = "SystemTool::Iris::DisplayResolutionChangeListener"
    }
}
