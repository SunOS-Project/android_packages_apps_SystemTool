/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.AttributeSet
import android.widget.TextView

import org.nameless.systemtool.gamemode.util.Shared.batteryManager

class BatteryView(
    context: Context,
    attrs: AttributeSet
) : TextView(context, attrs) {

    private val batteryChangedReceiver = BatteryChangedReceiver()

    private var batteryLevel = 100
    private var plugged = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        plugged = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS) ==
                BatteryManager.BATTERY_STATUS_CHARGING
        batteryChangedReceiver.register()
    }

    override fun onDetachedFromWindow() {
        batteryChangedReceiver.unregister()
        super.onDetachedFromWindow()
    }

    private fun updateText() {
        text = if (plugged) {
            "\u26A1\uFE0E ${batteryLevel}%"
        } else {
            "${batteryLevel}%"
        }
    }

    private inner class BatteryChangedReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Intent.ACTION_BATTERY_CHANGED == intent.action) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                if (level < 0 || scale < 0) {
                    return
                }
                val newLevel = (level * 100 / scale.toFloat()).toInt()
                val newPlugged = status != 0

                if (newLevel != batteryLevel || newPlugged != plugged) {
                    batteryLevel = newLevel
                    plugged = newPlugged
                    updateText()
                }
            }
        }

        fun register() {
            context.registerReceiver(this, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        }

        fun unregister() {
            context.unregisterReceiver(this)
        }
    }
}
