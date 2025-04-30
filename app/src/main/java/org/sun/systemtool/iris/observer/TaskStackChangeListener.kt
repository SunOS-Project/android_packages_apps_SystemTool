/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.iris.observer

import com.android.internal.util.sun.FullscreenTaskStackChangeListener

import org.sun.os.DebugConstants.DEBUG_SYSTEM_TOOL
import org.sun.systemtool.iris.util.Shared.service

abstract class TaskStackChangeListener : FullscreenTaskStackChangeListener(service, true) {

    init {
        setDebug(DEBUG_SYSTEM_TOOL)
        setDebugTag(TAG)
    }

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            setListening(value)
        }

    companion object {
        private const val TAG = "SystemTool::Iris::TaskStackChangeListener"
    }
}
