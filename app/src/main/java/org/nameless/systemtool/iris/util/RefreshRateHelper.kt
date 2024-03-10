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

    fun requestTempRefreshRate(refreshRate: Int) {
        logD(TAG, "requestTempRefreshRate, refreshRate: $refreshRate")
        try {
            refreshRateManager.requestMemcRefreshRate(refreshRate)
        } catch (e: RemoteException) {
            logE(TAG, "Exception on requesting temp refresh rate", e)
        }
    }

    fun restoreRefreshRate() {
        logD(TAG, "restoreRefreshRate")
        try {
            refreshRateManager.clearRequestedMemcRefreshRate()
        } catch (e: RemoteException) {
            logE(TAG, "Exception on restoring refresh rate", e)
        }
    }

    fun isInRequestedRefreshRate(): Boolean {
        try {
            return refreshRateManager.requestedMemcRefreshRate > 0
        } catch (e: RemoteException) {
            logE(TAG, "Exception on restoring refresh rate", e)
        }
        return false
    }
}
