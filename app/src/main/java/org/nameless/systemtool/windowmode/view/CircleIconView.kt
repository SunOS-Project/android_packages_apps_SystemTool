/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.view

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.SystemClock
import android.os.UserHandle
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.animation.PathInterpolator

import androidx.core.animation.doOnEnd

import de.hdodenhof.circleimageview.CircleImageView

import org.nameless.systemtool.common.Utils
import org.nameless.systemtool.windowmode.AllAppsPickerActivity
import org.nameless.systemtool.windowmode.ViewAnimator
import org.nameless.systemtool.windowmode.util.Config.FOCUS_ANIMATION_DURATION
import org.nameless.systemtool.windowmode.util.Config.SCALE_FOCUS_VALUE
import org.nameless.systemtool.windowmode.util.IconDrawableHelper
import org.nameless.systemtool.windowmode.util.Shared.launcherApps
import org.nameless.systemtool.windowmode.util.ShortcutHelper
import org.nameless.view.PopUpViewManager.ACTION_START_MINI_WINDOW
import org.nameless.view.PopUpViewManager.EXTRA_ACTIVITY_NAME
import org.nameless.view.PopUpViewManager.EXTRA_PACKAGE_NAME

class CircleIconView(
    context: Context,
    val packageName: String,
    val shortcutId: String = String(),
    val shortcutUserId: Int = Int.MIN_VALUE
) : CircleImageView(context) {

    private var fromDown = false
    private var focused = false
    private var downTime = 0L

    init {
        setImageDrawable(IconDrawableHelper.getDrawable(context, launcherApps, this))
        borderColor = Color.parseColor("#80FFFFFF")
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                fromDown = true
                downTime = SystemClock.uptimeMillis()
            }
            MotionEvent.ACTION_MOVE -> {
                if (!focused) {
                    if (!fromDown) {
                        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    }

                    if (!fromDown || SystemClock.uptimeMillis() - downTime >= FOCUS_MIN_TIME_ON_DOWN) {
                        focused = true
                        playScaleUpAnimation()
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                resetState {
                    ViewAnimator.hideCircle {
                        if (shortcutId.isNotBlank() && shortcutUserId != Int.MIN_VALUE) {
                            ShortcutHelper.startShortcut(context, launcherApps, this)
                        } else {
                            sendMiniWindowBroadcast(context, packageName)
                        }
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                resetState()
            }
        }
        return true
    }

    private fun playScaleUpAnimation() {
        val scaleAnimator = ValueAnimator.ofFloat(1.0f, SCALE_FOCUS_VALUE).apply {
            addUpdateListener {
                (it.animatedValue as Float).let { v ->
                    if (scaleX < v) {
                        scaleX = v
                        scaleY = v
                    }
                }
            }
            duration = FOCUS_ANIMATION_DURATION
            interpolator = PathInterpolator(0.17f, 0.0f, 0.53f, 0.7f)
        }
        val borderAnimator = ValueAnimator.ofFloat(0f, width * 0.08f).apply {
            addUpdateListener {
                (it.animatedValue as Float).toInt().let { v ->
                    if (borderWidth < v) {
                        borderWidth = v
                    }
                }
            }
            duration = FOCUS_ANIMATION_DURATION
            interpolator = PathInterpolator(0.17f, 0.0f, 0.53f, 0.7f)
        }
        AnimatorSet().apply {
            playTogether(
                scaleAnimator,
                borderAnimator
            )
            doOnEnd {
                if (!focused) {
                    playScaleDownAnimation()
                }
            }
            start()
        }
    }

    private fun playScaleDownAnimation(endAction: Runnable? = null) {
        val scaleAnimator = ValueAnimator.ofFloat(SCALE_FOCUS_VALUE, 1.0f).apply {
            addUpdateListener {
                (it.animatedValue as Float).let { v ->
                    if (scaleX > v) {
                        scaleX = v
                        scaleY = v
                    }
                }
            }
            duration = FOCUS_ANIMATION_DURATION
            interpolator = PathInterpolator(0.19f, 0.31f, 0.48f, 1.0f)
        }
        val borderAnimator = ValueAnimator.ofFloat(width * 0.08f, 0f).apply {
            addUpdateListener {
                (it.animatedValue as Float).toInt().let { v ->
                    if (borderWidth > v) {
                        borderWidth = v
                    }
                }
            }
            duration = FOCUS_ANIMATION_DURATION
            interpolator = PathInterpolator(0.19f, 0.31f, 0.48f, 1.0f)
        }
        AnimatorSet().apply {
            playTogether(
                scaleAnimator,
                borderAnimator
            )
            doOnEnd {
                endAction?.run()
            }
            start()
        }
    }

    fun resetState(endAction: Runnable? = null) {
        if (focused) {
            fromDown = false
            focused = false
            playScaleDownAnimation(endAction)
        } else {
            endAction?.run()
        }
    }

    fun inTouchRegion(x: Float, y: Float): Boolean {
        return x >= this.x && x <= this.x + width &&
                y >= this.y && y <= this.y + height
    }

    companion object {
        private const val FOCUS_MIN_TIME_ON_DOWN = 100L

        fun sendMiniWindowBroadcast(
            context: Context,
            packageName: String
        ) {
            if (Utils.PACKAGE_NAME == packageName) {
                context.sendBroadcastAsUser(Intent().apply {
                    action = ACTION_START_MINI_WINDOW
                    putExtra(EXTRA_PACKAGE_NAME, Utils.PACKAGE_NAME)
                    putExtra(EXTRA_ACTIVITY_NAME, AllAppsPickerActivity::class.java.name)
                }, UserHandle.SYSTEM)
                return
            }

            context.sendBroadcastAsUser(Intent().apply {
                action = ACTION_START_MINI_WINDOW
                putExtra(EXTRA_PACKAGE_NAME, packageName)
            }, UserHandle.SYSTEM)
        }
    }
}
