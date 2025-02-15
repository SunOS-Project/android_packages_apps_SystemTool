/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.UserHandle
import android.os.VibrationExtInfo
import android.provider.Settings
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView

import org.nameless.os.CustomVibrationAttributes.VIBRATION_ATTRIBUTES_QS_TILE
import org.nameless.provider.SettingsExt
import org.nameless.systemtool.R
import org.nameless.systemtool.common.Utils
import org.nameless.systemtool.gamemode.util.Config.ITEM_SCALE_DURATION
import org.nameless.systemtool.gamemode.util.Config.ITEM_SCALE_VALUE
import org.nameless.systemtool.gamemode.util.GameModeListenerProxy

import vendor.nameless.hardware.vibratorExt.Effect.BUTTON_CLICK
import vendor.nameless.hardware.vibratorExt.Effect.CLICK

@SuppressLint("AppCompatCustomView")
class BrightnessLockButton(
    context: Context,
    attrs: AttributeSet
) : ImageView(context, attrs) {

    private val gameModeInfoListener = object : GameModeListenerProxy.Callback {
        override fun onGameModeInfoChanged() {
            brightnessLocked = GameModeListenerProxy.gameModeInfo?.shouldDisableAutoBrightness() ?: false
        }
    }

    private var brightnessLocked: Boolean

    init {
        background = context.getDrawable(R.drawable.bg_brightness_lock)
        brightnessLocked = GameModeListenerProxy.gameModeInfo?.shouldDisableAutoBrightness() ?: false
        updateResources()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        GameModeListenerProxy.addCallback(gameModeInfoListener)
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
                    Utils.playScaleDownAnimation(this, ITEM_SCALE_VALUE, ITEM_SCALE_DURATION)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Utils.playScaleUpAnimation(this, ITEM_SCALE_VALUE, ITEM_SCALE_DURATION)
                }
            }
            return@setOnTouchListener false
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        setOnTouchListener(null)
        setOnClickListener(null)
        GameModeListenerProxy.removeCallback(gameModeInfoListener)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateResources() {
        setImageDrawable(if (brightnessLocked) {
            context.getDrawable(R.drawable.ic_brightness_lock)
        } else {
            context.getDrawable(R.drawable.ic_brightness)
        })

        background.setTint(if (brightnessLocked) {
            context.getColor(android.R.color.system_accent1_600)
        } else {
            context.getColor(R.color.game_panel_background_inactive_default)
        })
    }
}
