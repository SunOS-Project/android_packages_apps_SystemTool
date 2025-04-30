/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.gamemode.observer

import org.sun.systemtool.gamemode.controller.GamePanelViewController
import org.sun.systemtool.gamemode.util.Shared.inGame
import org.sun.systemtool.gamemode.util.Shared.resolutionManager
import org.sun.systemtool.gamemode.util.Shared.service
import org.sun.view.IDisplayResolutionListener

class DisplayResolutionChangeListener : IDisplayResolutionListener.Stub() {

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
            service.mainHandler.postDelayed({
                if (inGame) {
                    GamePanelViewController.onConfigurationChanged()
                }
            }, 500L)
        }

    override fun onDisplayResolutionChanged(width: Int, height: Int) {
        if (displayWidth != width) {
            displayWidth = width
        }
    }
}
