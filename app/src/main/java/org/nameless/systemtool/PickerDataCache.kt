/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool

import org.nameless.systemtool.util.PackageInfoCache
import org.nameless.systemtool.view.GridItem
import org.nameless.systemtool.view.GridItemAdapter

object PickerDataCache {

    var pinnedPackages: MutableSet<String>? = null

    val pinnedAppsItems: MutableList<GridItem> = mutableListOf()
    val allAppsItems: MutableList<GridItem> = mutableListOf()

    fun updatePinnedPackages(packages: MutableSet<String>) {
        pinnedPackages = packages

        pinnedAppsItems.clear()
        pinnedPackages?.forEach {
            pinnedAppsItems.add(GridItem(it))
        }

        allAppsItems.clear()
        PackageInfoCache.availablePackages.filterNot {
            pinnedPackages?.contains(it)?: false }.forEach {
                allAppsItems.add(GridItem(it))
            }
    }

    fun onAvailablePackagesChanged() {
        allAppsItems.clear()
        PackageInfoCache.availablePackages.filterNot {
            pinnedPackages?.contains(it)?: false }.forEach {
                allAppsItems.add(GridItem(it))
            }
    }
}
