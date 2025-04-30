/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.gamemode.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.widget.LinearLayout
import android.widget.ScrollView

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

import org.sun.systemtool.R
import org.sun.systemtool.common.Utils.logD
import org.sun.systemtool.gamemode.controller.GamePanelViewController.animateHide
import org.sun.systemtool.gamemode.controller.GamePanelViewController.animateShow
import org.sun.systemtool.gamemode.controller.GamePanelViewController.panelWidth
import org.sun.systemtool.gamemode.controller.GamePanelViewController.targetCollapsedX
import org.sun.systemtool.gamemode.controller.GamePanelViewController.targetExpandedX
import org.sun.systemtool.gamemode.util.Shared.portrait
import org.sun.systemtool.gamemode.util.Shared.screenShortWidth

class GamePanelView(
    context: Context,
    attrs: AttributeSet
) : LinearLayout(context, attrs) {

    val scrollViewApps by lazy { findViewById<ScrollView>(R.id.sv_apps)!! }
    val recycleViewShortcuts by lazy { findViewById<ShortcutGridView>(R.id.rv_shortcut_tiles)!! }

    private var downX = 0f
    private var downY = 0f
    private var lastTranslationX = 0f

    private var interceptTouch = false

    private var touchSlop = 0

    private var velocityTracker: VelocityTracker? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                interceptTouch = false
                downX = event.rawX
                downY = event.rawY
                lastTranslationX = translationX
                velocityTracker = VelocityTracker.obtain()
            }
            MotionEvent.ACTION_MOVE -> {
                if (interceptTouch) {
                    return true
                }
                if (abs(event.rawX - downX) >= touchSlop &&
                        abs(event.rawY - downY) < touchSlop) {
                    interceptTouch = true
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (interceptTouch) {
                    return true
                }
                if (abs(event.rawX - downX) >= touchSlop &&
                        abs(event.rawY - downY) < touchSlop) {
                    interceptTouch = true
                    return true
                }
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                velocityTracker?.apply {
                    addMovement(event)
                    computeCurrentVelocity(VELOCITY_UNIT_MS)
                }
                translationX = max(targetCollapsedX, min(targetExpandedX,
                        lastTranslationX + event.rawX - downX))
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.apply {
                    computeCurrentVelocity(VELOCITY_UNIT_MS)
                    onFingerUpWhenMove(xVelocity)
                    clear()
                    recycle()
                }
                velocityTracker = null
                return true
            }
            else -> return super.onTouchEvent(event)
        }
    }

    fun expandPanelView(event: MotionEvent) {
        val x = event.rawX + panelWidth * (if (portrait) 0.08f else 0.16f)
        translationX = min(targetExpandedX, x - targetExpandedX - panelWidth)
    }

    fun onFingerUpWhenExpand(velocityX: Float) {
        logD(TAG, "onFingerUpWhenExpand, velocityX=${velocityX}")
        if (velocityX < VELOCITY_X_FLING && translationX + panelWidth < 0.09f * screenShortWidth) {
            animateHide()
        } else {
            animateShow()
        }
    }

    private fun onFingerUpWhenMove(velocityX: Float) {
        logD(TAG, "onFingerUpWhenMove, velocityX=${velocityX}")
        if (velocityX <= -VELOCITY_X_FLING || translationX + panelWidth < 0.6f * screenShortWidth) {
            animateHide()
        } else {
            animateShow()
        }
    }

    companion object {
        private const val TAG = "SystemTool::GamePanelView"

        const val VELOCITY_UNIT_MS = 1000
        const val VELOCITY_X_FLING = 1000f
    }
}
