/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.view

import android.graphics.drawable.Drawable

import org.nameless.systemtool.windowmode.util.PackageInfoCache

data class GridItem(
    val packageName: String
) {
    val icon: Drawable? = PackageInfoCache.getIconDrawable(packageName)
    val label: String = PackageInfoCache.getPackageLabel(packageName)
}
