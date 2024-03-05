/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.onlineconfig.util

object Constants {

    const val INTENT_DEBUG_MODE = "org.nameless.systemtool.intent.ONLINE_CONFIG_DEBUG_MODE"
    const val EXTRA_DEBUG_MODE_STATE = "state"

    // Update interval on debug mode
    const val DEBUG_MODE_UPDATE_INTERVAL = 60  // 1 minute

    // Update interval normally
    const val UPDATE_INTERVAL = 60 * 60 * 12  // 12 hours

    // Update interval when last update is intercepted
    const val UPDATE_INTERCEPTED_INTERVAL = 60 * 10  // 10 minutes

    // Delay time for updating once boot completed
    const val BOOT_COMPLETED_UPDATE_DELAY = 30  // 30 seconds

    // Delay time for updating once wifi becomes available
    const val WIFI_AVAILABLE_UPDATE_DELAY = 10  // 10 seconds
}
