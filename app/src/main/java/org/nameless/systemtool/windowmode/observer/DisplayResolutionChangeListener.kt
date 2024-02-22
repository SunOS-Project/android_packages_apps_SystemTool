/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.observer

import android.os.Handler

import org.nameless.systemtool.windowmode.ViewHolder
import org.nameless.systemtool.windowmode.util.IconLayoutAlgorithm
import org.nameless.systemtool.windowmode.util.Shared.resolutionManager
import org.nameless.view.IDisplayResolutionListener

class DisplayResolutionChangeListener(
    private val handler: Handler
) : IDisplayResolutionListener.Stub() {

    private var displayWidth = -1

    override fun onDisplayResolutionChanged(width: Int, height: Int) {
        if (displayWidth != width) {
            displayWidth = width
            handler.postDelayed({
                IconLayoutAlgorithm.updateNavbarHeight()
                ViewHolder.relocateIconView()
            }, 500L)
        }
    }

    fun register() {
        resolutionManager.registerDisplayResolutionListener(this)
    }

    fun unregister() {
        resolutionManager.unregisterDisplayResolutionListener(this)
    }
}
