/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode

import android.os.Bundle

import org.nameless.systemtool.R

class ManageAppsActivity : AllAppsPickerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.manage_apps_title)
    }

    override fun allowStartApp() = false

    override fun finishOnStop() = false
}
