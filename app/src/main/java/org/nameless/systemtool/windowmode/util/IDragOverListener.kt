/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.util

import androidx.recyclerview.widget.RecyclerView

interface IDragOverListener {
    fun startDragItem(holder: RecyclerView.ViewHolder)
}
