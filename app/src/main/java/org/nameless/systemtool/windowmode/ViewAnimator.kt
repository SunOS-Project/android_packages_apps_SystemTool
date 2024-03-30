/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.graphics.Rect
import android.view.animation.PathInterpolator

import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.children
import androidx.core.view.isVisible

import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.windowmode.util.Config.HIDE_ANIMATION_DURATION
import org.nameless.systemtool.windowmode.util.Config.REBOUND_ANIMATION_DURATION
import org.nameless.systemtool.windowmode.util.Config.ROTATE_REBOUND_ANGLE
import org.nameless.systemtool.windowmode.util.Config.ROTATE_START_ANGLE
import org.nameless.systemtool.windowmode.util.Config.SCALE_REBOUND_VALUE
import org.nameless.systemtool.windowmode.util.Config.SCALE_START_VALUE
import org.nameless.systemtool.windowmode.util.Config.SHOW_ANIMATION_DURATION
import org.nameless.systemtool.windowmode.util.Shared.dimmerView
import org.nameless.systemtool.windowmode.util.Shared.leftCircle
import org.nameless.systemtool.windowmode.util.Shared.rightCircle
import org.nameless.systemtool.windowmode.view.BaseAppCircleViewGroup

object ViewAnimator {

    private const val TAG = "SystemTool::ViewAnimator"

    var circleViewGroup: BaseAppCircleViewGroup? = null

    var currentlyVisible = false
    var animating = false
    var allowVisible = true
        set(value) {
            field = value
            if (!value && currentlyVisible) {
                hideCircle()
            }
        }

    fun showCircle(isLeft: Boolean, displayBounds: Rect) {
        if (!allowVisible) {
            logD(TAG, "showCircle: Set visible is disabled, return early")
            return
        }

        if (currentlyVisible) {
            logD(TAG, "showCircle: Already showing, return early")
            return
        }

        logD(TAG, "showCircle")

        circleViewGroup = if (isLeft) leftCircle else rightCircle

        val alphaAnimator = ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            addUpdateListener {
                circleViewGroup?.children?.forEach { child ->
                    child.alpha = it.animatedValue as Float
                }
            }
            duration = SHOW_ANIMATION_DURATION
            interpolator = PathInterpolator(0.33f, 0.0f, 0.67f, 1.0f)
        }
        val rotateClockwiseAnimator =
            ValueAnimator.ofFloat(ROTATE_START_ANGLE, ROTATE_REBOUND_ANGLE).apply {
                addUpdateListener {
                    circleViewGroup?.children?.forEach { child ->
                        child.rotation = it.animatedValue as Float
                    }
                }
                duration = SHOW_ANIMATION_DURATION
                interpolator = PathInterpolator(0.24f, 0.17f, 0.53f, 0.82f)
            }
        val scaleAnimator =
            ValueAnimator.ofFloat(SCALE_START_VALUE, SCALE_REBOUND_VALUE).apply {
                addUpdateListener {
                    circleViewGroup?.children?.forEach { child ->
                        child.scaleX = it.animatedValue as Float
                        child.scaleY = it.animatedValue as Float
                    }
                }
                duration = SHOW_ANIMATION_DURATION
                interpolator = PathInterpolator(0.24f, 0.74f, 0.53f, 0.92f)
            }
        val translationAnimator = ValueAnimator.ofFloat(1.0f, 0.0f).apply {
            addUpdateListener {
                circleViewGroup?.children?.forEach { child ->
                    child.translationX = if (isLeft) {
                        -child.x - 100f
                    } else {
                        displayBounds.width() - child.x + 100f
                    } * it.animatedValue as Float
                    child.translationY =
                        (displayBounds.height() - child.y + 100f) * it.animatedValue as Float
                }
            }
            duration = SHOW_ANIMATION_DURATION
            interpolator = PathInterpolator(0.24f, 0.55f, 0.53f, 0.8f)
        }
        val rotateReboundAnimator = ValueAnimator.ofFloat(ROTATE_REBOUND_ANGLE, 0.0f).apply {
            addUpdateListener {
                circleViewGroup?.children?.forEach { child ->
                    child.rotation = it.animatedValue as Float
                }
            }
            duration = REBOUND_ANIMATION_DURATION
            startDelay = SHOW_ANIMATION_DURATION
            interpolator = PathInterpolator(0.19f, -7.6f, 0.48f, 1.0f)
        }
        val scaleReboundAnimator = ValueAnimator.ofFloat(SCALE_REBOUND_VALUE, 1.0f).apply {
            addUpdateListener {
                circleViewGroup?.children?.forEach { child ->
                    child.scaleX = it.animatedValue as Float
                    child.scaleY = it.animatedValue as Float
                }
            }
            duration = REBOUND_ANIMATION_DURATION
            startDelay = SHOW_ANIMATION_DURATION
            interpolator = PathInterpolator(0.19f, 0.31f, 0.48f, 1.0f)
        }

        circleViewGroup?.postOnAnimation {
            AnimatorSet().apply {
                playTogether(
                    alphaAnimator,
                    rotateClockwiseAnimator,
                    scaleAnimator,
                    translationAnimator,
                    rotateReboundAnimator,
                    scaleReboundAnimator
                )
                doOnStart {
                    dimmerView.isVisible = true
                    circleViewGroup?.isVisible = true
                    currentlyVisible = true
                    animating = true
                }
                doOnEnd {
                    animating = false
                }
                start()
            }
        }
    }

    fun hideCircle(endAction: Runnable? = null) {
        if (!currentlyVisible) {
            logD(TAG, "hideCircle: Already invisible, return early")
            return
        }

        logD(TAG, "hideCircle")

        val alphaAnimator = ValueAnimator.ofFloat(1.0f, 0.0f).apply {
            addUpdateListener {
                circleViewGroup?.children?.forEach { child ->
                    child.alpha = it.animatedValue as Float
                }
            }
            duration = HIDE_ANIMATION_DURATION
            interpolator = PathInterpolator(0.33f, 0.0f, 0.67f, 1.0f)
        }
        val rotateClockwiseAnimator = ValueAnimator.ofFloat(0f, ROTATE_REBOUND_ANGLE).apply {
            addUpdateListener {
                circleViewGroup?.children?.forEach { child ->
                    child.rotation = it.animatedValue as Float
                }
            }
            duration = HIDE_ANIMATION_DURATION
            interpolator = PathInterpolator(0.17f, 0.0f, 0.53f, -6.56f)
        }
        val scaleAnimator = ValueAnimator.ofFloat(1.0f, SCALE_REBOUND_VALUE).apply {
            addUpdateListener {
                circleViewGroup?.children?.forEach { child ->
                    child.scaleX = it.animatedValue as Float
                    child.scaleY = it.animatedValue as Float
                }
            }
            duration = HIDE_ANIMATION_DURATION
            interpolator = PathInterpolator(0.17f, 0.0f, 0.53f, 0.7f)
        }
        val rotateReboundAnimator = ValueAnimator.ofFloat(ROTATE_REBOUND_ANGLE, ROTATE_START_ANGLE).apply {
            addUpdateListener {
                circleViewGroup?.children?.forEach { child ->
                    child.rotation = it.animatedValue as Float
                }
            }
            duration = HIDE_ANIMATION_DURATION
            startDelay = HIDE_ANIMATION_DURATION
            interpolator = PathInterpolator(0.19f, -0.06f, 0.32f, 1.0f)
        }
        val scaleReboundAnimator = ValueAnimator.ofFloat(SCALE_REBOUND_VALUE, SCALE_START_VALUE).apply {
            addUpdateListener {
                circleViewGroup?.children?.forEach { child ->
                    child.scaleX = it.animatedValue as Float
                    child.scaleY = it.animatedValue as Float
                }
            }
            duration = HIDE_ANIMATION_DURATION
            startDelay = HIDE_ANIMATION_DURATION
            interpolator = PathInterpolator(0.19f, 0.0f, 0.32f, 1.0f)
        }

        circleViewGroup?.postOnAnimation {
            AnimatorSet().apply {
                playTogether(
                    alphaAnimator,
                    rotateClockwiseAnimator,
                    scaleAnimator,
                    rotateReboundAnimator,
                    scaleReboundAnimator
                )
                doOnStart {
                    currentlyVisible = false
                    animating = true
                }
                doOnEnd {
                    dimmerView.isVisible = false
                    circleViewGroup?.isVisible = false
                    circleViewGroup = null
                    animating = false
                    endAction?.run()
                }
                start()
            }
        }
    }
}
