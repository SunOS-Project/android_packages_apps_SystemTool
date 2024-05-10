/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.UserHandle

import com.android.systemui.screenrecord.IRemoteRecording

import org.nameless.systemtool.gamemode.util.Shared.service

object ScreenRecordHelper {

    var recorder: IRemoteRecording? = null
    private var isRecorderBound = false

    private val recorderConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                recorder = IRemoteRecording.Stub.asInterface(service)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            recorder = null
        }
    }

    fun bind() {
        isRecorderBound = service.bindServiceAsUser(Intent().apply {
            component = ComponentName(
                "com.android.systemui",
                "com.android.systemui.screenrecord.RecordingService"
            )
        }, recorderConnection, Context.BIND_AUTO_CREATE, UserHandle.CURRENT)
    }

    fun unbind() {
        if (isRecorderBound) {
            service.unbindService(recorderConnection)
        }
        recorder = null
    }
}
