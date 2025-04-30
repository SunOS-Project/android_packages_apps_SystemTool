/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("DEPRECATION")

package org.sun.systemtool.gamemode.observer

import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager

import org.sun.systemtool.gamemode.controller.AutoCallController
import org.sun.systemtool.gamemode.util.Shared.telephonyManager

class CallStateListener : PhoneStateListener() {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            if (value) {
                telephonyManager.listen(this, LISTEN_CALL_STATE)
            } else {
                telephonyManager.listen(this, LISTEN_NONE)
            }
            field = value
        }

    override fun onCallStateChanged(state: Int, incomingNumber: String) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                AutoCallController.onCallRinging(incomingNumber)
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                AutoCallController.onCallOffHook()
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                AutoCallController.onCallIdle()
            }
        }
    }
}
