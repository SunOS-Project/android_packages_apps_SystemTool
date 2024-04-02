/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.windowmode.view

import android.animation.ValueAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator

import androidx.recyclerview.widget.RecyclerView

import org.nameless.systemtool.R
import org.nameless.systemtool.windowmode.bean.AppInfo
import org.nameless.systemtool.windowmode.callback.IIconClickedListener
import org.nameless.systemtool.windowmode.util.Config.GRID_ITEM_ANIMATION_DURATION
import org.nameless.systemtool.windowmode.util.Config.SCALE_GRID_ITEM_VALUE

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

    fun playScaleDownAnimation(view: View) {
        ValueAnimator.ofFloat(1.0f, SCALE_GRID_ITEM_VALUE).apply {
            addUpdateListener {
                (it.animatedValue as Float).let { v ->
                    if (view.scaleX > v) {
                        view.scaleX = v
                        view.scaleY = v
                    }
                }
            }
            duration = GRID_ITEM_ANIMATION_DURATION
            interpolator = PathInterpolator(0.19f, 0.31f, 0.48f, 1.0f)
        }.start()
    }

    fun playScaleUpAnimation(view: View) {
        ValueAnimator.ofFloat(SCALE_GRID_ITEM_VALUE, 1.0f).apply {
            addUpdateListener {
                (it.animatedValue as Float).let { v ->
                    if (view.scaleX < v) {
                        view.scaleX = v
                        view.scaleY = v
                    }
                }
            }
            duration = GRID_ITEM_ANIMATION_DURATION
            interpolator = PathInterpolator(0.17f, 0.0f, 0.53f, 0.7f)
        }.start()
    }
}
