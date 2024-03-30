/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.callback

import org.nameless.systemtool.windowmode.bean.AppInfo

interface IIconClickedListener {
    fun onIconClicked(appInfo: AppInfo) {}
    fun onAddClicked(appInfo: AppInfo) {}
    fun onRemovedClicked(appInfo: AppInfo, index: Int) {}
}
