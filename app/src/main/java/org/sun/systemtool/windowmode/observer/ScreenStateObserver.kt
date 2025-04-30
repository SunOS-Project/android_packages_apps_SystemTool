/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.windowmode.observer

import android.os.Handler

import com.android.internal.util.sun.ScreenStateListener

import org.sun.systemtool.windowmode.ViewAnimator
import org.sun.systemtool.windowmode.util.Shared.service

class ScreenStateObserver(
    handler: Handler
) : ScreenStateListener(service, handler) {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            setListening(value)
        }

    override fun onScreenOff() {
        ViewAnimator.allowVisible = false
    }

    override fun onScreenOn() {}

    override fun onScreenUnlocked() {
        ViewAnimator.allowVisible = true
    }
}
