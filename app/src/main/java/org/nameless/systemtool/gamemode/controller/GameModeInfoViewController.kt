/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.controller

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.core.animation.doOnEnd
import androidx.core.view.updateLayoutParams

import org.nameless.systemtool.R
import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.gamemode.util.Shared.portrait
import org.nameless.systemtool.gamemode.util.Shared.screenShortWidth
import org.nameless.systemtool.gamemode.util.Shared.service
import org.nameless.systemtool.gamemode.util.Shared.windowManager
import org.nameless.systemtool.gamemode.util.ViewSizeAlgorithm

@SuppressLint("StaticFieldLeak")
object GameModeInfoViewController {

    private const val TAG = "SystemTool::GameModeInfoViewController"

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
        windowAnimations = 0
    }

    private const val ANIMATION_DURATION_SHOW = 300L
    private const val ANIMATION_DURATION_HIDE = 300L

    private const val PORTRAIT_WIDTH = 0.64f
    private const val PORTRAIT_HEIGHT = 0.2f
    private const val PORTRAIT_LEFT_PADDING = 0.05f
    private const val PORTRAIT_TOP_PADDING = 0.2f

    private const val LANDSCAPE_WIDTH = 0.64f
    private const val LANDSCAPE_HEIGHT = 0.2f
    private const val LANDSCAPE_LEFT_PADDING = 0.12f
    private const val LANDSCAPE_TOP_PADDING = 0.1f

    private var infoView: View? = null

    var showingInfo = false
        get() = infoView != null

    @Suppress("DEPRECATION")
    private val hideHandler = Handler()

    private fun addInfoView() {
        logD(TAG, "addInfoView")
        infoView = LayoutInflater.from(service).inflate(R.layout.game_mode_info_layout, null)
        infoView?.findViewById<ImageView>(R.id.iv_game_info)?.apply {
            updateLayoutParams<LinearLayout.LayoutParams> {
                ViewSizeAlgorithm.calculateFixedSize(
                    148,
                    considerDensity = false,
                    considerRes = true
                ).let { size ->
                    height = size
                    width = size
                }
            }
        }
        infoView?.findViewById<TextView>(R.id.tv_game_info)?.apply {
            setPadding(
                ViewSizeAlgorithm.calculateFixedSize(
                    36,
                    considerDensity = false,
                    considerRes = true
                ),
                paddingTop,
                paddingRight,
                paddingBottom
            )
            textSize = ViewSizeAlgorithm.calculateFixedSize(
                18,
                considerDensity = true,
                considerRes = false
            ).toFloat()
        }
        windowManager.addView(infoView, WindowManager.LayoutParams().apply {
            copyFrom(layoutParams)
            if (portrait) {
                width = (PORTRAIT_WIDTH * screenShortWidth).toInt()
                height = (PORTRAIT_HEIGHT * screenShortWidth).toInt()
                y = (PORTRAIT_TOP_PADDING * screenShortWidth).toInt()
            } else {
                width = (LANDSCAPE_WIDTH * screenShortWidth).toInt()
                height = (LANDSCAPE_HEIGHT * screenShortWidth).toInt()
                y = (LANDSCAPE_TOP_PADDING * screenShortWidth).toInt()
            }
            x = -width
        })
    }

    private fun removeInfoView() {
        logD(TAG, "removeInfoView")
        windowManager.removeView(infoView)
        infoView = null
    }

    fun showGameModeOnInfo() {
        if (showingInfo) {
            return
        }
        addInfoView()
        infoView?.let { v ->
            v.post {
                ValueAnimator.ofInt(
                    (v.layoutParams as WindowManager.LayoutParams).x,
                    (screenShortWidth * if (portrait) PORTRAIT_LEFT_PADDING else LANDSCAPE_LEFT_PADDING).toInt(),
                ).apply {
                    addUpdateListener {
                        v.updateLayoutParams<WindowManager.LayoutParams> {
                            x = it.animatedValue as Int
                        }
                        windowManager.updateViewLayout(v, v.layoutParams)
                    }
                    duration = ANIMATION_DURATION_SHOW
                    doOnEnd {
                        hideHandler.postDelayed({
                            hideGameModeInfo()
                        }, 1200L)
                    }
                }.start()
            }
        }
    }

    private fun hideGameModeInfo() {
        infoView?.let { v ->
            v.post {
                ValueAnimator.ofInt(
                    (v.layoutParams as WindowManager.LayoutParams).x,
                    -(v.width)
                ).apply {
                    addUpdateListener {
                        v.updateLayoutParams<WindowManager.LayoutParams> {
                            x = it.animatedValue as Int
                        }
                        windowManager.updateViewLayout(v, v.layoutParams)
                    }
                    duration = ANIMATION_DURATION_HIDE
                    doOnEnd {
                        removeInfoView()
                    }
                }.start()
            }
        }
    }

    fun forceRemoveInfoView() {
        hideHandler.removeCallbacksAndMessages(null)
        if (!showingInfo) {
            return
        }
        removeInfoView()
    }
}
