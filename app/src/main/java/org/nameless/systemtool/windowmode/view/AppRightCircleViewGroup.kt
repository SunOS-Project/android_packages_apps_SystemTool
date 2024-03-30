/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.view

import android.content.Context
import android.view.WindowManager

import androidx.core.view.isVisible

import org.nameless.systemtool.common.Utils.logE
import org.nameless.systemtool.windowmode.util.Shared.rightCircle
import org.nameless.systemtool.windowmode.util.Shared.service
import org.nameless.systemtool.windowmode.util.Shared.windowManager

class AppRightCircleViewGroup(
    context: Context,
) : BaseAppCircleViewGroup(context) {

    init {
        rightCircle = this
    }

    override fun isLeft() = false

    companion object {

        private const val TAG = "SystemTool::AppRightCircleViewGroup"

        fun addCircleViewGroup(): Boolean {
            try {
                windowManager.addView(
                    AppRightCircleViewGroup(
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
