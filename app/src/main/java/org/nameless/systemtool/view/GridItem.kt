/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.view

import android.content.Context
import android.graphics.drawable.Drawable

import org.nameless.systemtool.util.PackageInfoCache

data class GridItem constructor(
    val packageName: String
) {

    val icon: Drawable?
    val label: String

    init {
        icon = PackageInfoCache.getIconDrawable(packageName)
        label = PackageInfoCache.getPackageLabel(packageName)
    }
}
