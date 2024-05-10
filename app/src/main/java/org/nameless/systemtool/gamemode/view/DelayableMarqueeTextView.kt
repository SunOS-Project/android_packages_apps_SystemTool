/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.view

import android.content.Context
import android.util.AttributeSet

class DelayableMarqueeTextView(
    context: Context,
    attrs: AttributeSet,
) : SafeMarqueeTextView(context, attrs) {

    private var wantsMarquee = false
    private var marqueeBlocked = true

    private val enableMarquee = Runnable {
        if (wantsMarquee) {
            marqueeBlocked = false
            startMarquee()
        }
    }

    override fun startMarquee() {
        if (!isSelected) {
            return
        }
        wantsMarquee = true
        if (marqueeBlocked) {
            if (handler?.hasCallbacks(enableMarquee) == false) {
                postDelayed(enableMarquee, MARQUEE_DELAY)
            }
            return
        }
        super.startMarquee()
    }

    override fun stopMarquee() {
        handler?.removeCallbacks(enableMarquee)
        wantsMarquee = false
        marqueeBlocked = true
        super.stopMarquee()
    }

    companion object {
        const val MARQUEE_DELAY = 1500L
    }
}
