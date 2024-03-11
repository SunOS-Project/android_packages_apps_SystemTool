/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.util

import com.android.settingslib.display.DisplayDensityUtils

import org.nameless.systemtool.windowmode.util.Shared.service

object DensityHelper {

    private val densityUtils by lazy { DisplayDensityUtils(service) }

    enum class DisplaySize {
        SMALLEST,
        SMALLER,
        SMALL,
        NORMAL,
        LARGE,
        VERY_LARGE,
        EXTREMELY_LARGE
    }

    var displaySize = 0
        get() = densityUtils.currentIndexForDefaultDisplay
}
