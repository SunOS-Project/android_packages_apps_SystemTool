/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.view

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ScrollView

import org.nameless.systemtool.R
import org.nameless.systemtool.gamemode.controller.GamePanelViewController
import org.nameless.systemtool.gamemode.util.Shared.screenShortWidth

class GamePanelView(
    context: Context,
    attrs: AttributeSet
) : LinearLayout(context, attrs) {

    val scrollViewApps by lazy { findViewById<ScrollView>(R.id.sv_apps)!! }
    val recycleViewShortcuts by lazy { findViewById<ShortcutGridView>(R.id.rv_shortcut_tiles)!! }

    private val gestureDetector = GestureDetector(context, object: SimpleOnGestureListener() {
        override fun onSingleTapUp(event: MotionEvent): Boolean {
            if (!inTouchRegion(event)) {
                GamePanelViewController.animateHide()
            }
            return super.onSingleTapUp(event)
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (GamePanelViewController.animating) {
            return true
        }
        return gestureDetector.onTouchEvent(event)
    }

    private fun inTouchRegion(event: MotionEvent): Boolean {
        val x = event.rawX.toInt()
        val y = event.rawY.toInt()
        return x >= (screenShortWidth - layoutParams.width) / 2 &&
                x <= (screenShortWidth + layoutParams.width) / 2 &&
                (layoutParams as WindowManager.LayoutParams).let {
                    y >= it.y && y <= it.y + it.height
                }
    }
}
