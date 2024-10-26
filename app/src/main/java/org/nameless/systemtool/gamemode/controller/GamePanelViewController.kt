/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.controller

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager.LayoutParams
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout

import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams

import kotlin.math.min

import org.nameless.systemtool.R
import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.gamemode.util.Config.PANEL_LANDSCAPE_HEIGHT
import org.nameless.systemtool.gamemode.util.Config.PANEL_LANDSCAPE_LEFT_PADDING
import org.nameless.systemtool.gamemode.util.Config.PANEL_LANDSCAPE_TOP_PADDING
import org.nameless.systemtool.gamemode.util.Config.PANEL_LANDSCAPE_WIDTH
import org.nameless.systemtool.gamemode.util.Config.PANEL_PORTRAIT_HEIGHT
import org.nameless.systemtool.gamemode.util.Config.PANEL_PORTRAIT_LEFT_PADDING
import org.nameless.systemtool.gamemode.util.Config.PANEL_PORTRAIT_TOP_PADDING
import org.nameless.systemtool.gamemode.util.Config.PANEL_PORTRAIT_WIDTH
import org.nameless.systemtool.gamemode.util.Config.SIDE_LANDSCAPE_TOP_PADDING
import org.nameless.systemtool.gamemode.util.Config.SIDE_PORTRAIT_TOP_PADDING
import org.nameless.systemtool.gamemode.util.GestureResourceUtils
import org.nameless.systemtool.gamemode.util.Shared.portrait
import org.nameless.systemtool.gamemode.util.Shared.screenShortWidth
import org.nameless.systemtool.gamemode.util.Shared.screenWidth
import org.nameless.systemtool.gamemode.util.Shared.service
import org.nameless.systemtool.gamemode.util.Shared.windowManager
import org.nameless.systemtool.gamemode.view.GamePanelView

@SuppressLint("StaticFieldLeak")
object GamePanelViewController {

    private const val TAG = "SystemTool::GamePanelViewController"

    private val layoutParams = LayoutParams().apply {
        type = LayoutParams.TYPE_APPLICATION_OVERLAY
        width = LayoutParams.MATCH_PARENT
        height = LayoutParams.MATCH_PARENT
        format = PixelFormat.TRANSPARENT
        gravity = Gravity.TOP or Gravity.LEFT
        flags = LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                LayoutParams.FLAG_HARDWARE_ACCELERATED
        privateFlags = LayoutParams.PRIVATE_FLAG_TRUSTED_OVERLAY or
                LayoutParams.SYSTEM_FLAG_SHOW_FOR_ALL_USERS
        layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
    }

    private val singleTapDetector by lazy {
        GestureDetector(service, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(event: MotionEvent): Boolean {
                if (animating) {
                    return false
                }
                if (event.rawX >= targetExpandedX && event.rawX <= targetExpandedX + panelWidth
                        && event.rawY >= targetY && event.rawY <= targetY + panelHeight) {
                    return false
                }
                animateHide()
                return true
            }
        })
    }

    private const val ANIMATION_DURATION = 200L

    private var container: FrameLayout? = null

    private var panelView: GamePanelView? = null
    private var sideView: View? = null

    var targetExpandedX = 0f
    var targetCollapsedX = 0f
    private var targetY = 0f
    var panelWidth = 0f
    private var panelHeight = 0f

    var animating = false

    @SuppressLint("ClickableViewAccessibility")
    fun onGameStart() {
        service.mainHandler.post {
            if (container != null) {
                return@post
            }
            logD(TAG, "onGameStart")

            container = FrameLayout(
                service.createWindowContext(
                    LayoutParams.TYPE_APPLICATION_OVERLAY, null))
            container?.apply {
                setOnTouchListener { _, e ->
                    singleTapDetector.onTouchEvent(e)
                }
            }
            windowManager.addView(container, layoutParams)
            setContainerTouch(false)

            updateScreenWidth()

            panelView = LayoutInflater.from(service)
                    .inflate(R.layout.game_panel_layout, null) as GamePanelView?
            updatePanelViewLayout()
            container?.addView(panelView)

            sideView = LayoutInflater.from(service).inflate(R.layout.game_side_layout, null)
            updateSideViewLayout()
            container?.addView(sideView)
        }
    }

    fun onGameStop() {
        service.mainHandler.post {
            if (container == null) {
                return@post
            }
            logD(TAG, "onGameStop")

            targetExpandedX = 0f
            targetCollapsedX = 0f
            targetY = 0f
            panelWidth = 0f
            panelHeight = 0f

            sideView = null

            panelView?.recycleViewShortcuts?.tileList?.forEach { it.destroy() }
            panelView = null

            container?.removeAllViews()
            windowManager.removeViewImmediate(container)
            container = null
        }
    }

    fun onConfigurationChanged() {
        service.mainHandler.post {
            if (container == null) {
                return@post
            }
            logD(TAG, "onConfigurationChanged")

            setContainerTouch(false)

            updateScreenWidth()
            updatePanelViewLayout()
            updateSideViewLayout()
            DanmakuController.updateLayout()

            sideView?.isVisible = true
        }
    }

    fun expandPanelView(event: MotionEvent) {
        service.mainHandler.post {
            sideView?.isVisible = false
            panelView?.expandPanelView(event)
        }
    }

    private fun updateScreenWidth() {
        windowManager.currentWindowMetrics.bounds.let {
            screenShortWidth = min(it.width(), it.height())
            screenWidth = it.width()
            portrait = it.width() < it.height()
        }
    }

    private fun updatePanelViewLayout() {
        val l: Float
        val t: Float
        val w: Float
        val h: Float
        if (portrait) {
            l = screenShortWidth * PANEL_PORTRAIT_LEFT_PADDING
            t = screenShortWidth * PANEL_PORTRAIT_TOP_PADDING
            w = screenShortWidth * PANEL_PORTRAIT_WIDTH
            h = screenShortWidth * PANEL_PORTRAIT_HEIGHT
        } else {
            l = screenShortWidth * PANEL_LANDSCAPE_LEFT_PADDING
            t = screenShortWidth * PANEL_LANDSCAPE_TOP_PADDING
            w = screenShortWidth * PANEL_LANDSCAPE_WIDTH
            h = screenShortWidth * PANEL_LANDSCAPE_HEIGHT
        }

        targetExpandedX = l
        targetCollapsedX = -(l + w)
        targetY = t
        panelWidth = w
        panelHeight = h

        panelView?.apply {
            translationX = targetCollapsedX
            translationY = targetY
            layoutParams = FrameLayout.LayoutParams(w.toInt(), h.toInt())
        }
    }

    private fun updateSideViewLayout() {
        sideView?.apply {
            val width = GestureResourceUtils.getGameModeGestureValidDistance(service.resources) / 2
            if (portrait) {
                translationY = screenShortWidth * SIDE_PORTRAIT_TOP_PADDING
                layoutParams = FrameLayout.LayoutParams(width,
                    GestureResourceUtils.getGameModePortraitAreaBottom(
                        service.resources) - translationY.toInt())
            } else {
                translationY = screenShortWidth * SIDE_LANDSCAPE_TOP_PADDING
                layoutParams = FrameLayout.LayoutParams(width,
                    GestureResourceUtils.getGameModeLandscapeAreaBottom(
                        service.resources) - translationY.toInt())
            }
        }
    }

    fun setContainerTouch(touchable: Boolean) {
        container?.let {
            it.updateLayoutParams<LayoutParams> {
                flags = if (touchable) {
                    flags and LayoutParams.FLAG_NOT_FOCUSABLE.inv() and
                            LayoutParams.FLAG_NOT_TOUCHABLE.inv()
                } else {
                    flags or LayoutParams.FLAG_NOT_FOCUSABLE or
                            LayoutParams.FLAG_NOT_TOUCHABLE
                }
            }
            windowManager.updateViewLayout(it, it.layoutParams)
        }
    }

    fun animateShow(endRunnable: Runnable? = null) {
        service.mainHandler.post {
            if (animating) {
                return@post
            }
            panelView?.let { v ->
                ValueAnimator.ofFloat(
                    v.translationX, targetExpandedX
                ).apply {
                    addUpdateListener {
                        v.translationX = it.animatedValue as Float
                    }
                    duration = ANIMATION_DURATION
                    interpolator = DecelerateInterpolator()
                    doOnStart {
                        animating = true
                        setContainerTouch(true)
                    }
                    doOnEnd {
                        animating = false
                        endRunnable?.run()
                    }
                }.start()
            }
        }
    }

    fun animateHide(endRunnable: Runnable? = null) {
        service.mainHandler.post {
            if (animating || panelView?.translationX == targetCollapsedX) {
                return@post
            }
            panelView?.let { v ->
                ValueAnimator.ofFloat(
                    v.translationX, targetCollapsedX
                ).apply {
                    addUpdateListener {
                        v.translationX = it.animatedValue as Float
                    }
                    duration = ANIMATION_DURATION
                    interpolator = DecelerateInterpolator()
                    doOnStart {
                        animating = true
                    }
                    doOnEnd {
                        animating = false
                        panelView?.scrollViewApps?.scrollTo(0, 0)
                        animateShowSideView()
                        setContainerTouch(false)
                        endRunnable?.run()
                    }
                }.start()
            }
        }
    }

    private fun animateShowSideView() {
        sideView?.let { v ->
            ValueAnimator.ofFloat(
                -v.width.toFloat(), 0f
            ).apply {
                addUpdateListener {
                    v.translationX = it.animatedValue as Float
                }
                duration = ANIMATION_DURATION
                interpolator = DecelerateInterpolator()
                doOnStart {
                    v.translationX = -v.width.toFloat()
                    v.isVisible = true
                }
            }.start()
        }
    }

    fun onGestureUp(velocityX: Float) {
        panelView?.onFingerUpWhenExpand(velocityX)
    }

    fun isShowing() = panelView?.translationX == targetExpandedX
}
