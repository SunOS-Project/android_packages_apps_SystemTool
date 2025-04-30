/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.common

import android.animation.ValueAnimator
import android.util.Log
import android.view.View
import android.view.animation.PathInterpolator

import org.sun.os.DebugConstants

object Utils {

    const val PACKAGE_NAME = "org.sun.systemtool"

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

    fun playScaleDownAnimation(view: View, scaleFactor: Float, scaleDuration: Long) {
        ValueAnimator.ofFloat(1.0f, scaleFactor).apply {
            addUpdateListener {
                (it.animatedValue as Float).let { v ->
                    if (view.scaleX > v) {
                        view.scaleX = v
                        view.scaleY = v
                    }
                }
            }
            duration = scaleDuration
            interpolator = PathInterpolator(0.19f, 0.31f, 0.48f, 1.0f)
        }.start()
    }

    fun playScaleUpAnimation(view: View, scaleFactor: Float, scaleDuration: Long) {
        ValueAnimator.ofFloat(scaleFactor, 1.0f).apply {
            addUpdateListener {
                (it.animatedValue as Float).let { v ->
                    if (view.scaleX < v) {
                        view.scaleX = v
                        view.scaleY = v
                    }
                }
            }
            duration = scaleDuration
            interpolator = PathInterpolator(0.17f, 0.0f, 0.53f, 0.7f)
        }.start()
    }
}
