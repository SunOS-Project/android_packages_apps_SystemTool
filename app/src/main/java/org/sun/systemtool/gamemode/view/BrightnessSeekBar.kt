/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.gamemode.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.SystemClock
import android.os.SystemProperties
import android.os.VibrationExtInfo
import android.util.AttributeSet
import android.widget.SeekBar
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart

import com.android.settingslib.display.BrightnessUtils.GAMMA_SPACE_MAX

import kotlin.math.abs

import org.sun.os.CustomVibrationAttributes.VIBRATION_ATTRIBUTES_SLIDER
import org.sun.systemtool.R
import org.sun.systemtool.gamemode.util.BrightnessHelper
import org.sun.systemtool.gamemode.util.Shared.displayManager
import org.sun.systemtool.gamemode.util.Shared.service

import vendor.sun.hardware.vibratorExt.Effect.SLIDER_EDGE
import vendor.sun.hardware.vibratorExt.Effect.SLIDER_STEP

@SuppressLint("AppCompatCustomView")
class BrightnessSeekBar(
    context: Context,
    attrs: AttributeSet
) : SeekBar(context, attrs) {

    private var animating = false
    private var userTracking = false
    private var lastHapticTimestamp = 0L
    private var progressAnimator: ValueAnimator? = null
    private val progressListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (fromUser) {
                onProgressChanged(progress)
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            userTracking = true
            if (animating) {
                progressAnimator?.cancel()
                animating = false
            }
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            userTracking = false
            val percentage = (progress - min).toFloat() / (max - min)
            BrightnessHelper.setBrightnessPercentage(percentage, false)
        }
    }
    private val brightnessChangeObserver = BrightnessChangeObserver()

    init {
        min = 0
        max = GAMMA_SPACE_MAX
        progressDrawable = context.getDrawable(R.drawable.brightness_seek_bar_drawable)
        splitTrack = false
        thumb = null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setOnSeekBarChangeListener(progressListener)
        brightnessChangeObserver.register()
        updateProgress(false)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        brightnessChangeObserver.unregister()
        setOnSeekBarChangeListener(null)
    }

    private fun onProgressChanged(progress: Int) {
        val now = SystemClock.uptimeMillis()
        val percentage = (progress - min).toFloat() / (max - min)
        if (percentage == 0f || percentage == 1f) {
            lastHapticTimestamp = now
            performHapticFeedbackExt(VibrationExtInfo.Builder().apply {
                setEffectId(SLIDER_EDGE)
                setVibrationAttributes(VIBRATION_ATTRIBUTES_SLIDER)
            }.build())
        } else if (now - lastHapticTimestamp > HAPTIC_MIN_INTERVAL) {
            lastHapticTimestamp = now
            performHapticFeedbackExt(VibrationExtInfo.Builder().apply {
                setEffectId(SLIDER_STEP)
                setAmplitude(percentage)
                setVibrationAttributes(VIBRATION_ATTRIBUTES_SLIDER)
            }.build())
        }
        BrightnessHelper.setBrightnessPercentage(percentage, true)
    }

    private fun updateProgress(animate: Boolean) {
        if (animate) {
            progressAnimator = BrightnessHelper.getBrightness().let { newProgress ->
                ValueAnimator.ofInt(progress, newProgress).apply {
                    addUpdateListener {
                        progress = it.animatedValue as Int
                    }
                    duration = SLIDER_ANIMATION_DURATION * abs(progress - newProgress) / GAMMA_SPACE_MAX
                    doOnStart {
                        animating = true
                    }
                    doOnEnd {
                        animating = false
                        progressAnimator = null
                    }
                    start()
                }
            }
        } else {
            progress = BrightnessHelper.getBrightness()
        }
    }

    private inner class BrightnessChangeObserver : DisplayManager.DisplayListener {

        private var registered = false

        override fun onDisplayAdded(displayId: Int) {}

        override fun onDisplayRemoved(displayId: Int) {}

        override fun onDisplayChanged(displayId: Int) {
            if (!userTracking) {
                updateProgress(true)
            }
        }

        fun register() {
            if (registered) {
                return
            }
            displayManager.registerDisplayListener(
                brightnessChangeObserver,
                null,
                DisplayManager.EVENT_FLAG_DISPLAY_BRIGHTNESS
            )
            registered = true
        }

        fun unregister() {
            if (!registered) {
                return
            }
            displayManager.unregisterDisplayListener(brightnessChangeObserver)
            registered = false
        }
    }

    companion object {
        private const val SLIDER_ANIMATION_DURATION = 3000L

        private val HAPTIC_MIN_INTERVAL = SystemProperties.getLong(
                "sys.sun.haptic.slider_interval", 50L)
    }
}
