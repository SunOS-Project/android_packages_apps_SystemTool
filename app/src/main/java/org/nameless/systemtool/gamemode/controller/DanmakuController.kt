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

    private const val TEXT_SIZE_RATIO = 0.048f

    private const val DISPLAY_AREA_PORT = 0.08f
    private const val DISPLAY_AREA_LAND = 0.16f

    private const val SCROLL_DURATION_PORT = 7000
    private const val SCROLL_DURATION_LAND = 12000

    private const val HEIGHT_RATIO_PORT = 0.16f
    private const val HEIGHT_RATIO_LAND = 0.16f

    init {
        Danmakus.Options.apply {
            antiCoverEnabled = true
        }
        Danmakus.Globals.apply {
            baseTextSize = screenShortWidth * TEXT_SIZE_RATIO
        }
    }

    private const val TAG = "SystemTool::DanmakuController"

    private val notificationListener by lazy { NotificationListener() }

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
    }

    private var danmakuView: DanmakuView? = null

    private var listening = false

    private var runTime = 0L

    fun startListening() {
        service.mainHandler.post {
            if (listening) {
                return@post
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
                setDisplayArea(if (portrait) DISPLAY_AREA_PORT else DISPLAY_AREA_LAND)
                setScrollDuration(if (portrait) SCROLL_DURATION_PORT else SCROLL_DURATION_LAND)
            }
            windowManager.addView(danmakuView, LayoutParams().apply {
                copyFrom(layoutParams)
                width = screenWidth
                y = if (portrait) {
                    screenShortWidth * HEIGHT_RATIO_PORT
                } else {
                    screenShortWidth * HEIGHT_RATIO_LAND
                }.toInt()
            })
            notificationListener.registered = true
        }
    }

    fun stopListening() {
        service.mainHandler.post {
            if (!listening) {
                return@post
            }
            logD(TAG, "stopListening")

            listening = false
            notificationListener.registered = false

            danmakuView?.finish()
            windowManager.removeViewImmediate(danmakuView)
            danmakuView = null
        }
    }

    fun updateLayout() {
        if (!listening) {
            return
        }
        logD(TAG, "updateLayout")

        notificationListener.suspended = true

        // Force remove current danmaku
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
            setDisplayArea(if (portrait) DISPLAY_AREA_PORT else DISPLAY_AREA_LAND)
            setScrollDuration(if (portrait) SCROLL_DURATION_PORT else SCROLL_DURATION_LAND)
        }
        windowManager.addView(danmakuView, LayoutParams().apply {
            copyFrom(layoutParams)
            width = screenWidth
            y = if (portrait) {
                screenShortWidth * HEIGHT_RATIO_PORT
            } else {
                screenShortWidth * HEIGHT_RATIO_LAND
            }.toInt()
        })

        notificationListener.suspended = false
    }

    fun postDanmaku(packageName: String, content: String) {
        service.mainHandler.post {
            logD(TAG, "postDanmaku, packageName=$packageName, content=$content")
            val icon = IconDrawableHelper.getDrawable(service, packageName)
            danmakuView?.setDanmakus(
                listOf(
                    DanmakuItem(
                        text = "  $content",
                        time = runTime,
                        type = Danmaku.TYPE_RL,
                        color = Color.WHITE,
                        priority = Danmakus.Constants.PRIORITY_MAX,
                        avatar = icon
                    )
                )
            )
        }
    }
}
