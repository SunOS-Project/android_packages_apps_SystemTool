/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.controller

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.view.isVisible

import org.nameless.systemtool.R
import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.gamemode.util.GestureResourceUtils
import org.nameless.systemtool.gamemode.util.Shared.portrait
import org.nameless.systemtool.gamemode.util.Shared.screenShortWidth
import org.nameless.systemtool.gamemode.util.Shared.service
import org.nameless.systemtool.gamemode.util.Shared.windowManager

@SuppressLint("StaticFieldLeak")
object GameModeSideViewController {

    private const val TAG = "SystemTool::GameModeSideViewController"

    @Suppress("DEPRECATION")
    private val layoutParams = WindowManager.LayoutParams().apply {
        type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        format = PixelFormat.TRANSPARENT
        gravity = Gravity.TOP or Gravity.LEFT
        flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        privateFlags = WindowManager.LayoutParams.PRIVATE_FLAG_TRUSTED_OVERLAY or
                WindowManager.LayoutParams.SYSTEM_FLAG_SHOW_FOR_ALL_USERS
        x = 0
        windowAnimations = 0
    }

    private const val PORTRAIT_TOP_PADDING = 0.2f
    private const val LANDSCAPE_TOP_PADDING = 0.1f

    private var sideView: View? = null

    fun addSideView() {
        if (sideView != null) {
            return
        }
        logD(TAG, "addSideView")
        sideView = LayoutInflater.from(service).inflate(R.layout.game_side_layout, null)
        windowManager.addView(sideView, WindowManager.LayoutParams().apply {
            copyFrom(layoutParams)
            width = GestureResourceUtils.getGameModeGestureValidDistance(service.resources) / 2
            if (portrait) {
                y = (PORTRAIT_TOP_PADDING * screenShortWidth).toInt()
                height = GestureResourceUtils.getGameModePortraitAreaBottom(service.resources) - y
            } else {
                y = (LANDSCAPE_TOP_PADDING * screenShortWidth).toInt()
                height = GestureResourceUtils.getGameModeLandscapeAreaBottom(service.resources) - y
            }
        })
    }

    fun removeSideView() {
        if (sideView == null) {
            return
        }
        logD(TAG, "removeSideView")
        windowManager.removeView(sideView)
        sideView = null
    }

    fun resetSideView() {
        if (sideView == null) {
            return
        }
        logD(TAG, "resetSideView")
        windowManager.updateViewLayout(sideView, WindowManager.LayoutParams().apply {
            copyFrom(layoutParams)
            width = GestureResourceUtils.getGameModeGestureValidDistance(service.resources) / 2
            if (portrait) {
                y = (PORTRAIT_TOP_PADDING * screenShortWidth).toInt()
                height = GestureResourceUtils.getGameModePortraitAreaBottom(service.resources) - y
            } else {
                y = (LANDSCAPE_TOP_PADDING * screenShortWidth).toInt()
                height = GestureResourceUtils.getGameModeLandscapeAreaBottom(service.resources) - y
            }
        })
    }

    fun setSideViewVisible(visible: Boolean) {
        sideView?.let {
            if (it.isVisible != visible) {
                it.post {
                    it.isVisible = visible
                }
            }
        }
    }
}
