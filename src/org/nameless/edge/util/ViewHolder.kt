/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.res.Configuration.ORIENTATION_UNDEFINED
import android.view.WindowManager
import android.view.WindowManager.LayoutParams

import androidx.core.view.isVisible

import org.nameless.edge.view.DimmerView
import org.nameless.edge.view.IconView
import org.nameless.wm.PopUpDebugHelper.logD
import org.nameless.wm.PopUpDebugHelper.logE

object ViewHolder {

    private val TAG = "EdgeTool::ViewHolder"

    private val CROSSFADE_ANIMATION_DURATION = 50L

    private var wm: WindowManager? = null

    var dimmerView: DimmerView? = null
    private val iconViewsLeft: MutableList<IconView> = mutableListOf()
    private val iconViewsRight: MutableList<IconView> = mutableListOf()
    val iconViewsShowing: MutableList<IconView> = mutableListOf()

    private var lastOrientation = ORIENTATION_UNDEFINED

    var currentlyVisible = false
    var allowVisible = true

    fun getWindowManager(context: Context): WindowManager? {
        if (wm == null) {
            wm = context.getSystemService(WindowManager::class.java)
        }
        return wm
    }

    private fun addView(
        context: Context,
        view: IconView?,
        params: LayoutParams,
        isLeft: Boolean
    ): Boolean {
        if (getWindowManager(context) == null) {
            logE(TAG, "Failed to addView: WindowManager is null")
            return false
        }
        if (view == null) {
            return false
        }
        try {
            wm?.addView(view, params)
            logD(TAG, "addView: packageName=${view.packageName}, isLeft=$isLeft")
            return true
        } catch (e: Exception) {
            logE(TAG, "Exception on addView for packageName=${view.packageName}, isLeft=$isLeft")
            return false
        }
    }

    private fun removeView(
        context: Context,
        view: IconView,
        isLeft: Boolean
    ) {
        if (getWindowManager(context) == null) {
            logE(TAG, "Failed to removeView: WindowManager is null")
            return
        }
        try {
            wm?.removeView(view)
            logD(TAG, "removeView: packageName=${view.packageName}, isLeft=$isLeft")
        } catch (e: Exception) {
            logE(TAG, "Exception on removeView for packageName=${view.packageName}, isLeft=$isLeft")
        }
    }

    fun showForAll(isLeft: Boolean) {
        if (!allowVisible) {
            logD(TAG, "showForAll: Set visible is disabled, return early")
            return
        }

        if (currentlyVisible) {
            logD(TAG, "showForAll: Already showing, return early")
            return
        }

        val views = if (isLeft) iconViewsLeft else iconViewsRight
        if (views.size == 0) {
            logD(TAG, "showForAll: No icons, return early")
            return
        }

        logD(TAG, "showForAll: isLeft=$isLeft")
        views.forEach {
            it.postOnAnimation {
                iconViewsShowing.add(it)
                it.alpha = 0f
                it.isVisible = true
                it.animate().alpha(1f).setDuration(CROSSFADE_ANIMATION_DURATION).setListener(null)
            }
        }
        dimmerView?.postOnAnimation {
            dimmerView?.isVisible = true
        }

        currentlyVisible = true
    }

    fun hideForAll() {
        if (!currentlyVisible) {
            logD(TAG, "hideForAll: Already invisible, return early")
            return
        }

        if (iconViewsShowing.size == 0) {
            logD(TAG, "hideForAll: No icons, return early")
            return
        }

        logD(TAG, "hideForAll")
        iconViewsShowing.forEach {
            it.postOnAnimation {
                it.animate().alpha(0f).setDuration(CROSSFADE_ANIMATION_DURATION).setListener(
                    object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            it.isVisible = false
                            it.resetState()
                            super.onAnimationEnd(animation)
                        }
                    }
                )
            }
        }
        dimmerView?.postOnAnimation {
            dimmerView?.isVisible = false
        }

        iconViewsShowing.clear()
        currentlyVisible = false
    }

    fun safelyClearIconViews(context: Context) {
        hideForAll()

        iconViewsLeft.forEach { removeView(context, it, true) }
        iconViewsRight.forEach { removeView(context, it, false) }
        iconViewsLeft.clear()
        iconViewsRight.clear()
    }

    fun addIconView(context: Context, packageName: String, idx: Int, total: Int) {
        val params = IconLayoutAlgorithm.getDefaultIconLayoutParams()
        val iconRadius = IconLayoutAlgorithm.getIconRadius(context)
        params.width = iconRadius * 2
        params.height = iconRadius * 2

        var viewLeft: IconView?
        IconLayoutAlgorithm.getIconCenterPos(context, true, idx, total).let {
            params.x = it.first - iconRadius
            params.y = it.second - iconRadius
            viewLeft = IconView(context.createWindowContext(
                    LayoutParams.TYPE_APPLICATION_OVERLAY, null),
                    packageName, it.first, it.second, iconRadius)
            viewLeft?.isVisible = false
            if (!addView(context, viewLeft, params, true)) {
                viewLeft = null
            }
        }

        var viewRight: IconView?
        IconLayoutAlgorithm.getIconCenterPos(context, false, idx, total).let {
            params.x = it.first - iconRadius
            params.y = it.second - iconRadius
            viewRight = IconView(context.createWindowContext(
                    LayoutParams.TYPE_APPLICATION_OVERLAY, null),
                    packageName, it.first, it.second, iconRadius)
            viewRight?.isVisible = false
            if (!addView(context, viewRight, params, false)) {
                viewRight = null
            }
        }

        if (viewLeft != null && viewRight != null) {
            iconViewsLeft.add(viewLeft!!)
            iconViewsRight.add(viewRight!!)
        }
    }

    fun onScreenOrientationChanged(context: Context, newOrientation: Int) {
        if (newOrientation != lastOrientation) {
            lastOrientation = newOrientation
            relocateIconView(context)
        }
    }

    fun relocateIconView(context: Context) {
        hideForAll()

        if (getWindowManager(context) == null) {
            logE(TAG, "Failed to relocateIconView, WindowManager is null")
            return
        }

        val params = IconLayoutAlgorithm.getDefaultIconLayoutParams()
        val iconRadius = IconLayoutAlgorithm.getIconRadius(context)
        params.width = iconRadius * 2
        params.height = iconRadius * 2
        iconViewsLeft.forEachIndexed { i, v ->
            IconLayoutAlgorithm.getIconCenterPos(context, true, i + 1, iconViewsLeft.size).let {
                params.x = it.first - iconRadius
                params.y = it.second - iconRadius
                v.centerPosX = it.first
                v.centerPosY = it.second
                v.radius = iconRadius
                try {
                    wm?.updateViewLayout(v, params)
                    logD(TAG, "relocateIconView: packageName=${v.packageName}, isLeft=true")
                } catch (e: Exception) {
                    logE(TAG, "Exception on relocateIconView for packageName=${v.packageName}, isLeft=true")
                }
            }
        }
        iconViewsRight.forEachIndexed { i, v ->
            IconLayoutAlgorithm.getIconCenterPos(context, false, i + 1, iconViewsRight.size).let {
                params.x = it.first - iconRadius
                params.y = it.second - iconRadius
                v.centerPosX = it.first
                v.centerPosY = it.second
                v.radius = iconRadius
                try {
                    wm?.updateViewLayout(v, params)
                    logD(TAG, "relocateIconView: packageName=${v.packageName}, isLeft=false")
                } catch (e: Exception) {
                    logE(TAG, "Exception on relocateIconView for packageName=${v.packageName}, isLeft=false")
                }
            }
        }
    }
}
