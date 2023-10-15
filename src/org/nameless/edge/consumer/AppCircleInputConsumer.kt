/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.consumer

import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent
import android.view.ViewConfiguration

import com.android.systemui.shared.system.InputMonitorCompat

import java.util.function.Consumer

import org.nameless.edge.R
import org.nameless.edge.util.IconLayoutAlgorithm
import org.nameless.edge.util.ViewHolder

class AppCircleInputConsumer(
    private val context: Context,
    private val inputMonitor: InputMonitorCompat?
) : InputConsumer {

    private val downPos = PointF()
    private val lastPos = PointF()
    private val startDragPos = PointF()

    private var passedSlop = false

    private var distance = 0f

    private val angleThreshold: Int
    private val dragDistThreshold: Float
    private val squaredSlop: Float

    init {
        context.resources.let { res ->
            angleThreshold = res.getInteger(R.integer.gesture_corner_deg_threshold)
            dragDistThreshold = res.getDimension(R.dimen.gestures_drag_threshold)
        }

        ViewConfiguration.get(context).scaledTouchSlop.toFloat().let {
            squaredSlop = it * it
        }
    }

    override fun onMotionEvent(ev: MotionEvent, fromLeft: Boolean) {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downPos.set(ev.x, ev.y)
                lastPos.set(downPos)
                ViewHolder.dimmerView?.offsetX = IconLayoutAlgorithm.navbarHeight
            }
            MotionEvent.ACTION_MOVE -> {
                if (ViewHolder.currentlyVisible) {
                    ViewHolder.dimmerView?.onTouchEvent(ev)
                    return
                }
                lastPos.set(ev.x, ev.y)
                if (!passedSlop) {
                    // Normal gesture, ensure we pass the slop before we start tracking the gesture
                    if (squaredHypot(lastPos.x - downPos.x, lastPos.y - downPos.y) > squaredSlop) {
                        passedSlop = true
                        startDragPos.set(lastPos.x, lastPos.y)
                        if (isValidGestureAngle(downPos.x - lastPos.x, downPos.y - lastPos.y)) {
                            setActive(ev)
                        }
                    }
                } else {
                    // Movement
                    distance = Math.hypot(
                        (lastPos.x - startDragPos.x).toDouble(),
                        (lastPos.y - startDragPos.y).toDouble()).toFloat()
                    if (distance >= 0) {
                        maybeShowDimmerView(ev, fromLeft)
                    }
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (ViewHolder.currentlyVisible) {
                    ViewHolder.dimmerView?.onTouchEvent(ev)
                }
                ViewHolder.dimmerView?.offsetX = 0
                passedSlop = false
            }
        }
    }

    protected fun setActive(ev: MotionEvent) {
        inputMonitor?.pilferPointers()
    }

    private fun maybeShowDimmerView(ev: MotionEvent, fromLeft: Boolean) {
        if (distance >= dragDistThreshold && !ViewHolder.currentlyVisible) {
            ViewHolder.showForAll(fromLeft)
        }
    }

    /**
     * Determine if angle is larger than threshold for gesture detection
     */
    private fun isValidGestureAngle(deltaX: Float, deltaY: Float): Boolean {
        var angle = Math.toDegrees(Math.atan2(deltaY.toDouble(), deltaX.toDouble())).toFloat()

        // normalize so that angle is measured clockwise from horizontal in the bottom right corner
        // and counterclockwise from horizontal in the bottom left corner
        angle = if (angle > 90) 180 - angle else angle
        return angle > angleThreshold && angle < 90
    }

    public fun squaredHypot(x: Float, y: Float): Float {
        return x * x + y * y
    }
}
