/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.view

import android.view.MotionEvent
import android.view.View

import org.nameless.systemtool.R
import org.nameless.systemtool.windowmode.util.Shared.isEditing

class AllItemAdapter : BaseItemAdapter() {

    override fun onBindViewHolder(holder: AppHolder, position: Int) {
        data[position].let { appInfo ->
            holder.hashCode = appInfo.hashCode()
            holder.iconApp.setImageDrawable(appInfo.icon)
            holder.iconState.setImageResource(R.drawable.ic_add)
            holder.iconState.visibility = if (isEditing) View.VISIBLE else View.INVISIBLE
            holder.iconState.setOnClickListener {
                if (isEditing) {
                    clickedListener?.onAddClicked(appInfo)
                }
            }
            holder.textAppLabel.text = appInfo.label

            holder.root.setOnClickListener {
                if (!isEditing) {
                    clickedListener?.onIconClicked(appInfo)
                } else {
                    clickedListener?.onAddClicked(appInfo)
                }
            }

            holder.root.setOnTouchListener { _, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        playScaleDownAnimation(holder.root)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        playScaleUpAnimation(holder.root)
                    }
                }
                return@setOnTouchListener false
            }
        }
    }
}
