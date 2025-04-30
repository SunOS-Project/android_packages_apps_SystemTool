/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.gamemode.view

import android.view.View
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import org.sun.systemtool.R
import org.sun.systemtool.gamemode.tile.BaseShortcutTile

class ShortcutHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tile: BaseShortcutTile? = null
    val root = itemView
    val iconShortcut: ImageView by lazy { itemView.findViewById(R.id.icon_shortcut)!! }
    val textShortcutLabel: TextView by lazy { itemView.findViewById(R.id.text_shortcut_label)!! }
    val textShortcutSecondaryLabel: TextView by lazy { itemView.findViewById(R.id.text_shortcut_secondary_label)!! }
}
