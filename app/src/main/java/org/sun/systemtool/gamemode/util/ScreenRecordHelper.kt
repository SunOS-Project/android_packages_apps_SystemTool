/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.gamemode.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.UserHandle

import com.android.systemui.screenrecord.IRecordingCallback
import com.android.systemui.screenrecord.IRemoteRecording

import org.sun.systemtool.gamemode.util.Shared.service

object ScreenRecordHelper : IRecordingCallback.Stub() {

    private var recorder: IRemoteRecording? = null
    private var isRecorderBound = false

    private val callbacks = mutableListOf<ScreenRecordCallback>()

    private val recorderConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            try {
                recorder = IRemoteRecording.Stub.asInterface(service)
                recorder?.addRecordingCallback(this@ScreenRecordHelper)
                isRecording().let { recording ->
                    callbacks.forEach {
                        if (recording) {
                            it.onStartRecording()
                        } else {
                            it.onStopRecording()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            recorder = null
        }
    }

    override fun onRecordingStart() {
        callbacks.forEach { it.onStartRecording() }
    }

    override fun onRecordingEnd() {
        callbacks.forEach { it.onStopRecording() }
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
        recorder?.removeRecordingCallback(this)
        recorder = null
    }

    fun addCallback(callback: ScreenRecordCallback) {
        callbacks.add(callback)
    }

    fun removeCallback(callback: ScreenRecordCallback) {
        callbacks.remove(callback)
    }

    fun isStarting() = recorder?.isStarting == true
    fun isRecording() = recorder?.isRecording == true

    fun startRecord() {
        recorder?.startRecording()
    }

    fun stopRecord() {
        recorder?.stopRecording()
    }

    interface ScreenRecordCallback {
        fun onStartRecording()
        fun onStopRecording()
    }
}
