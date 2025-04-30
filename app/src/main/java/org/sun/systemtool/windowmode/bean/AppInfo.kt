/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.windowmode.bean

import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable

data class AppInfo(
    val label: String,
    val compareLabel: String,
    val packageName: String,
    val icon: Drawable,
    val shortcutInfo: ShortcutInfo? = null
)
