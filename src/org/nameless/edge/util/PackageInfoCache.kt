/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.util

import android.content.Context
import android.content.pm.ApplicationInfo.FLAG_SYSTEM
import android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.drawable.Drawable

import java.text.Collator

import kotlin.Comparator

import org.nameless.edge.PickerDataCache
import org.nameless.edge.util.Constants

object PackageInfoCache {

    val availablePackages: MutableList<String> = mutableListOf()
    private val caches: MutableMap<String, Pair<Drawable?, String>> = mutableMapOf()

    private val DEFAULT_LABEL = "Unknown"

    fun initPackageList(context: Context) {
        availablePackages.clear()

        context.packageManager.getInstalledPackages(0).filter {
            Constants.miniWindowSystemAppsWhitelist.contains(it.packageName) ||
            ((it.applicationInfo.flags and FLAG_SYSTEM) == 0 &&
            (it.applicationInfo.flags and FLAG_UPDATED_SYSTEM_APP) == 0)
        }.forEach {
            availablePackages.add(it.packageName)
            caches[it.packageName] = Pair(
                getAppIcon(context, it.packageName),
                it.applicationInfo.loadLabel(context.packageManager).toString()
            )
        }

        availablePackages.sortWith(AppComparator())
    }

    fun isPackageAvailable(packageName: String): Boolean {
        return caches.containsKey(packageName)
    }

    fun getIconDrawable(packageName: String): Drawable? {
        return caches[packageName]?.first
    }

    fun getPackageLabel(packageName: String): String {
        return caches[packageName]?.second ?: DEFAULT_LABEL
    }

    fun onPackageStateChanged(context: Context, packageName: String, newApp: Boolean = false) {
        if (isSystemApp(context, packageName) &&
                !Constants.miniWindowSystemAppsWhitelist.contains(packageName)) {
            return
        }

        if (!newApp) {
            availablePackages.remove(packageName)
            caches.remove(packageName)
        }

        if (newApp || isPackageInstalled(context, packageName)) {
            availablePackages.add(packageName)
            caches[packageName] = Pair(
                getAppIcon(context, packageName),
                getAppName(context, packageName)?: DEFAULT_LABEL
            )

            availablePackages.sortWith(AppComparator())
        }

        PickerDataCache.onAvailablePackagesChanged()
    }

    private fun getAppName(context: Context, packageName: String): String? {
        try {
            return context.packageManager.getPackageInfo(packageName, 0)
                    .applicationInfo.loadLabel(context.packageManager).toString()
        } catch (e: NameNotFoundException) {
            return null
        }
    }

    private fun getAppIcon(context: Context, packageName: String): Drawable? {
        var loadIcon: Drawable? = null
        try {
            loadIcon = context.packageManager.getApplicationIcon(packageName)
        } catch (e: NameNotFoundException) {}
        if (loadIcon == null) {
            loadIcon = context.packageManager.defaultActivityIcon
        }
        return loadIcon
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        try {
            return context.packageManager.getPackageInfo(packageName, 0)
                    .applicationInfo.enabled
        } catch (e: NameNotFoundException) {
            return false
        }
    }

    private fun isSystemApp(context: Context, packageName: String): Boolean {
        try {
            context.packageManager.getPackageInfo(packageName, 0).applicationInfo.let {
                return (it.flags and FLAG_SYSTEM) != 0 || (it.flags and FLAG_UPDATED_SYSTEM_APP) != 0
            }
        } catch (e: NameNotFoundException) {
            return false
        }
    }

    internal class AppComparator : Comparator<String> {
        val collator = Collator.getInstance()

        override fun compare(s1: String, s2: String): Int {
            return collator.compare(caches[s1]?.second ?: DEFAULT_LABEL,
                    caches[s2]?.second ?: DEFAULT_LABEL)
        }
    }
}
