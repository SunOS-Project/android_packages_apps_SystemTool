/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.common

import android.animation.ValueAnimator
import android.util.Log
import android.view.View
import android.view.animation.PathInterpolator

import org.nameless.os.DebugConstants

object Utils {

    const val PACKAGE_NAME = "org.nameless.systemtool"

    private const val ITEM_SCALE_VALUE = 0.85f
    private const val ITEM_SCALE_DURATION = 200L

    fun logD(tag: String, msg: String) {
        if (DebugConstants.DEBUG_SYSTEM_TOOL) {
            Log.d(tag, msg)
        }
    }

    fun logE(tag: String, msg: String) {
        Log.e(tag, msg)
    }

    fun logE(tag: String, msg: String, e: Exception) {
        Log.e(tag, msg, e)
    }

    fun playScaleDownAnimation(view: View) {
        ValueAnimator.ofFloat(1.0f, ITEM_SCALE_VALUE).apply {
            addUpdateListener {
                (it.animatedValue as Float).let { v ->
                    if (view.scaleX > v) {
                        view.scaleX = v
                        view.scaleY = v
                    }
                }
            }
            duration = ITEM_SCALE_DURATION
            interpolator = PathInterpolator(0.19f, 0.31f, 0.48f, 1.0f)
        }.start()
    }

    fun playScaleUpAnimation(view: View) {
        ValueAnimator.ofFloat(ITEM_SCALE_VALUE, 1.0f).apply {
            addUpdateListener {
                (it.animatedValue as Float).let { v ->
                    if (view.scaleX < v) {
                        view.scaleX = v
                        view.scaleY = v
                    }
                }
            }
            duration = ITEM_SCALE_DURATION
            interpolator = PathInterpolator(0.17f, 0.0f, 0.53f, 0.7f)
        }.start()
    }
}
