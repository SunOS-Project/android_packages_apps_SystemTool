/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.view

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.os.UserHandle
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.widget.ImageView

import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap

import com.android.internal.util.nameless.CustomUtils

import org.nameless.edge.util.Constants
import org.nameless.edge.util.ViewHolder
import org.nameless.wm.PopUpBroadcastConstants.ACTION_START_MINI_WINDOW
import org.nameless.wm.PopUpBroadcastConstants.EXTRA_PACKAGE_NAME

class IconView(
    context: Context,
    val packageName: String,
    var centerPosX: Int,
    var centerPosY: Int,
    val radius: Int
) : ImageView(context) {

    private var fromDown = false
    private var focused = false
    private var downTime = 0L

    init {
        CustomUtils.getAppIcon(context, packageName, true /* useDefault */)?.let {
            setImageDrawable(RoundedBitmapDrawableFactory.create(resources, it.toBitmap()).apply {
                cornerRadius = 1000f
                setAntiAlias(true)
            })
        }
        scaleX = 1f / Constants.iconFocusedScaleRatio
        scaleY = 1f / Constants.iconFocusedScaleRatio
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                fromDown = true
                downTime = SystemClock.uptimeMillis()
            }
            MotionEvent.ACTION_MOVE -> {
                if (!focused) {
                    if (!fromDown) {
                        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    }

                    if (!fromDown || SystemClock.uptimeMillis() - downTime >= FOCUSE_MIN_TIME_ON_DOWN) {
                        focused = true
                        animate().scaleX(1f)
                            .scaleY(1f)
                            .setDuration(SCALE_ANIMATION_DURATION)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                resetState()
                ViewHolder.hideForAll()
                postDelayed({
                    sendMiniWindowBroadcast(context, packageName)
                }, 200L)
            }
            MotionEvent.ACTION_CANCEL -> {
                resetState()
            }
        }
        return true
    }

    fun resetState() {
        if (focused) {
            fromDown = false
            focused = false

            animate().scaleX(1f / Constants.iconFocusedScaleRatio)
                .scaleY(1f / Constants.iconFocusedScaleRatio)
                .setDuration(SCALE_ANIMATION_DURATION)
        }
    }

    fun inTouchRegion(x: Float, y: Float): Boolean {
        (radius * scaleX).let {
            return x >= centerPosX - it && x <= centerPosX + it &&
                    y >= centerPosY - it && y <= centerPosY + it
        }
    }

    companion object {
        private val FOCUSE_MIN_TIME_ON_DOWN = 100L

        private val SCALE_ANIMATION_DURATION = 150L

        private fun sendMiniWindowBroadcast(context: Context, packageName: String) {
            context.sendBroadcastAsUser(Intent().apply {
                action = ACTION_START_MINI_WINDOW
                putExtra(EXTRA_PACKAGE_NAME, packageName)
            }, UserHandle.SYSTEM)
        }
    }
}
