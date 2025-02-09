/*
 * Copyright (C) 2025 The Nameless-CLO Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.pixelworks

import kotlin.math.min

import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.common.Utils.logE

abstract class BaseIrisWrapper<T> {

    abstract val tag: String

    abstract fun isSupported(): Boolean
    abstract fun irisConfigureGetInternal(type: Int, values: IntArray): IrisGetResult
    abstract fun irisConfigureSetInternal(type: Int, values: IntArray): Int
    abstract fun getChipFeatureInternal(): Int
    abstract fun registerCallbackInternal(callback: T)

    private fun irisConfigureGet(type: Int, values: IntArray?): Int {
        if (values == null) {
            logE(tag, "irisConfigureGet failed, invalid parameter")
            return -1
        }
        logD(tag, "irisConfigureGet, type: $type, values: ${values.contentToString()}, count: ${values.size}")
        val ret = irisConfigureGetInternal(type, values)
        if (ret.ret < 0) {
            logE(tag, "irisConfigureGet failed, result: ${ret.ret}")
            return -1
        }
        if (ret.values.isEmpty()) {
            logE(tag, "irisConfigureGet failed, ret.values is empty")
            return -1
        }
        val len = min(values.size, ret.values.size)
        for (i in 0 until len) {
            values[i] = ret.values[i]
        }
        return ret.ret
    }

    private fun irisConfigureSet(type: Int, values: IntArray?): Int {
        if (values == null) {
            logE(tag, "irisConfigureSet failed, invalid parameter")
            return -1
        }
        logD(tag, "irisConfigureSet, type: $type, values: ${values.contentToString()}")
        return irisConfigureSetInternal(type, values)
    }

    fun getIrisCommand(type: Int): Int {
        val values = IntArray(1)
        val result = irisConfigureGet(type, values)
        if (result < 0) {
            logE(tag, "getIrisCommand failed, result: $result")
            return -1
        }
        return values[0]
    }

    fun setIrisCommand(cmd: String?): Int {
        if (cmd.isNullOrBlank()) {
            logE(tag, "setIrisCommand failed, cmd string is empty")
            return -1
        }
        val cmdArr = cmd.split("-")
        if (cmdArr.isEmpty() || cmdArr[0].isBlank()) {
            logE(tag, "setIrisCommand failed, cmd string is empty")
            return -1
        }
        val type = cmdArr[0].toInt()
        val values = cmdArr.filterIndexed { index, _ -> index > 0 }.map { it.toInt() }
        val result = irisConfigureSet(type, values.toIntArray())
        if (result < 0) {
            logE(tag, "setIrisCommand failed, result: $result")
        }
        return result
    }

    private fun getChipFeature(): Int {
        return getChipFeatureInternal()
    }

    fun registerCallback(callback: T) {
        if (getChipFeature() <= 0) {
            logE(tag, "getChipFeature failed. Skip register callback")
            return
        }
        registerCallbackInternal(callback)
    }

    class IrisGetResult(
        var ret: Int = -1,
        var values: IntArray = intArrayOf(0)
    )
}
