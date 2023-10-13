/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge

import android.os.Bundle
import android.os.PowerManager
import android.widget.GridView
import android.widget.TextView

import androidx.core.view.isVisible

import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity

import org.nameless.edge.R
import org.nameless.edge.observer.SettingsObserver
import org.nameless.edge.util.DensityHelper
import org.nameless.edge.util.DensityHelper.DisplaySize
import org.nameless.edge.util.PackageInfoCache
import org.nameless.edge.view.GridItem
import org.nameless.edge.view.GridItemAdapter
import org.nameless.edge.view.IconView

open class AllAppsPickerActivity : CollapsingToolbarBaseActivity() {

    private var pinnedAppsGridView: GridView? = null
    private var allAppsGridView: GridView? = null

    private var noPinnedAppsText: TextView? = null
    private var allAppsPinnedText: TextView? = null

    private var pinnedAppsAdapter: GridItemAdapter<GridItem>? = null
    private var allAppsAdapter: GridItemAdapter<GridItem>? = null

    private var pinnedPackages: MutableSet<String>? = null

    private var pinnedAppSize = 0
    private var allAppsSize = 0

    private var powerManager: PowerManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_apps_picker)

        powerManager = getSystemService(PowerManager::class.java)

        pinnedPackages = PickerDataCache.pinnedPackages?.toMutableSet() ?: mutableSetOf<String>()

        pinnedAppsGridView = findViewById(R.id.grid_pinned_apps)
        pinnedAppsAdapter = object : GridItemAdapter<GridItem>(
            PickerDataCache.pinnedAppsItems, R.layout.grid_item) {
                override fun bindView(holder: AdapterViewHolder?, obj: GridItem) {
                    holder?.setDrawable(R.id.item_icon, obj.icon)
                    holder?.setText(R.id.item_label, obj.label)
                }
        }
        pinnedAppsGridView?.adapter = pinnedAppsAdapter
        pinnedAppSize = PickerDataCache.pinnedAppsItems.size
        if (allowStartApp()) {
            pinnedAppsGridView?.setOnItemClickListener { _, _, position, _ ->
                IconView.sendMiniWindowBroadcast(this,
                    PickerDataCache.pinnedAppsItems[position].packageName)
                finish()
            }
        }
        pinnedAppsGridView?.setOnItemLongClickListener { _, _, position, _ ->
            pinnedAppsAdapter?.remove(position)?.let { item ->
                pinnedPackages?.remove(item.packageName)
                val prevIdx = PackageInfoCache.availablePackages.filterNot {
                    pinnedPackages?.contains(it) ?: false }.indexOf(item.packageName)
                if (prevIdx != -1) {
                    allAppsAdapter?.add(item, prevIdx)
                } else {
                    allAppsAdapter?.add(item)
                }
                ++allAppsSize
                --pinnedAppSize
            }
            updateViewsVisibility()
            updatePinnedAppsSettings()
            true
        }

        allAppsGridView = findViewById(R.id.grid_all_apps)
        allAppsAdapter = object : GridItemAdapter<GridItem>(
            PickerDataCache.allAppsItems, R.layout.grid_item) {
                override fun bindView(holder: AdapterViewHolder?, obj: GridItem) {
                    holder?.setDrawable(R.id.item_icon, obj.icon)
                    holder?.setText(R.id.item_label, obj.label)
                }
        }
        allAppsGridView?.adapter = allAppsAdapter
        allAppsSize = PickerDataCache.allAppsItems.size
        if (allowStartApp()) {
            allAppsGridView?.setOnItemClickListener { _, _, position, _ ->
                IconView.sendMiniWindowBroadcast(this,
                    PickerDataCache.allAppsItems[position].packageName)
                finish()
            }
        }
        allAppsGridView?.setOnItemLongClickListener { _, _, position, _ ->
            allAppsAdapter?.remove(position)?.let {
                pinnedAppsAdapter?.add(it)
                pinnedPackages?.add(it.packageName)
                ++pinnedAppSize
                --allAppsSize
            }
            updateViewsVisibility()
            updatePinnedAppsSettings()
            true
        }

        when (DensityHelper.getDisplaySize(this)) {
            in DisplaySize.SMALLEST.ordinal .. DisplaySize.SMALLER.ordinal -> 5
            in DisplaySize.SMALL.ordinal .. DisplaySize.LARGE.ordinal -> 4
            in DisplaySize.VERY_LARGE.ordinal .. DisplaySize.EXTREMELY_LARGE.ordinal -> 3
            else -> 4
        }.let {
            pinnedAppsGridView?.numColumns = it
            allAppsGridView?.numColumns = it
        }

        noPinnedAppsText = findViewById(R.id.text_no_pined_app)
        allAppsPinnedText = findViewById(R.id.text_all_apps_pinned)
        updateViewsVisibility()
    }

    override fun onStop() {
        super.onStop()
        if (finishOnStop() && !(powerManager?.isInteractive() ?: true)) {
            finish()
        }
    }

    open fun allowStartApp() = true

    open fun finishOnStop() = true

    private fun updatePinnedAppsSettings() {
        SettingsObserver.putMiniWindowAppsSettings(this, (pinnedPackages?: emptySet()).joinToString(";"))
    }

    private fun updateViewsVisibility() {
        noPinnedAppsText?.isVisible = pinnedAppSize <= 0
        pinnedAppsGridView?.isVisible = pinnedAppSize > 0

        allAppsPinnedText?.isVisible = allAppsSize <= 0
        allAppsGridView?.isVisible = allAppsSize > 0
    }
}
