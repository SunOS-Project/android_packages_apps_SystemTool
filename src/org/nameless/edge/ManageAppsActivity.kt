/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge

import android.os.Bundle

import org.nameless.edge.R

class ManageAppsActivity : AllAppsPickerActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.manage_apps_title)
    }

    override fun allowStartApp() = false

    override fun finishOnStop() = false
}
