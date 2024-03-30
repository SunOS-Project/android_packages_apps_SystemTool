/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.view

import android.view.View

import org.nameless.systemtool.R
import org.nameless.systemtool.windowmode.callback.IDragOverListener
import org.nameless.systemtool.windowmode.util.Shared.isEditing

class PinnedItemAdapter : BaseItemAdapter() {

    var dragOverListener: IDragOverListener? = null

    override fun onBindViewHolder(holder: AppHolder, position: Int) {
        data[position].let { appInfo ->
            holder.hashCode = appInfo.hashCode()
            holder.iconApp.setImageDrawable(appInfo.icon)
            holder.iconState.setImageResource(R.drawable.ic_remove)
            holder.iconState.visibility = if (isEditing) View.VISIBLE else View.INVISIBLE
            holder.iconState.setOnClickListener {
                if (isEditing) {
                    clickedListener?.onRemovedClicked(appInfo, position)
                }
            }
            holder.textAppLabel.text = appInfo.label

            holder.root.setOnClickListener {
                if (!isEditing) {
                    clickedListener?.onIconClicked(appInfo)
                } else {
                    clickedListener?.onRemovedClicked(appInfo, position)
                }
            }

            holder.root.setOnLongClickListener {
                dragOverListener?.startDragItem(holder)
                return@setOnLongClickListener false
            }
        }
    }
}
