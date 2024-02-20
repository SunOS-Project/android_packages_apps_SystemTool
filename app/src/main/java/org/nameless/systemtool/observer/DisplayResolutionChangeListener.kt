/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.observer

import android.os.Handler

import org.nameless.systemtool.EdgeService
import org.nameless.systemtool.util.IconLayoutAlgorithm
import org.nameless.systemtool.util.ViewHolder
import org.nameless.view.DisplayResolutionManager
import org.nameless.view.DisplayResolutionManager.FHD_WIDTH
import org.nameless.view.IDisplayResolutionListener

class DisplayResolutionChangeListener(
    private val service: EdgeService,
    private val handler: Handler
) : IDisplayResolutionListener.Stub() {

    private val displayResolutionManager = service.getSystemService(DisplayResolutionManager::class.java)
    private var displayWidth = FHD_WIDTH

    override fun onDisplayResolutionChanged(width: Int, height: Int) {
        if (displayWidth != width) {
            displayWidth = width
            handler.postDelayed({
                IconLayoutAlgorithm.updateNarbarHeight(service)
                ViewHolder.relocateIconView(service)
            }, 500L)
        }
    }

    fun register() {
        displayResolutionManager.registerDisplayResolutionListener(this)
    }

    fun unregister() {
        displayResolutionManager.unregisterDisplayResolutionListener(this)
    }
}
