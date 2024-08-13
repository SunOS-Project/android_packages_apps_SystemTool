/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.controller

import android.annotation.SuppressLint
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.AudioSystem
import android.telephony.TelephonyManager
import android.widget.Toast

import org.nameless.systemtool.R
import org.nameless.systemtool.gamemode.observer.CallStateListener
import org.nameless.systemtool.gamemode.util.Shared.audioManager
import org.nameless.systemtool.gamemode.util.Shared.service
import org.nameless.systemtool.gamemode.util.Shared.telecomManager

@Suppress("DEPRECATION")
object AutoCallController {

    private val callStateListener by lazy { CallStateListener() }

    enum class Mode {
        OFF,
        AUTO_ACCEPT,
        AUTO_REJECT
    }
    var mode = Mode.OFF
        set(value) {
            field = value
            callStateListener.registered = value != Mode.OFF
        }

    private var previousState = TelephonyManager.CALL_STATE_IDLE
    private var previousAudioMode = audioManager.mode

    @SuppressLint("MissingPermission")
    fun onCallRinging(incomingNumber: String) {
        service.handler.post {
            when (mode) {
                Mode.AUTO_ACCEPT -> {
                    telecomManager.acceptRingingCall()
                    Toast.makeText(
                        service, service.getString(
                            R.string.game_call_accepted_toast, incomingNumber
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Mode.AUTO_REJECT -> {
                    telecomManager.endCall()
                    Toast.makeText(
                        service, service.getString(
                            R.string.game_call_rejected_toast, incomingNumber
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
            previousState = TelephonyManager.CALL_STATE_RINGING
        }
    }

    fun onCallOffHook() {
        service.handler.post {
            if (mode == Mode.AUTO_ACCEPT && previousState == TelephonyManager.CALL_STATE_RINGING) {
                if (isHeadsetPluggedIn()) {
                    audioManager.isSpeakerphoneOn = false
                    AudioSystem.setForceUse(
                        AudioSystem.FOR_COMMUNICATION,
                        AudioSystem.FORCE_NONE
                    )
                } else {
                    audioManager.isSpeakerphoneOn = true
                    AudioSystem.setForceUse(
                        AudioSystem.FOR_COMMUNICATION,
                        AudioSystem.FORCE_SPEAKER
                    )
                }
                audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            }
            previousState = TelephonyManager.CALL_STATE_OFFHOOK
        }
    }

    fun onCallIdle() {
        service.handler.post {
            if (mode == Mode.AUTO_ACCEPT && previousState == TelephonyManager.CALL_STATE_OFFHOOK) {
                audioManager.mode = previousAudioMode
                AudioSystem.setForceUse(
                    AudioSystem.FOR_COMMUNICATION,
                    AudioSystem.FORCE_NONE
                )
                audioManager.isSpeakerphoneOn = false
            }
            previousState = TelephonyManager.CALL_STATE_IDLE
        }
    }

    private fun isHeadsetPluggedIn(): Boolean {
        return audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any {
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
            it.type == AudioDeviceInfo.TYPE_USB_HEADSET
        }
    }
}
