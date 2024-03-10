/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.pixelworks

import java.util.ArrayList
import java.util.Arrays

import kotlin.math.min

import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.common.Utils.logE
import org.nameless.systemtool.iris.util.Constants.IRIS_COOKIE

import vendor.pixelworks.hardware.display.V1_0.IIris
import vendor.pixelworks.hardware.display.V1_0.IIrisCallback
import vendor.pixelworks.hardware.feature.V1_0.IIrisFeature

@Suppress("DEPRECATION")
object IrisHIDLWrapper {

    private const val TAG = "SystemTool::IrisHIDLWrapper"

    private var iIris: IIris? = null
        get() {
            if (field == null) {
                try {
                    field = IIris.getService()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (field == null) {
                logE(TAG, "Failed to get IIris service")
            }
            return field
        }

    private var iIrisFeature: IIrisFeature? = null
        get() {
            if (field == null) {
                try {
                    field = IIrisFeature.getService()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            if (field == null) {
                logE(TAG, "Failed to get IIrisFeature service")
            }
            return field
        }

    private fun irisConfigureGet(type: Int, values: IntArray?, count: Int): Int {
        if (values == null) {
            logE(TAG, "irisConfigureGet failed, invalid parameter")
            return -1
        }
        logD(TAG, "irisConfigureGet, type: $type, values: ${Arrays.toString(values)}, count: $count")
        val arrayList = values.toCollection(ArrayList())
        val callback = IrisConfigureGetCallback()
        try {
            iIris?.irisConfigureGet(type, arrayList, callback) ?: return -1
            val len = min(count, callback.values?.size ?: count)
            for (i in 0 until len) {
                values[i] = callback.values?.get(i) ?: values[i]
            }
            return callback.result
        } catch (e: Exception) {
            logE(TAG, "irisConfigureGet failed")
            return -1
        }
    }

    private fun irisConfigureSet(type: Int, values: IntArray?): Int {
        if (values == null) {
            logE(TAG, "irisConfigureSet failed, invalid parameter")
            return -1
        }
        logD(TAG, "irisConfigureSet, type: $type, values: ${Arrays.toString(values)}")
        val arrayList = values.toCollection(ArrayList())
        return try {
            iIris?.irisConfigureSet(type, arrayList) ?: -1
        } catch (e: Exception) {
            logE(TAG, "irisConfigureSet failed")
            -1
        }
    }

    fun getIrisCommand(type: Int): Int {
        val values = IntArray(1)
        val result = irisConfigureGet(type, values, values.size)
        if (result < 0) {
            logE(TAG, "getIrisCommand failed, result: $result")
            return result
        }
        return values[0]
    }

    fun setIrisCommand(cmd: String?): Int {
        if (cmd.isNullOrBlank()) {
            logE(TAG, "setIrisCommand failed, cmd string is empty")
            return -1
        }
        val cmdArr = cmd.split("-")
        if (cmdArr.isEmpty() || cmdArr[0].isBlank()) {
            logE(TAG, "setIrisCommand failed, cmd string is empty")
            return -1
        }
        val type = cmdArr[0].toInt()
        val values = cmdArr.filterIndexed { index, _ -> index > 0 }.map { it.toInt() }
        val result = irisConfigureSet(type, values.toIntArray())
        if (result < 0) {
            logE(TAG, "setIrisCommand failed, result: $result")
        }
        return result
    }

    private fun getChipFeature(): Int {
        val callback = GetFeatureCallback()
        try {
            iIrisFeature?.getFeature(callback) ?: return -1
            return if (callback.result >= 0) {
                callback.feature
            } else {
                -1
            }
        } catch (e: Exception) {
            logE(TAG, "getChipFeature failed")
            return -1
        }
    }

    fun registerCallback(callback: IIrisCallback.Stub) {
        if (getChipFeature() <= 0) {
            logE(TAG, "getChipFeature failed. Skip register callback")
            return
        }
        try {
            iIris?.registerCallback2(IRIS_COOKIE, callback)
        } catch (e: Exception) {
            logE(TAG, "registerCallback failed")
        }
    }
}
