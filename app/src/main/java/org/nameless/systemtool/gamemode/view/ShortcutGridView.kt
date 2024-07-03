/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.view

import android.content.Context
import android.os.VibrationExtInfo
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup

import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import org.nameless.os.CustomVibrationAttributes.VIBRATION_ATTRIBUTES_QS_TILE
import org.nameless.systemtool.R
import org.nameless.systemtool.common.Utils
import org.nameless.systemtool.gamemode.tile.AutoCallTile
import org.nameless.systemtool.gamemode.tile.BaseShortcutTile
import org.nameless.systemtool.gamemode.tile.BlockHeadsUpTile
import org.nameless.systemtool.gamemode.tile.BlockThreeFingerGestureTile
import org.nameless.systemtool.gamemode.tile.DanmakuTile
import org.nameless.systemtool.gamemode.tile.KeepWakeTIle
import org.nameless.systemtool.gamemode.tile.MuteNotificationTile
import org.nameless.systemtool.gamemode.tile.PreventMistouchTile
import org.nameless.systemtool.gamemode.tile.ScreenRecordTile
import org.nameless.systemtool.gamemode.tile.ScreenshotTile
import org.nameless.systemtool.gamemode.util.Config.ITEM_SCALE_DURATION
import org.nameless.systemtool.gamemode.util.Config.ITEM_SCALE_VALUE

import vendor.nameless.hardware.vibratorExt.V1_0.Effect.BUTTON_CLICK
import vendor.nameless.hardware.vibratorExt.V1_0.Effect.CLICK

class ShortcutGridView(
    context: Context,
    attrs: AttributeSet
) : RecyclerView(context, attrs) {

    val tileList = mutableListOf(
        ScreenshotTile(),
        ScreenRecordTile(),
        BlockHeadsUpTile(),
        DanmakuTile(),
        MuteNotificationTile(),
        BlockThreeFingerGestureTile(),
        AutoCallTile(),
        PreventMistouchTile(),
        KeepWakeTIle(),
    )

    init {
        layoutManager = GridLayoutManager(
            context, 2, LinearLayoutManager.VERTICAL, false
        )
        adapter = ItemAdapter().apply {
            data = tileList
        }
    }

    private inner class ItemAdapter : Adapter<ShortcutHolder>() {

        var data: MutableList<BaseShortcutTile> = mutableListOf()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getItemCount() = data.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortcutHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.panel_tile_shortcut, parent, false)
            return ShortcutHolder(view)
        }

        override fun onBindViewHolder(holder: ShortcutHolder, position: Int) {
            data[position].let { tile ->
                holder.root.apply {
                    val margin = context.resources.getDimensionPixelSize(R.dimen.game_panel_tiles_margin)
                    updateLayoutParams<LayoutParams> {
                        setMargins(
                            if (position % 2 == 0) margin else margin / 2,
                            if (position <= 1) margin else margin / 2,
                            if (position % 2 == 1) margin else margin / 2,
                            if ((data.size % 2 == 0 && position >= data.size - 2) ||
                                    (data.size % 2 == 1 && position == data.size - 1)) {
                                margin
                            } else margin / 2,
                        )
                    }
                }

                holder.textShortcutLabel.isSelected = true
                holder.textShortcutSecondaryLabel.isSelected = true

                tile.bind(
                    holder.root,
                    holder.iconShortcut,
                    holder.textShortcutLabel,
                    holder.textShortcutSecondaryLabel
                )

                holder.tile = tile
                holder.root.setOnClickListener {
                    holder.root.performHapticFeedbackExt(VibrationExtInfo.Builder().apply {
                        setEffectId(BUTTON_CLICK)
                        setFallbackEffectId(CLICK)
                        setVibrationAttributes(VIBRATION_ATTRIBUTES_QS_TILE)
                    }.build())
                    tile.onClicked()
                }
                if (tile.longClickable) {
                    holder.root.setOnLongClickListener {
                        tile.onLongClicked()
                        return@setOnLongClickListener false
                    }
                }

                holder.root.setOnTouchListener { _, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            Utils.playScaleDownAnimation(holder.root, ITEM_SCALE_VALUE, ITEM_SCALE_DURATION)
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            Utils.playScaleUpAnimation(holder.root, ITEM_SCALE_VALUE, ITEM_SCALE_DURATION)
                        }
                    }
                    return@setOnTouchListener false
                }

                tile.onAttach()
            }
        }
    }
}
