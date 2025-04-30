/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.windowmode.util

import androidx.recyclerview.widget.DiffUtil

import org.sun.systemtool.windowmode.bean.AppInfo
import org.sun.systemtool.windowmode.view.BaseItemAdapter

class AdapterDifferHelper(
    private val oldData: MutableList<AppInfo>,
    private val dispatchTo: BaseItemAdapter
) {

    fun start() {
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return oldData.size
            }

            override fun getNewListSize(): Int {
                return dispatchTo.data.size
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return true
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldData[oldItemPosition] == dispatchTo.data[newItemPosition]
            }
        }).apply {
            dispatchUpdatesTo(dispatchTo)
        }
    }
}
