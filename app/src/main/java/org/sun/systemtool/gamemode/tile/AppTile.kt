/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.gamemode.tile

import android.content.Context
import android.os.VibrationExtInfo
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.LinearLayout

import org.sun.os.CustomVibrationAttributes.VIBRATION_ATTRIBUTES_QS_TILE
import org.sun.systemtool.R
import org.sun.systemtool.common.BroadcastSender
import org.sun.systemtool.common.IconDrawableHelper
import org.sun.systemtool.common.ShortcutHelper
import org.sun.systemtool.common.Utils
import org.sun.systemtool.gamemode.controller.GamePanelViewController
import org.sun.systemtool.gamemode.util.Config.ITEM_SCALE_DURATION
import org.sun.systemtool.gamemode.util.Config.ITEM_SCALE_VALUE
import org.sun.systemtool.gamemode.util.Shared.launcherApps

import vendor.sun.hardware.vibratorExt.Effect.BUTTON_CLICK
import vendor.sun.hardware.vibratorExt.Effect.CLICK

class AppTile(
    context: Context,
    val packageName: String,
    val shortcutId: String = String(),
    val shortcutUserId: Int = Int.MIN_VALUE
) : LinearLayout(context) {

    private val appTileIcon by lazy { findViewById<ImageView>(R.id.app_tile_icon)!! }

    init {
        LayoutInflater.from(context).inflate(R.layout.panel_tile_app, this, true)
        appTileIcon.setImageDrawable(
            IconDrawableHelper.getDrawable(context, launcherApps, this)
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        appTileIcon.setOnClickListener {
            performHapticFeedbackExt(VibrationExtInfo.Builder().apply {
                setEffectId(BUTTON_CLICK)
                setFallbackEffectId(CLICK)
                setVibrationAttributes(VIBRATION_ATTRIBUTES_QS_TILE)
            }.build())
            GamePanelViewController.animateHide {
                if (shortcutId.isNotBlank() && shortcutUserId != Int.MIN_VALUE) {
                    ShortcutHelper.startShortcut(context, this)
                } else {
                    BroadcastSender.sendStartPackageBroadcast(context, packageName)
                }
            }
        }
        appTileIcon.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    Utils.playScaleDownAnimation(appTileIcon, ITEM_SCALE_VALUE, ITEM_SCALE_DURATION)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Utils.playScaleUpAnimation(appTileIcon, ITEM_SCALE_VALUE, ITEM_SCALE_DURATION)
                }
            }
            return@setOnTouchListener false
        }
    }

    override fun onDetachedFromWindow() {
        appTileIcon.setOnTouchListener(null)
        appTileIcon.setOnClickListener(null)
        super.onDetachedFromWindow()
    }
}
