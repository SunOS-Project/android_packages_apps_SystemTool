/*
 * Copyright (C) 2025 The Nameless-CLO Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.pixelworks

import org.nameless.systemtool.iris.IrisService

import kotlin.collections.ArrayList

object IrisHelper {

    private val aidlWrapper = IrisAIDLWrapper()
    private val hidlWrapper = IrisHIDLWrapper()

    fun getIrisCommand(type: Int): Int {
        if (aidlWrapper.isSupported()) {
            return aidlWrapper.getIrisCommand(type)
        }
        if (hidlWrapper.isSupported()) {
            return hidlWrapper.getIrisCommand(type)
        }
        return -1
    }

    fun setIrisCommand(cmd: String?): Int {
        if (aidlWrapper.isSupported()) {
            return aidlWrapper.setIrisCommand(cmd)
        }
        if (hidlWrapper.isSupported()) {
            return hidlWrapper.setIrisCommand(cmd)
        }
        return -1
    }

    fun registerCallback(callback: IrisService.IrisCallback) {
        if (aidlWrapper.isSupported()) {
            aidlWrapper.registerCallback(object : vendor.pixelworks.hardware.display.IIrisCallback.Default() {
                override fun onFeatureChanged(type: Int, values: IntArray?) {
                    if (values == null || values.isEmpty()) {
                        return
                    }
                    callback.onFeatureChanged(type, values.toCollection(ArrayList()))
                }
            })
            return
        }
        if (hidlWrapper.isSupported()) {
            hidlWrapper.registerCallback(object : vendor.pixelworks.hardware.display.V1_0.IIrisCallback.Stub() {
                override fun onFeatureChanged(type: Int, values: ArrayList<Int>?) {
                    if (values.isNullOrEmpty()) {
                        return
                    }
                    callback.onFeatureChanged(type, values)
                }
            })
            return
        }
    }
}
