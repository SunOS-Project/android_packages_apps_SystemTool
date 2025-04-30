/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.windowmode.view

import android.content.Context
import android.view.WindowManager

import androidx.core.view.isVisible

import org.sun.systemtool.common.Utils.logE
import org.sun.systemtool.windowmode.util.Shared.leftCircle
import org.sun.systemtool.windowmode.util.Shared.service
import org.sun.systemtool.windowmode.util.Shared.windowManager

class AppLeftCircleViewGroup(
    context: Context,
) : BaseAppCircleViewGroup(context) {

    init {
        leftCircle = this
    }

    override fun isLeft() = true

    companion object {

        private const val TAG = "SystemTool::AppLeftCircleViewGroup"

        fun addCircleViewGroup(): Boolean {
            try {
                windowManager.addView(
                    AppLeftCircleViewGroup(
                        service.createWindowContext(
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, null)).apply {
                        isVisible = false
                    }, groupLayoutParams)
                return true
            } catch (e: Exception) {
                logE(TAG, "Exception on addCircleViewGroup")
            }
            return false
        }
    }
}
