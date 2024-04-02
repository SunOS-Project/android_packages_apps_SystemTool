/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

import androidx.appcompat.content.res.AppCompatResources

import org.nameless.systemtool.R
import org.nameless.systemtool.common.Utils
import org.nameless.systemtool.windowmode.view.CircleIconView

object IconDrawableHelper {

    private val packageCache = mutableMapOf<String, Drawable>()
    private val shortcutCache = mutableMapOf<Pair<String, String>, Drawable>()
    private val shortcutInfoCache = mutableMapOf<ShortcutInfo, Drawable>()

    private fun getDrawable(context: Context, packageName: String): Drawable {
        return packageCache.getOrPut(packageName) {
            if (Utils.PACKAGE_NAME == packageName) {
                AppCompatResources.getDrawable(context, R.drawable.ic_more_app)
                    ?: context.packageManager.defaultActivityIcon
            } else {
                try {
                    context.packageManager.getApplicationIcon(packageName)
                } catch (_: NameNotFoundException) {
                    context.packageManager.defaultActivityIcon
                }
            }
        }
    }

    fun getDrawable(context: Context, launcherApps: LauncherApps, iconView: CircleIconView): Drawable {
        if (iconView.shortcutId.isNotBlank() && iconView.shortcutUserId != Int.MIN_VALUE) {
            return shortcutCache.getOrPut(
                Pair(iconView.packageName, iconView.shortcutId)
            ) {
                (ShortcutHelper.getShortcuts(launcherApps, iconView.packageName)
                        ?.find { iconView.shortcutId == it.id && iconView.shortcutUserId == it.userId }
                        ?.let { info ->
                            launcherApps.getShortcutBadgedIconDrawable(info, 0)
                        } ?: context.packageManager.defaultActivityIcon).let { largeDrawable ->
                    mergeDrawable(context, largeDrawable, getDrawable(context, iconView.packageName))
                }
            }
        }

        return getDrawable(context, iconView.packageName)
    }

    fun getDrawable(context: Context, applicationInfo: ApplicationInfo?): Drawable {
        return applicationInfo?.loadIcon(context.packageManager)
            ?: context.packageManager.defaultActivityIcon
    }

    fun getDrawable(context: Context, launcherApps: LauncherApps, shortcutInfo: ShortcutInfo): Drawable {
        return (launcherApps.getShortcutBadgedIconDrawable(shortcutInfo, 0)
                ?: context.packageManager.defaultActivityIcon).let { largeDrawable ->
            mergeDrawable(context, largeDrawable, getDrawable(context, shortcutInfo.`package`))
        }
    }

    private fun mergeDrawable(context: Context, largeDrawable: Drawable, smallDrawable: Drawable): Drawable {
        val badgedWidth = largeDrawable.intrinsicWidth
        val badgedHeight = largeDrawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(badgedWidth, badgedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        largeDrawable.setBounds(0, 0, badgedWidth, badgedHeight)
        largeDrawable.draw(canvas)
        smallDrawable.setBounds(
            (badgedWidth * 0.55f).toInt(),
            (badgedHeight * 0.55f).toInt(),
            (badgedWidth * 0.95f).toInt(),
            (badgedHeight * 0.95f).toInt()
        )
        smallDrawable.draw(canvas)
        val mergedDrawable = BitmapDrawable(context.resources, bitmap)
        if (largeDrawable is BitmapDrawable) {
            mergedDrawable.setTargetDensity(largeDrawable.bitmap.density)
        }
        return mergedDrawable
    }

    fun invalidatePackageCache(packageName: String) {
        packageCache.remove(packageName)
        shortcutCache.filter { it.key.first == packageName }.map { it.key }.forEach {
            shortcutCache.remove(it)
        }
        shortcutInfoCache.filter { it.key.`package` == packageName }.map { it.key }.forEach {
            shortcutInfoCache.remove(it)
        }
    }
}
