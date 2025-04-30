/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.windowmode.observer

import android.os.Handler

import org.sun.systemtool.windowmode.util.Shared.resolutionManager
import org.sun.systemtool.windowmode.util.Shared.updateCircleViewGroup
import org.sun.systemtool.windowmode.util.Shared.updateNavbarHeight
import org.sun.view.IDisplayResolutionListener

class DisplayResolutionChangeListener(
    private val handler: Handler
) : IDisplayResolutionListener.Stub() {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                resolutionManager.registerDisplayResolutionListener(this)
            } else {
                resolutionManager.unregisterDisplayResolutionListener(this)
            }
        }

    private var displayWidth = -1
        set(value) {
            field = value
            handler.postDelayed({
                updateNavbarHeight()
                updateCircleViewGroup()
            }, 500L)
        }

    override fun onDisplayResolutionChanged(width: Int, height: Int) {
        if (displayWidth != width) {
            displayWidth = width
        }
    }
}
