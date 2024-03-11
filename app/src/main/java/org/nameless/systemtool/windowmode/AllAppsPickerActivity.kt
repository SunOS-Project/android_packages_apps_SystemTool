/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode

import android.app.Activity
import android.os.Bundle
import android.os.PowerManager
import android.widget.GridView
import android.widget.TextView

import androidx.core.view.isVisible

import org.nameless.systemtool.R
import org.nameless.systemtool.windowmode.observer.SettingsObserver
import org.nameless.systemtool.windowmode.util.DensityHelper
import org.nameless.systemtool.windowmode.util.DensityHelper.DisplaySize
import org.nameless.systemtool.windowmode.util.PackageInfoCache
import org.nameless.systemtool.windowmode.view.GridItem
import org.nameless.systemtool.windowmode.view.GridItemAdapter
import org.nameless.systemtool.windowmode.view.IconView

open class AllAppsPickerActivity : Activity() {

    private val pinnedAppsGridView: GridView by lazy { findViewById(R.id.grid_pinned_apps) }
    private val allAppsGridView: GridView by lazy { findViewById(R.id.grid_all_apps) }

    private val noPinnedAppsText: TextView by lazy { findViewById(R.id.text_no_pined_app) }
    private val allAppsPinnedText: TextView by lazy { findViewById(R.id.text_all_apps_pinned) }

    private val pinnedAppsAdapter = object : GridItemAdapter<GridItem>(
        PickerDataCache.allAppsItems, R.layout.grid_item
    ) {
        override fun bindView(holder: AdapterViewHolder?, obj: GridItem) {
            holder?.setDrawable(R.id.item_icon, obj.icon)
            holder?.setText(R.id.item_label, obj.label)
        }
    }
    private val allAppsAdapter = object : GridItemAdapter<GridItem>(
        PickerDataCache.allAppsItems, R.layout.grid_item
    ) {
        override fun bindView(holder: AdapterViewHolder?, obj: GridItem) {
            holder?.setDrawable(R.id.item_icon, obj.icon)
            holder?.setText(R.id.item_label, obj.label)
        }
    }

    private var pinnedPackages = mutableSetOf<String>()

    private var pinnedAppSize = 0
    private var allAppsSize = 0

    private val powerManager by lazy { getSystemService(PowerManager::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_apps_picker)

        pinnedPackages = PickerDataCache.pinnedPackages.toMutableSet()

        pinnedAppsGridView.adapter = pinnedAppsAdapter
        pinnedAppSize = PickerDataCache.pinnedAppsItems.size
        if (allowStartApp()) {
            pinnedAppsGridView.setOnItemClickListener { _, _, position, _ ->
                IconView.sendMiniWindowBroadcast(this,
                    PickerDataCache.pinnedAppsItems[position].packageName)
                finish()
            }
        }
        pinnedAppsGridView.setOnItemLongClickListener { _, _, position, _ ->
            pinnedAppsAdapter.remove(position).let { item ->
                pinnedPackages.remove(item.packageName)
                val prevIdx = PackageInfoCache.availablePackages.filterNot {
                    pinnedPackages.contains(it) }.indexOf(item.packageName)
                if (prevIdx != -1) {
                    allAppsAdapter.add(item, prevIdx)
                } else {
                    allAppsAdapter.add(item)
                }
                ++allAppsSize
                --pinnedAppSize
            }
            updateViewsVisibility()
            updatePinnedAppsSettings()
            true
        }

        allAppsGridView.adapter = allAppsAdapter
        allAppsSize = PickerDataCache.allAppsItems.size
        if (allowStartApp()) {
            allAppsGridView.setOnItemClickListener { _, _, position, _ ->
                IconView.sendMiniWindowBroadcast(this,
                    PickerDataCache.allAppsItems[position].packageName)
                finish()
            }
        }
        allAppsGridView.setOnItemLongClickListener { _, _, position, _ ->
            allAppsAdapter.remove(position).let {
                pinnedAppsAdapter.add(it)
                pinnedPackages.add(it.packageName)
                ++pinnedAppSize
                --allAppsSize
            }
            updateViewsVisibility()
            updatePinnedAppsSettings()
            true
        }

        when (DensityHelper.displaySize) {
            in DisplaySize.SMALLEST.ordinal .. DisplaySize.SMALLER.ordinal -> 5
            in DisplaySize.SMALL.ordinal .. DisplaySize.LARGE.ordinal -> 4
            in DisplaySize.VERY_LARGE.ordinal .. DisplaySize.EXTREMELY_LARGE.ordinal -> 3
            else -> 4
        }.let {
            pinnedAppsGridView.numColumns = it
            allAppsGridView.numColumns = it
        }

        updateViewsVisibility()
    }

    override fun onStop() {
        super.onStop()
        if (finishOnStop() && !powerManager.isInteractive) {
            finish()
        }
    }

    open fun allowStartApp() = true

    open fun finishOnStop() = true

    private fun updatePinnedAppsSettings() {
        SettingsObserver.putMiniWindowAppsSettings(this, pinnedPackages.joinToString(";"))
    }

    private fun updateViewsVisibility() {
        noPinnedAppsText.isVisible = pinnedAppSize <= 0
        pinnedAppsGridView.isVisible = pinnedAppSize > 0

        allAppsPinnedText.isVisible = allAppsSize <= 0
        allAppsGridView.isVisible = allAppsSize > 0
    }
}
