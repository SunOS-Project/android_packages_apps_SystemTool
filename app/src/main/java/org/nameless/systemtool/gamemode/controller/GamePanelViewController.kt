/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.controller

import android.animation.ValueAnimator
import android.graphics.PixelFormat
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager.LayoutParams

import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.updateLayoutParams

import kotlin.math.min

import org.nameless.systemtool.R
import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.gamemode.util.ScreenRecordHelper
import org.nameless.systemtool.gamemode.util.Shared.newGameLaunched
import org.nameless.systemtool.gamemode.util.Shared.portrait
import org.nameless.systemtool.gamemode.util.Shared.screenShortWidth
import org.nameless.systemtool.gamemode.util.Shared.screenWidth
import org.nameless.systemtool.gamemode.util.Shared.service
import org.nameless.systemtool.gamemode.util.Shared.windowManager
import org.nameless.systemtool.gamemode.view.GamePanelView

object GamePanelViewController {

    private const val TAG = "SystemTool::GamePanelViewController"

    @Suppress("DEPRECATION")
    private val layoutParams = LayoutParams().apply {
        type = LayoutParams.TYPE_APPLICATION_OVERLAY
        format = PixelFormat.TRANSPARENT
        gravity = Gravity.TOP or Gravity.LEFT
        flags = LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                LayoutParams.FLAG_LAYOUT_IN_SCREEN
        windowAnimations = 0
    }

    private const val ANIMATION_DURATION = 150L

    private const val PORTRAIT_WIDTH = 0.9f
    private const val PORTRAIT_HEIGHT = 0.87f
    private const val PORTRAIT_LEFT_PADDING = 0.05f
    private const val PORTRAIT_TOP_PADDING = 0.15f

    private const val LANDSCAPE_WIDTH = 0.9f
    private const val LANDSCAPE_HEIGHT = 0.87f
    private const val LANDSCAPE_LEFT_PADDING = 0.12f
    private const val LANDSCAPE_TOP_PADDING = 0.06f

    private var panelView: GamePanelView? = null

    @Suppress("DEPRECATION")
    private val infoHandler = Handler()

    var animating = false
    var expanded = false
    var shownInfo = false

    fun addPanelView() {
        if (panelView != null) {
            return
        }
        logD(TAG, "addPanelView")
        panelView = LayoutInflater.from(service).inflate(R.layout.game_panel_layout, null) as GamePanelView?
        windowManager.currentWindowMetrics.bounds.let {
            screenShortWidth = min(it.width(), it.height())
            screenWidth = it.width()
            portrait = it.width() < it.height()
        }
        windowManager.addView(panelView, LayoutParams().apply {
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
        ScreenRecordHelper.bind()
        setPanelViewTouch(false)
        expanded = false
        if (!newGameLaunched) {
            shownInfo = true
        } else {
            shownInfo = false
            infoHandler.postDelayed({
                shownInfo = true
                GameModeInfoViewController.showGameModeOnInfo()
            }, 800L)
        }
    }

    fun removePanelView() {
        if (panelView == null) {
            return
        }
        logD(TAG, "removePanelView")
        infoHandler.removeCallbacksAndMessages(null)
        panelView?.recycleViewShortcuts?.tileList?.forEach { it.onDetach() }
        ScreenRecordHelper.unbind()
        windowManager.removeView(panelView)
        GameModeInfoViewController.forceRemoveInfoView()
        shownInfo = false
        expanded = false
        panelView = null
    }

    fun resetPanelView() {
        if (panelView == null) {
            return
        }
        logD(TAG, "resetPanelView")
        infoHandler.removeCallbacksAndMessages(null)
        windowManager.currentWindowMetrics.bounds.let {
            screenShortWidth = min(it.width(), it.height())
            screenWidth = it.width()
            portrait = it.width() < it.height()
        }
        windowManager.updateViewLayout(panelView, LayoutParams().apply {
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
        setPanelViewTouch(false)
        DanmakuController.updateLayout()
        expanded = false
        if (!shownInfo) {
            infoHandler.postDelayed({
                shownInfo = true
                GameModeInfoViewController.showGameModeOnInfo()
            }, 500L)
        }
    }

    fun movePanelView(rawX: Float) {
        panelView?.let {
            it.post {
                it.updateLayoutParams<LayoutParams> {
                    x = min(
                        (screenShortWidth * if (portrait) PORTRAIT_LEFT_PADDING else LANDSCAPE_LEFT_PADDING).toInt(),
                        rawX.toInt() - width
                    )
                }
                windowManager.updateViewLayout(it, it.layoutParams)
            }
        }
    }

    fun setPanelViewTouch(touchable: Boolean) {
        panelView?.let {
            it.post {
                it.updateLayoutParams<LayoutParams> {
                    flags = if (touchable) {
                        flags and LayoutParams.FLAG_NOT_FOCUSABLE.inv()
                    } else {
                        flags or LayoutParams.FLAG_NOT_FOCUSABLE
                    }
                }
                windowManager.updateViewLayout(it, it.layoutParams)
            }
        }
    }

    fun animateShow(endRunnable: Runnable? = null) {
        if (animating) {
            return
        }
        panelView?.let { v ->
            v.post {
                ValueAnimator.ofInt(
                    (v.layoutParams as LayoutParams).x,
                    (screenShortWidth * if (portrait) PORTRAIT_LEFT_PADDING else LANDSCAPE_LEFT_PADDING).toInt(),
                ).apply {
                    addUpdateListener {
                        v.updateLayoutParams<LayoutParams> {
                            x = it.animatedValue as Int
                        }
                        windowManager.updateViewLayout(v, v.layoutParams)
                    }
                    duration = ANIMATION_DURATION
                    doOnStart {
                        animating = true
                        expanded = true
                    }
                    doOnEnd {
                        animating = false
                        expanded = true
                        endRunnable?.run()
                    }
                }.start()
            }
        }
    }

    fun animateHide(endRunnable: Runnable? = null) {
        if (animating) {
            return
        }
        panelView?.let { v ->
            v.post {
                ValueAnimator.ofInt(
                    (v.layoutParams as LayoutParams).x,
                    -(v.width)
                ).apply {
                    addUpdateListener {
                        v.updateLayoutParams<LayoutParams> {
                            x = it.animatedValue as Int
                        }
                        windowManager.updateViewLayout(v, v.layoutParams)
                    }
                    duration = ANIMATION_DURATION
                    doOnStart {
                        animating = true
                    }
                    doOnEnd {
                        animating = false
                        expanded = false
                        panelView?.scrollViewApps?.scrollTo(0, 0)
                        setPanelViewTouch(false)
                        endRunnable?.run()
                    }
                }.start()
            }
        }
    }

    fun getLayoutParams(): LayoutParams? {
        return panelView?.layoutParams as LayoutParams?
    }
}
