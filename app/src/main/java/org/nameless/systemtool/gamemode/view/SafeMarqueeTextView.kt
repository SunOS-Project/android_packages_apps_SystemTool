/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.TextView

/**
 * A TextField that doesn't relayout when changing from marquee to ellipsis.
 */
@SuppressLint("AppCompatCustomView")
open class SafeMarqueeTextView(
    context: Context,
    attrs: AttributeSet,
) : TextView(context, attrs) {

    private var safelyIgnoreLayout = false
    private val hasStableWidth
        get() = layoutParams.width != ViewGroup.LayoutParams.WRAP_CONTENT

    override fun requestLayout() {
        if (safelyIgnoreLayout) {
            return
        }
        super.requestLayout()
    }

    override fun startMarquee() {
        val wasIgnoring = safelyIgnoreLayout
        safelyIgnoreLayout = hasStableWidth
        super.startMarquee()
        safelyIgnoreLayout = wasIgnoring
    }

    override fun stopMarquee() {
        val wasIgnoring = safelyIgnoreLayout
        safelyIgnoreLayout = hasStableWidth
        super.stopMarquee()
        safelyIgnoreLayout = wasIgnoring
    }
}
