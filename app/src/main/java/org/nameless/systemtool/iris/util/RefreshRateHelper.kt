/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.util

import android.os.RemoteException

import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.common.Utils.logE
import org.nameless.systemtool.iris.util.Shared.refreshRateManager

object RefreshRateHelper {

    private const val TAG = "SystemTool::Iris::RefreshRateHelper"

    var memcRefreshRate = -1
        set(value) {
            if (field > 0 && value <= 0) {
                logD(TAG, "restoreRefreshRate")
                try {
                    refreshRateManager.clearRequestedMemcRefreshRate()
                    field = value
                } catch (e: RemoteException) {
                    logE(TAG, "Exception on restoring refresh rate", e)
                }
            } else if (field <= 0 && value > 0) {
                logD(TAG, "requestTempRefreshRate, refreshRate: $value")
                try {
                    refreshRateManager.requestMemcRefreshRate(value)
                    field = value
                } catch (e: RemoteException) {
                    logE(TAG, "Exception on requesting temp refresh rate", e)
                }
            }
        }
}
