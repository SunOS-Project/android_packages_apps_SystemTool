/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.windowmode.view

import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView

import org.sun.systemtool.R
import org.sun.systemtool.windowmode.bean.AppInfo
import org.sun.systemtool.windowmode.callback.IIconClickedListener

abstract class BaseItemAdapter : RecyclerView.Adapter<AppHolder>() {

    var data: MutableList<AppInfo> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var clickedListener: IIconClickedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_item, parent, false)
        return AppHolder(view)
    }

    override fun getItemCount() = data.size
}
