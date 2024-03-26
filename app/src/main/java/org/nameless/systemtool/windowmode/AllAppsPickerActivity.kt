/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import java.util.Collections

import org.nameless.systemtool.R
import org.nameless.systemtool.windowmode.observer.SettingsObserver
import org.nameless.systemtool.windowmode.util.AppHolder
import org.nameless.systemtool.windowmode.util.AppInfo
import org.nameless.systemtool.windowmode.util.Config.miniWindowSystemAppsWhitelist
import org.nameless.systemtool.windowmode.util.DensityHelper
import org.nameless.systemtool.windowmode.util.DensityHelper.DisplaySize
import org.nameless.systemtool.windowmode.util.IDragOverListener
import org.nameless.systemtool.windowmode.util.IIconClickedListener
import org.nameless.systemtool.windowmode.util.Shared.isEditing
import org.nameless.systemtool.windowmode.view.AllAppsAdapter
import org.nameless.systemtool.windowmode.view.IconView
import org.nameless.systemtool.windowmode.view.PinnedAppsAdapter

open class AllAppsPickerActivity : Activity() {

    private val root by lazy { window.decorView.rootView }
    private val layoutLoading by lazy { findViewById<LinearLayout>(R.id.layout_loading)!! }
    private val listAllApps by lazy { findViewById<RecyclerView>(R.id.list_all_apps)!! }
    private val listPinnedApps by lazy { findViewById<RecyclerView>(R.id.list_pinned_apps)!! }
    private val scrollViewApps by lazy { findViewById<NestedScrollView>(R.id.scroll_view_apps)!! }
    private val textAllPinned by lazy { findViewById<TextView>(R.id.text_all_pinned)!! }
    private val textEdit by lazy { findViewById<TextView>(R.id.text_edit)!! }
    private val textNoPinned by lazy { findViewById<TextView>(R.id.text_no_pinned)!! }
    private val viewSplit by lazy { findViewById<View>(R.id.view_split)!! }

    private val pinnedAppsAdapter by lazy { PinnedAppsAdapter() }
    private val allAppsAdapter by lazy { AllAppsAdapter() }

    private val mergedAppList = mutableListOf<AppInfo>()

    private val sharedPool = RecyclerView.RecycledViewPool()

    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
                0
            )
        }

        override fun onMove(
            recyclerView: RecyclerView,
            oldHolder: RecyclerView.ViewHolder,
            targetHolder: RecyclerView.ViewHolder
        ): Boolean {
            pinnedAppsAdapter.notifyItemMoved(
                oldHolder.bindingAdapterPosition,
                targetHolder.bindingAdapterPosition
            )
            val newData = mutableListOf<Pair<Int, Int>>()
            pinnedAppsAdapter.data.forEachIndexed { index, _ ->
                val holder =
                    recyclerView.findViewHolderForAdapterPosition(index) as AppHolder?
                newData.add(Pair(holder?.hashCode ?: 0, index))
            }
            for (i in newData) {
                val sameIndex = pinnedAppsAdapter.data.indexOfFirst { i.first == it.hashCode() }
                if (sameIndex >= 0) {
                    Collections.swap(pinnedAppsAdapter.data, i.second, sameIndex)
                    recyclerView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                }
            }
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

        override fun isLongPressDragEnabled() = false

        override fun canDropOver(
            recyclerView: RecyclerView,
            current: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = true

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            listPinnedApps.itemAnimator = null
            root.post {
                savePinnedApps()
            }
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_apps_picker)
        actionBar?.elevation = 0f

        isEditing = isEditOnlyMode()
        if (isEditing) {
            enableDragItem(true)
        }

        val span = when (DensityHelper.getDisplaySize(this)) {
            in DisplaySize.SMALLEST.ordinal .. DisplaySize.SMALLER.ordinal -> 5
            in DisplaySize.SMALL.ordinal .. DisplaySize.LARGE.ordinal -> 4
            in DisplaySize.VERY_LARGE.ordinal .. DisplaySize.EXTREMELY_LARGE.ordinal -> 3
            else -> 4
        }
        listPinnedApps.apply {
            setRecycledViewPool(sharedPool)
            layoutManager = GridLayoutManager (
                this@AllAppsPickerActivity, span, LinearLayoutManager.VERTICAL, false
            )
            adapter = pinnedAppsAdapter
            itemAnimator = null
        }
        pinnedAppsAdapter.clickedListener = object : IIconClickedListener {
            override fun onIconClicked(appInfo: AppInfo) {
                IconView.sendMiniWindowBroadcast(
                    this@AllAppsPickerActivity,
                    appInfo.packageName,
                    appInfo.activityName
                )
            }

            override fun onRemovedClicked(appInfo: AppInfo, index: Int) {
                val originalIndex = mergedAppList.filterNot {
                    it != appInfo && pinnedAppsAdapter.data.contains(it)
                }.indexOf(appInfo)
                if (originalIndex < 0) {
                    return
                }
                val oldAllAppsData = allAppsAdapter.data.toMutableList()
                allAppsAdapter.data.add(originalIndex, appInfo)
                DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize(): Int {
                        return oldAllAppsData.size
                    }

                    override fun getNewListSize(): Int {
                        return allAppsAdapter.data.size
                    }

                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        return true
                    }

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        return oldAllAppsData[oldItemPosition] == allAppsAdapter.data[newItemPosition]
                    }
                }).apply {
                    dispatchUpdatesTo(allAppsAdapter)
                }

                val oldPinnedAppsData = pinnedAppsAdapter.data.toMutableList()
                pinnedAppsAdapter.data.removeAt(index)
                root.post {
                    savePinnedApps()
                }
                DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize(): Int {
                        return oldPinnedAppsData.size
                    }

                    override fun getNewListSize(): Int {
                        return pinnedAppsAdapter.data.size
                    }

                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        return true
                    }

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        return oldPinnedAppsData[oldItemPosition] == pinnedAppsAdapter.data[newItemPosition]
                    }
                }).apply {
                    dispatchUpdatesTo(pinnedAppsAdapter)
                }

                if (pinnedAppsAdapter.data.size == 0 || allAppsAdapter.data.size == 1) {
                    updateVisibility()
                }
            }
        }

        listAllApps.apply {
            setRecycledViewPool(sharedPool)
            layoutManager = GridLayoutManager (
                this@AllAppsPickerActivity, span, LinearLayoutManager.VERTICAL, false
            )
            adapter = allAppsAdapter
            itemAnimator = null
        }
        allAppsAdapter.clickedListener = object : IIconClickedListener {
            override fun onIconClicked(appInfo: AppInfo) {
                IconView.sendMiniWindowBroadcast(
                    this@AllAppsPickerActivity,
                    appInfo.packageName,
                    appInfo.activityName
                )
            }

            override fun onAddClicked(appInfo: AppInfo) {
                allAppsAdapter.data.indexOf(appInfo).let { idx ->
                    if (idx < 0) {
                        return
                    }
                    val oldAllAppsData = allAppsAdapter.data.toMutableList()
                    allAppsAdapter.data.removeAt(idx)
                    DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                        override fun getOldListSize(): Int {
                            return oldAllAppsData.size
                        }

                        override fun getNewListSize(): Int {
                            return allAppsAdapter.data.size
                        }

                        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                            return true
                        }

                        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                            return oldAllAppsData[oldItemPosition] == allAppsAdapter.data[newItemPosition]
                        }
                    }).apply {
                        dispatchUpdatesTo(allAppsAdapter)
                    }

                    val oldPinnedAppsData = pinnedAppsAdapter.data.toMutableList()
                    appInfo.copy().let {
                        pinnedAppsAdapter.data.add(it)
                    }
                    root.post {
                        savePinnedApps()
                    }
                    DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                        override fun getOldListSize(): Int {
                            return oldPinnedAppsData.size
                        }

                        override fun getNewListSize(): Int {
                            return pinnedAppsAdapter.data.size
                        }

                        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                            return true
                        }

                        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                            return oldPinnedAppsData[oldItemPosition] == pinnedAppsAdapter.data[newItemPosition]
                        }
                    }).apply {
                        dispatchUpdatesTo(pinnedAppsAdapter)
                    }

                    if (allAppsAdapter.data.size == 0 || pinnedAppsAdapter.data.size == 1) {
                        updateVisibility()
                    }
                }
            }
        }

        textEdit.isVisible = !isEditOnlyMode()
        textEdit.setOnClickListener {
            isEditing = !isEditing

            root.post {
                if (isEditing) {
                    enableDragItem(true)
                    pinnedAppsAdapter.data.forEachIndexed { index, _ ->
                        val holder =
                            listPinnedApps.findViewHolderForAdapterPosition(index) as AppHolder?
                        holder?.iconState?.visibility = View.VISIBLE
                    }
                    allAppsAdapter.data.forEachIndexed { index, _ ->
                        val holder =
                            listAllApps.findViewHolderForAdapterPosition(index) as AppHolder?
                        holder?.iconState?.visibility = View.VISIBLE
                    }
                    textEdit.text = getString(R.string.finish_title)
                } else {
                    enableDragItem(false)
                    pinnedAppsAdapter.data.forEachIndexed { index, _ ->
                        val holder =
                            listPinnedApps.findViewHolderForAdapterPosition(index) as AppHolder?
                        holder?.iconState?.visibility = View.INVISIBLE
                    }
                    allAppsAdapter.data.forEachIndexed { index, _ ->
                        val holder =
                            listAllApps.findViewHolderForAdapterPosition(index) as AppHolder?
                        holder?.iconState?.visibility = View.INVISIBLE
                    }
                    textEdit.text = getString(R.string.edit_title)
                }
            }
        }

        root.post {
            reloadApps()
        }
    }

    open fun isEditOnlyMode() = false

    private fun reloadApps() {
        scrollViewApps.post {
            scrollViewApps.isVisible = false
        }
        layoutLoading.post {
            layoutLoading.isVisible = true
        }

        val pinnedAppsList = mutableListOf<AppInfo>()
        val allAppsList = mutableListOf<AppInfo>()
        val pinnedAppsSettings = SettingsObserver.getMiniWindowAppsSettings(this)
            ?.takeIf { it.isNotBlank() }?.split(";")?.toSet() ?: emptySet()
        val apps = packageManager.getInstalledPackages(0).filter {
            val isSystemApp = it.applicationInfo?.flags?.let { flags ->
                (flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            } ?: false
            miniWindowSystemAppsWhitelist.contains(it.packageName) || !isSystemApp
        }.toMutableList()
        pinnedAppsSettings.forEach { packageName ->
            apps.find { app -> app.packageName == packageName }?.let {
                AppInfo(
                    it.applicationInfo?.loadLabel(packageManager)?.toString() ?: String(),
                    it.packageName,
                    String(),
                    it.applicationInfo?.loadIcon(packageManager) ?: packageManager.defaultActivityIcon
                ).let { info ->
                    pinnedAppsList.add(info)
                    mergedAppList.add(info)
                }
                apps.remove(it)
            }
        }
        apps.forEach {
            AppInfo(
                it.applicationInfo?.loadLabel(packageManager)?.toString() ?: String(),
                it.packageName,
                String(),
                it.applicationInfo?.loadIcon(packageManager) ?: packageManager.defaultActivityIcon
            ).let { info ->
                allAppsList.add(info)
                mergedAppList.add(info)
            }
        }

        allAppsList.sortWith { o1, o2 ->
            if (o1.label == o2.label) {
                o1.packageName.compareTo(o2.packageName)
            } else {
                o1.label.compareTo(o2.label)
            }
        }
        mergedAppList.sortWith { o1, o2 ->
            if (o1.label == o2.label) {
                o1.packageName.compareTo(o2.packageName)
            } else {
                o1.label.compareTo(o2.label)
            }
        }

        pinnedAppsAdapter.data = pinnedAppsList.toMutableList()
        allAppsAdapter.data = allAppsList.toMutableList()

        updateVisibility()

        scrollViewApps.post {
            scrollViewApps.scrollTo(0, 0)
        }
        layoutLoading.post {
            layoutLoading.isVisible = false
        }
        scrollViewApps.post {
            scrollViewApps.isVisible = true
        }
    }

    private fun enableDragItem(enable: Boolean) {
        if (enable) {
            pinnedAppsAdapter.dragOverListener = object : IDragOverListener {
                override fun startDragItem(holder: RecyclerView.ViewHolder) {
                    listPinnedApps.itemAnimator = DefaultItemAnimator()
                    itemTouchHelper.startDrag(holder)
                }
            }
            itemTouchHelper.attachToRecyclerView(listPinnedApps)
        } else {
            pinnedAppsAdapter.dragOverListener = null
            itemTouchHelper.attachToRecyclerView(null)
        }
    }

    private fun updateVisibility() {
        if (pinnedAppsAdapter.data.size == 0) {
            listPinnedApps.isVisible = false
            textNoPinned.isVisible = true
            (viewSplit.layoutParams as ConstraintLayout.LayoutParams).let {
                it.bottomToBottom = R.id.text_no_pinned
                viewSplit.layoutParams = it
            }
        } else {
            listPinnedApps.isVisible = true
            textNoPinned.isVisible = false
            (viewSplit.layoutParams as ConstraintLayout.LayoutParams).let {
                it.bottomToBottom = R.id.list_pinned_apps
                viewSplit.layoutParams = it
            }
        }

        if (allAppsAdapter.data.size == 0) {
            listAllApps.isVisible = false
            textAllPinned.isVisible = true
        } else {
            listAllApps.isVisible = true
            textAllPinned.isVisible = false
        }
    }

    private fun savePinnedApps() {
        SettingsObserver.putMiniWindowAppsSettings(this,
            pinnedAppsAdapter.data.joinToString(";") { it.packageName }
        )
    }
}
