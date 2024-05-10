/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.UserHandle
import android.os.VibrationExtInfo
import android.provider.Settings
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView

import org.nameless.app.IGameModeInfoListener
import org.nameless.os.CustomVibrationAttributes.VIBRATION_ATTRIBUTES_QS_TILE
import org.nameless.provider.SettingsExt
import org.nameless.systemtool.R
import org.nameless.systemtool.common.Utils
import org.nameless.systemtool.gamemode.tile.BaseShortcutTile
import org.nameless.systemtool.gamemode.util.Shared.gameModeManager

import vendor.nameless.hardware.vibratorExt.V1_0.Effect.BUTTON_CLICK
import vendor.nameless.hardware.vibratorExt.V1_0.Effect.CLICK

@SuppressLint("AppCompatCustomView")
class BrightnessLockButton(
    context: Context,
    attrs: AttributeSet
) : ImageView(context, attrs) {

    private val gameModeInfoListener = object : IGameModeInfoListener.Stub() {
        override fun onGameModeInfoChanged() {
            brightnessLocked = gameModeManager.gameModeInfo?.shouldDisableAutoBrightness() ?: false
        }
    }

    private var brightnessLocked: Boolean

    init {
        background = context.getDrawable(R.drawable.bg_brightness_lock)
        brightnessLocked = gameModeManager.gameModeInfo?.shouldDisableAutoBrightness() ?: false
        updateResources()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        gameModeManager.registerGameModeInfoListener(gameModeInfoListener)
        setOnClickListener {
            performHapticFeedbackExt(VibrationExtInfo.Builder().apply {
                setEffectId(BUTTON_CLICK)
                setFallbackEffectId(CLICK)
                setVibrationAttributes(VIBRATION_ATTRIBUTES_QS_TILE)
            }.build())
            brightnessLocked = !brightnessLocked
            Settings.System.putIntForUser(
                context.contentResolver,
                SettingsExt.System.GAME_MODE_DISABLE_AUTO_BRIGHTNESS,
                if (brightnessLocked) 1 else 0,
                UserHandle.USER_CURRENT
            )
            updateResources()
        }
        setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    Utils.playScaleDownAnimation(this)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Utils.playScaleUpAnimation(this)
                }
            }
            return@setOnTouchListener false
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        setOnTouchListener(null)
        setOnClickListener(null)
        gameModeManager.unregisterGameModeInfoListener(gameModeInfoListener)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateResources() {
        val outside = context.getDrawable(R.drawable.ic_brightness)
        val inside = if (brightnessLocked) {
            context.getDrawable(R.drawable.ic_brightness_lock)
        } else {
            context.getDrawable(R.drawable.ic_brightness_unlock)
        }
        if (outside == null || inside == null) {
            return
        }
        setImageDrawable(mergeDrawable(outside, inside))

        background.setTint(if (brightnessLocked) {
            context.getColor(android.R.color.system_accent1_600)
        } else {
            Color.parseColor(BaseShortcutTile.DEFAULT_COLOR_INACTIVE_BACKGROUND)
        })
    }

    private fun mergeDrawable(largeDrawable: Drawable, smallDrawable: Drawable): Drawable {
        val badgedWidth = largeDrawable.intrinsicWidth
        val badgedHeight = largeDrawable.intrinsicHeight
        val bitmap = Bitmap.createBitmap(badgedWidth, badgedHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        largeDrawable.setBounds(0, 0, badgedWidth, badgedHeight)
        largeDrawable.draw(canvas)
        smallDrawable.setBounds(
            (badgedWidth * 0.3f).toInt(),
            (badgedHeight * 0.3f).toInt(),
            (badgedWidth * 0.7f).toInt(),
            (badgedHeight * 0.7f).toInt()
        )
        smallDrawable.draw(canvas)
        val mergedDrawable = BitmapDrawable(context.resources, bitmap)
        if (largeDrawable is BitmapDrawable) {
            mergedDrawable.setTargetDensity(largeDrawable.bitmap.density)
        }
        return mergedDrawable
    }
}
