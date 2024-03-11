/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.common

import android.util.Log

import org.nameless.os.DebugConstants

object Utils {

    const val PACKAGE_NAME = "org.nameless.systemtool"

    fun logD(tag: String, msg: String) {
        if (DebugConstants.DEBUG_SYSTEM_TOOL) {
            Log.d(tag, msg)
        }
    }

    fun logE(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    fun logE(tag: String, msg: String, e: Exception) {
        Log.e(tag, msg, e)
    }
}
