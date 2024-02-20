/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap

abstract class GridItemAdapter<T> : BaseAdapter {

    private val dataList: MutableList<T>
    private val layoutRes: Int

    constructor(dataList: MutableList<T>, layoutRes: Int) {
        this.dataList = dataList
        this.layoutRes = layoutRes
    }

    override fun getCount() = dataList.size

    override fun getItem(position: Int) = dataList[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder = AdapterViewHolder.bind(
            parent.context, convertView, parent, layoutRes, position
        )
        bindView(holder, getItem(position))
        return holder.itemView
    }

    abstract fun bindView(holder: AdapterViewHolder?, obj: T)

    fun add(data: T) {
        dataList.add(data)
        notifyDataSetChanged()
    }

    fun add(data: T, position: Int) {
        dataList.add(position, data)
        notifyDataSetChanged()
    }

    fun remove(position: Int): T {
        val data = dataList.removeAt(position)
        notifyDataSetChanged()
        return data
    }

    class AdapterViewHolder constructor(
        private val context: Context,
        parent: ViewGroup,
        layoutRes: Int
    ) {
        private val views : SparseArray<View?> = SparseArray()

        var itemView : View
        private var itemPosition = 0

        init {
            itemView = LayoutInflater.from(context).inflate(layoutRes, parent, false)
            itemView.tag = this
        }

        fun <T : View?> getView(resId: Int): T? {
            var t = views[resId] as T?
            if (t == null) {
                t = itemView.findViewById<View>(resId) as T
                views.put(resId, t)
            }
            return t
        }

        fun setText(resId: Int, text: CharSequence?): AdapterViewHolder {
            val view = getView<View>(resId)!!
            if (view is TextView) {
                view.text = text
            }
            return this
        }

        fun setDrawable(resId: Int, drawable: Drawable?): AdapterViewHolder {
            val view = getView<View>(resId)!!
            if (view is ImageView) {
                view.setImageDrawable(RoundedBitmapDrawableFactory.create(
                    context.resources, drawable?.toBitmap()).apply {
                        cornerRadius = 1000f
                        setAntiAlias(true)
                    })
            }
            return this
        }

        companion object {
            fun bind(
                context: Context,
                convertView: View?,
                parent: ViewGroup,
                layoutRes: Int,
                position: Int
            ): AdapterViewHolder {
                val holder : AdapterViewHolder
                if (convertView == null) {
                    holder = AdapterViewHolder(context, parent, layoutRes)
                } else {
                    holder = convertView.tag as AdapterViewHolder
                    holder.itemView = convertView
                }
                holder.itemPosition = position
                return holder
            }
        }
    }
}
