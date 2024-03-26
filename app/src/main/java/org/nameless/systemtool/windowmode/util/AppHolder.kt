/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.util

import android.view.View
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import org.nameless.systemtool.R

class AppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val root = itemView
    val iconApp: ImageView by lazy { itemView.findViewById(R.id.icon_app)!! }
    val iconState: ImageView by lazy { itemView.findViewById(R.id.icon_state)!! }
    val textAppLabel: TextView by lazy { itemView.findViewById(R.id.text_app_label)!! }
    var hashCode = 0
}
