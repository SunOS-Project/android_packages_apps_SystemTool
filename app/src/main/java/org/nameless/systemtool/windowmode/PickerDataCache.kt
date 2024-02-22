/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode

import org.nameless.systemtool.windowmode.util.PackageInfoCache
import org.nameless.systemtool.windowmode.view.GridItem

object PickerDataCache {

    var pinnedPackages: MutableSet<String> = mutableSetOf()

    val pinnedAppsItems: MutableList<GridItem> = mutableListOf()
    val allAppsItems: MutableList<GridItem> = mutableListOf()

    fun updatePinnedPackages(packages: MutableSet<String>) {
        pinnedPackages = packages

        pinnedAppsItems.clear()
        pinnedPackages.forEach {
            pinnedAppsItems.add(GridItem(it))
        }

        allAppsItems.clear()
        PackageInfoCache.availablePackages.filterNot { pinnedPackages.contains(it) }.forEach {
                allAppsItems.add(GridItem(it))
            }
    }

    fun onAvailablePackagesChanged() {
        allAppsItems.clear()
        PackageInfoCache.availablePackages.filterNot {
            pinnedPackages.contains(it) }.forEach {
                allAppsItems.add(GridItem(it))
            }
    }
}
