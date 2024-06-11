/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.controller

import android.graphics.Color
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager.LayoutParams

import org.nameless.systemtool.R
import org.nameless.systemtool.common.IconDrawableHelper
import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.gamemode.observer.NotificationListener
import org.nameless.systemtool.gamemode.util.Shared.portrait
import org.nameless.systemtool.gamemode.util.Shared.screenShortWidth
import org.nameless.systemtool.gamemode.util.Shared.screenWidth
import org.nameless.systemtool.gamemode.util.Shared.service
import org.nameless.systemtool.gamemode.util.Shared.windowManager

import top.littlefogcat.easydanmaku.Danmakus
import top.littlefogcat.easydanmaku.danmakus.DanmakuItem
import top.littlefogcat.easydanmaku.danmakus.views.Danmaku
import top.littlefogcat.easydanmaku.ui.DanmakuView

object DanmakuController {

    init {
        Danmakus.Options.apply {
            antiCoverEnabled = true
        }
        Danmakus.Globals.baseTextSize = screenShortWidth * 0.048f
    }

    private const val TAG = "SystemTool::DanmakuController"

    private val notificationListener by lazy { NotificationListener(service.handler) }

    private val layoutParams = LayoutParams().apply {
        gravity = Gravity.TOP
        height = LayoutParams.MATCH_PARENT
        format = PixelFormat.TRANSLUCENT
        flags = LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                LayoutParams.FLAG_NOT_FOCUSABLE or
                LayoutParams.FLAG_NOT_TOUCHABLE or
                LayoutParams.FLAG_HARDWARE_ACCELERATED
        privateFlags = LayoutParams.PRIVATE_FLAG_TRUSTED_OVERLAY or
                LayoutParams.SYSTEM_FLAG_SHOW_FOR_ALL_USERS
        type = LayoutParams.TYPE_APPLICATION_OVERLAY
        windowAnimations = 0
    }

    private var danmakuView: DanmakuView? = null

    private var listening = false

    private var runTime = 0L

    fun startListening() {
        if (listening) {
            return
        }
        logD(TAG, "startListening")
        listening = true
        val currentTime = System.currentTimeMillis()
        danmakuView = View.inflate(service, R.layout.layout_danmaku, null) as DanmakuView?
        danmakuView?.apply {
            setShow(true)
            setActionOnFrame {
                runTime = System.currentTimeMillis() - currentTime
                time = runTime
            }
            setDisplayArea(if (portrait) 0.08f else 0.16f)
            setScrollDuration(if (portrait) 7000 else 12000)
        }
        windowManager.addView(danmakuView, LayoutParams().apply {
            copyFrom(layoutParams)
            width = screenWidth
            y = if (portrait) {
                screenShortWidth * 0.16f
            } else {
                screenShortWidth * 0.1f
            }.toInt()
        })
        notificationListener.registered = true
    }

    fun stopListening() {
        if (!listening) {
            return
        }
        logD(TAG, "stopListening")
        listening = false
        notificationListener.registered = false
        danmakuView?.finish()
        windowManager.removeViewImmediate(danmakuView)
        danmakuView = null
    }

    fun updateLayout() {
        if (!listening) {
            return
        }
        logD(TAG, "updateLayout")
        notificationListener.suspended = true
        danmakuView?.finish()
        windowManager.removeViewImmediate(danmakuView)
        val currentTime = System.currentTimeMillis()
        danmakuView = View.inflate(service, R.layout.layout_danmaku, null) as DanmakuView?
        danmakuView?.apply {
            setShow(true)
            setActionOnFrame {
                runTime = System.currentTimeMillis() - currentTime
                time = runTime
            }
            setDisplayArea(if (portrait) 0.08f else 0.16f)
            setScrollDuration(if (portrait) 7000 else 12000)
        }
        windowManager.addView(danmakuView, LayoutParams().apply {
            copyFrom(layoutParams)
            width = screenWidth
            y = if (portrait) {
                screenShortWidth * 0.16f
            } else {
                screenShortWidth * 0.1f
            }.toInt()
        })
        notificationListener.suspended = false
    }

    fun postDanmaku(packageName: String, content: String) {
        logD(TAG, "postDanmaku, packageName=$packageName, content=$content")
        val icon = IconDrawableHelper.getDrawable(service, packageName)
        danmakuView?.setDanmakus(listOf(
            DanmakuItem(
                text = "  $content",
                time = runTime,
                type = Danmaku.TYPE_RL,
                color = Color.WHITE,
                priority = Danmakus.Constants.PRIORITY_MAX,
                avatar = icon
            )
        ))
    }
}
