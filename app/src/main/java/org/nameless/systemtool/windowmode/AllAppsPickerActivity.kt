/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode

import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.os.Bundle
import android.os.VibrationExtInfo
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity

import java.util.Collections

import org.nameless.systemtool.R
import org.nameless.systemtool.common.BroadcastSender
import org.nameless.systemtool.common.IconDrawableHelper
import org.nameless.systemtool.common.ShortcutHelper
import org.nameless.systemtool.windowmode.bean.AppInfo
import org.nameless.systemtool.windowmode.callback.IDragOverListener
import org.nameless.systemtool.windowmode.callback.IIconClickedListener
import org.nameless.systemtool.windowmode.observer.SettingsObserver
import org.nameless.systemtool.windowmode.util.AdapterDifferHelper
import org.nameless.systemtool.windowmode.util.Config.miniWindowSystemAppsWhitelist
import org.nameless.systemtool.windowmode.util.Config.shortcutSystemAppsBlacklist
import org.nameless.systemtool.windowmode.util.DensityHelper
import org.nameless.systemtool.windowmode.util.DensityHelper.DisplaySize
import org.nameless.systemtool.windowmode.util.Shared.isEditing
import org.nameless.systemtool.windowmode.view.AllItemAdapter
import org.nameless.systemtool.windowmode.view.AppHolder
import org.nameless.systemtool.windowmode.view.PinnedItemAdapter

import vendor.nameless.hardware.vibratorExt.V1_0.Effect.INDEXABLE_WIDGET
import vendor.nameless.hardware.vibratorExt.V1_0.Effect.TICK

open class AllAppsPickerActivity : CollapsingToolbarBaseActivity() {

    private val root by lazy { window.decorView.rootView }
    private val indicatorLoading by lazy { findViewById<ProgressBar>(R.id.indicator_loading)!! }
    private val listAllApps by lazy { findViewById<RecyclerView>(R.id.list_all_apps)!! }
    private val listAllShortcuts by lazy { findViewById<RecyclerView>(R.id.list_all_shortcuts)!! }
    private val listPinnedApps by lazy { findViewById<RecyclerView>(R.id.list_pinned_apps)!! }
    private val scrollViewApps by lazy { findViewById<NestedScrollView>(R.id.scroll_view_apps)!! }
    private val textAllAppPinned by lazy { findViewById<TextView>(R.id.text_all_app_pinned)!! }
    private val textAllShortcutPinned by lazy { findViewById<TextView>(R.id.text_all_shortcut_pinned)!! }
    private val textEdit by lazy { findViewById<TextView>(R.id.text_edit)!! }
    private val textNoPinned by lazy { findViewById<TextView>(R.id.text_no_pinned)!! }
    private val viewSplitApp by lazy { findViewById<View>(R.id.view_split_app)!! }
    private val viewSplitShortcut by lazy { findViewById<View>(R.id.view_split_shortcut)!! }

    private val pinnedItemAdapter by lazy { PinnedItemAdapter() }
    private val allAppAdapter by lazy { AllItemAdapter() }
    private val allShortcutAdapter by lazy { AllItemAdapter() }

    private val mergedAppList = mutableListOf<AppInfo>()
    private val mergedShortcutList = mutableListOf<AppInfo>()

    private val sharedPool = RecyclerView.RecycledViewPool()

    private val launcherApps by lazy {
        getSystemService(LauncherApps::class.java)!!
    }

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
            pinnedItemAdapter.notifyItemMoved(
                oldHolder.bindingAdapterPosition,
                targetHolder.bindingAdapterPosition
            )
            val newData = mutableListOf<Pair<Int, Int>>()
            pinnedItemAdapter.data.forEachIndexed { index, _ ->
                val holder =
                    recyclerView.findViewHolderForAdapterPosition(index) as AppHolder?
                newData.add(Pair(holder?.hashCode ?: 0, index))
            }
            recyclerView.performHapticFeedbackExt(VibrationExtInfo.Builder().apply {
                setEffectId(INDEXABLE_WIDGET)
                setFallbackEffectId(TICK)
            }.build())
            for (i in newData) {
                val sameIndex = pinnedItemAdapter.data.indexOfFirst { i.first == it.hashCode() }
                if (sameIndex >= 0) {
                    Collections.swap(pinnedItemAdapter.data, i.second, sameIndex)
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
            savePinnedApps()
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_apps_picker)

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
            adapter = pinnedItemAdapter
            itemAnimator = null
        }
        pinnedItemAdapter.clickedListener = object : IIconClickedListener {
            override fun onIconClicked(appInfo: AppInfo) {
                if (appInfo.shortcutInfo != null) {
                    ShortcutHelper.startShortcut(
                        this@AllAppsPickerActivity,
                        appInfo.shortcutInfo
                    )
                    return
                }
                BroadcastSender.sendStartPackageBroadcast(
                    this@AllAppsPickerActivity,
                    appInfo.packageName
                )
            }

            override fun onRemovedClicked(appInfo: AppInfo, index: Int) {
                val originalIndex = if (appInfo.shortcutInfo != null) {
                    mergedShortcutList
                } else {
                    mergedAppList
                }.filterNot {
                    it != appInfo && pinnedItemAdapter.data.contains(it)
                }.indexOf(appInfo)
                if (originalIndex < 0) {
                    return
                }
                val adapter = if (appInfo.shortcutInfo != null) {
                    allShortcutAdapter
                } else {
                    allAppAdapter
                }
                val oldAllData = adapter.data.toMutableList()
                adapter.data.add(originalIndex, appInfo)
                AdapterDifferHelper(oldAllData, adapter).start()

                val oldPinnedData = pinnedItemAdapter.data.toMutableList()
                pinnedItemAdapter.data.removeAt(index)
                savePinnedApps()
                AdapterDifferHelper(oldPinnedData, pinnedItemAdapter).start()

                if (pinnedItemAdapter.data.size == 0 || adapter.data.size == 1) {
                    updateVisibility()
                }
            }
        }

        listAllApps.apply {
            setRecycledViewPool(sharedPool)
            layoutManager = GridLayoutManager (
                this@AllAppsPickerActivity, span, LinearLayoutManager.VERTICAL, false
            )
            adapter = allAppAdapter
            itemAnimator = null
        }
        allAppAdapter.clickedListener = object : IIconClickedListener {
            override fun onIconClicked(appInfo: AppInfo) {
                BroadcastSender.sendStartPackageBroadcast(
                    this@AllAppsPickerActivity,
                    appInfo.packageName
                )
            }

            override fun onAddClicked(appInfo: AppInfo) {
                allAppAdapter.data.indexOf(appInfo).let { idx ->
                    if (idx < 0) {
                        return
                    }
                    val oldAllData = allAppAdapter.data.toMutableList()
                    allAppAdapter.data.removeAt(idx)
                    AdapterDifferHelper(oldAllData, allAppAdapter).start()

                    val oldPinnedData = pinnedItemAdapter.data.toMutableList()
                    pinnedItemAdapter.data.add(appInfo)
                    savePinnedApps()
                    AdapterDifferHelper(oldPinnedData, pinnedItemAdapter).start()

                    if (allAppAdapter.data.size == 0 || pinnedItemAdapter.data.size == 1) {
                        updateVisibility()
                    }
                }
            }
        }

        listAllShortcuts.apply {
            setRecycledViewPool(sharedPool)
            layoutManager = GridLayoutManager (
                this@AllAppsPickerActivity, span, LinearLayoutManager.VERTICAL, false
            )
            adapter = allShortcutAdapter
            itemAnimator = null
        }
        allShortcutAdapter.clickedListener = object : IIconClickedListener {
            override fun onIconClicked(appInfo: AppInfo) {
                if (appInfo.shortcutInfo != null) {
                    ShortcutHelper.startShortcut(
                        this@AllAppsPickerActivity,
                        appInfo.shortcutInfo
                    )
                }
            }

            override fun onAddClicked(appInfo: AppInfo) {
                allShortcutAdapter.data.indexOf(appInfo).let { idx ->
                    if (idx < 0) {
                        return
                    }
                    val oldAllData = allShortcutAdapter.data.toMutableList()
                    allShortcutAdapter.data.removeAt(idx)
                    AdapterDifferHelper(oldAllData, allShortcutAdapter).start()

                    val oldPinnedData = pinnedItemAdapter.data.toMutableList()
                    pinnedItemAdapter.data.add(appInfo)
                    savePinnedApps()
                    AdapterDifferHelper(oldPinnedData, pinnedItemAdapter).start()

                    if (allShortcutAdapter.data.size == 0 || pinnedItemAdapter.data.size == 1) {
                        updateVisibility()
                    }
                }
            }
        }

        textEdit.isVisible = !isEditOnlyMode()
        textEdit.setOnClickListener {
            isEditing = !isEditing

            if (isEditing) {
                enableDragItem(true)
                pinnedItemAdapter.data.forEachIndexed { index, _ ->
                    val holder =
                        listPinnedApps.findViewHolderForAdapterPosition(index) as AppHolder?
                    holder?.iconState?.visibility = View.VISIBLE
                }
                allAppAdapter.data.forEachIndexed { index, _ ->
                    val holder =
                        listAllApps.findViewHolderForAdapterPosition(index) as AppHolder?
                    holder?.iconState?.visibility = View.VISIBLE
                }
                allShortcutAdapter.data.forEachIndexed { index, _ ->
                    val holder =
                        listAllShortcuts.findViewHolderForAdapterPosition(index) as AppHolder?
                    holder?.iconState?.visibility = View.VISIBLE
                }
                textEdit.text = getString(R.string.finish_title)
            } else {
                enableDragItem(false)
                pinnedItemAdapter.data.forEachIndexed { index, _ ->
                    val holder =
                        listPinnedApps.findViewHolderForAdapterPosition(index) as AppHolder?
                    holder?.iconState?.visibility = View.INVISIBLE
                }
                allAppAdapter.data.forEachIndexed { index, _ ->
                    val holder =
                        listAllApps.findViewHolderForAdapterPosition(index) as AppHolder?
                    holder?.iconState?.visibility = View.INVISIBLE
                }
                allShortcutAdapter.data.forEachIndexed { index, _ ->
                    val holder =
                        listAllShortcuts.findViewHolderForAdapterPosition(index) as AppHolder?
                    holder?.iconState?.visibility = View.INVISIBLE
                }
                textEdit.text = getString(R.string.edit_title)
            }
        }

        root.post {
            reloadApps()
        }
    }

    open fun isEditOnlyMode() = false

    private fun reloadApps() {
        scrollViewApps.visibility = View.INVISIBLE
        indicatorLoading.visibility = View.VISIBLE

        val pinnedItemList = mutableListOf<AppInfo>()
        val allAppsList = mutableListOf<AppInfo>()
        val allShortcutList = mutableListOf<AppInfo>()
        val pinnedAppsSettings = SettingsObserver.getMiniWindowAppsSettings(this)
            ?.takeIf { it.isNotBlank() }?.split(";")?.toSet() ?: emptySet()
        val apps = packageManager.getInstalledPackages(0).filter {
            val isSystemApp = it.applicationInfo?.flags?.let { flags ->
                (flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            } ?: false
            miniWindowSystemAppsWhitelist.contains(it.packageName) || !isSystemApp
        }.map {
            val label = it.applicationInfo?.loadLabel(packageManager)?.toString() ?: String()
            AppInfo(
                label,
                label,
                it.packageName,
                IconDrawableHelper.getDrawable(this, it.applicationInfo)
            )
        }.toMutableList()
        val shortcuts = mutableListOf<AppInfo>()
        apps.map { Pair(it.packageName, it.compareLabel) }.filterNot {
            shortcutSystemAppsBlacklist.contains(it.first)
        }.forEach { p ->
            ShortcutHelper.getShortcuts(launcherApps, p.first)?.forEach {
                shortcuts.add(
                    AppInfo(
                        it.shortLabel.toString(),
                        p.second,
                        it.`package`,
                        IconDrawableHelper.getDrawable(this, launcherApps, it),
                        it
                    )
                )
            }
        }
        pinnedAppsSettings.forEach { setting ->
            val packageName: String
            val shortcutId: String
            val shortcutUserId: Int
            setting.split(":").let {
                packageName = it[0]
                shortcutId = if (it.size == 3) it[1] else String()
                shortcutUserId = if (it.size == 3) it[2].toInt() else Int.MIN_VALUE
            }
            if (shortcutId.isNotBlank() && shortcutUserId != Int.MIN_VALUE) {
                shortcuts.find { shortcut ->
                    shortcut.packageName == packageName &&
                    shortcut.shortcutInfo?.id == shortcutId &&
                    shortcut.shortcutInfo.userId == shortcutUserId
                }?.let { info ->
                    pinnedItemList.add(info)
                    mergedShortcutList.add(info)
                    shortcuts.remove(info)
                }
            } else {
                apps.find { app -> app.packageName == packageName }?.let { info ->
                    pinnedItemList.add(info)
                    mergedAppList.add(info)
                    apps.remove(info)
                }
            }
        }
        apps.forEach { info ->
            allAppsList.add(info)
            mergedAppList.add(info)
        }
        shortcuts.forEach { info ->
            allShortcutList.add(info)
            mergedShortcutList.add(info)
        }

        allAppsList.sortWith { o1, o2 ->
            if (o1.compareLabel == o2.compareLabel) {
                o1.packageName.compareTo(o2.packageName)
            } else {
                o1.compareLabel.compareTo(o2.compareLabel)
            }
        }
        mergedAppList.sortWith { o1, o2 ->
            if (o1.compareLabel == o2.compareLabel) {
                o1.packageName.compareTo(o2.packageName)
            } else {
                o1.compareLabel.compareTo(o2.compareLabel)
            }
        }
        allShortcutList.sortWith { o1, o2 ->
            if (o1.compareLabel == o2.compareLabel) {
                o1.label.compareTo(o2.label)
            } else {
                o1.compareLabel.compareTo(o2.compareLabel)
            }
        }
        mergedShortcutList.sortWith { o1, o2 ->
            if (o1.compareLabel == o2.compareLabel) {
                o1.label.compareTo(o2.label)
            } else {
                o1.compareLabel.compareTo(o2.compareLabel)
            }
        }

        pinnedItemAdapter.data = pinnedItemList.toMutableList()
        allAppAdapter.data = allAppsList.toMutableList()
        allShortcutAdapter.data = allShortcutList.toMutableList()

        updateVisibility()

        indicatorLoading.visibility = View.INVISIBLE
        scrollViewApps.scrollTo(0, 0)
        scrollViewApps.visibility = View.VISIBLE
    }

    private fun enableDragItem(enable: Boolean) {
        if (enable) {
            pinnedItemAdapter.dragOverListener = object : IDragOverListener {
                override fun startDragItem(holder: RecyclerView.ViewHolder) {
                    listPinnedApps.itemAnimator = DefaultItemAnimator()
                    itemTouchHelper.startDrag(holder)
                }
            }
            itemTouchHelper.attachToRecyclerView(listPinnedApps)
        } else {
            pinnedItemAdapter.dragOverListener = null
            itemTouchHelper.attachToRecyclerView(null)
        }
    }

    private fun updateVisibility() {
        if (pinnedItemAdapter.data.size == 0) {
            listPinnedApps.isVisible = false
            textNoPinned.isVisible = true
            (viewSplitApp.layoutParams as ConstraintLayout.LayoutParams).let {
                it.topToBottom = R.id.text_no_pinned
                viewSplitApp.layoutParams = it
            }
        } else {
            listPinnedApps.isVisible = true
            textNoPinned.isVisible = false
            (viewSplitApp.layoutParams as ConstraintLayout.LayoutParams).let {
                it.topToBottom = R.id.list_pinned_apps
                viewSplitApp.layoutParams = it
            }
        }

        if (allAppAdapter.data.size == 0) {
            listAllApps.isVisible = false
            textAllAppPinned.isVisible = true
            (viewSplitShortcut.layoutParams as ConstraintLayout.LayoutParams).let {
                it.topToBottom = R.id.text_all_app_pinned
                viewSplitShortcut.layoutParams = it
            }
        } else {
            listAllApps.isVisible = true
            textAllAppPinned.isVisible = false
            (viewSplitShortcut.layoutParams as ConstraintLayout.LayoutParams).let {
                it.topToBottom = R.id.list_all_apps
                viewSplitShortcut.layoutParams = it
            }
        }

        if (allShortcutAdapter.data.size == 0) {
            listAllShortcuts.isVisible = false
            textAllShortcutPinned.isVisible = true
        } else {
            listAllShortcuts.isVisible = true
            textAllShortcutPinned.isVisible = false
        }
    }

    private fun savePinnedApps() {
        SettingsObserver.putMiniWindowAppsSettings(this,
            pinnedItemAdapter.data.joinToString(";") {
                if (it.shortcutInfo != null) {
                    "${it.packageName}:${it.shortcutInfo.id}:${it.shortcutInfo.userId}"
                } else {
                    it.packageName
                }
            }
        )
    }
}
