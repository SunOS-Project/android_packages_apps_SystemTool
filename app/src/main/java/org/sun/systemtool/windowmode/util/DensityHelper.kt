/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.windowmode.util

import android.content.Context

import com.android.settingslib.display.DisplayDensityUtils

object DensityHelper {

    enum class DisplaySize {
        SMALLEST,
        SMALLER,
        SMALL,
        NORMAL,
        LARGE,
        VERY_LARGE,
        EXTREMELY_LARGE
    }

    fun getDisplaySize(context: Context): Int {
        return DisplayDensityUtils(context).currentIndexForDefaultDisplay
    }
}
