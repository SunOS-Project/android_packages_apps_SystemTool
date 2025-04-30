/*
 * Copyright (C) 2025 The Nameless-CLO Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.iris.pixelworks

import java.util.ArrayList

import org.sun.systemtool.common.Utils.logE
import org.sun.systemtool.iris.util.Constants.IRIS_COOKIE

import vendor.pixelworks.hardware.display.V1_0.IIris
import vendor.pixelworks.hardware.display.V1_0.IIrisCallback
import vendor.pixelworks.hardware.feature.V1_0.IIrisFeature

@Suppress("DEPRECATION")
class IrisHIDLWrapper : BaseIrisWrapper<IIrisCallback.Stub>() {

    override val tag = "SystemTool::IrisHIDLWrapper"

    private var iIris: IIris? = null
        get() {
            if (field == null) {
                try {
                    field = IIris.getService()
                } catch (_: Exception) {
                }
            }
            if (field == null) {
                logE(tag, "Failed to get iris HIDL")
            }
            return field
        }

    private var iIrisFeature: IIrisFeature? = null
        get() {
            if (field == null) {
                try {
                    field = IIrisFeature.getService()
                } catch (_: Exception) {
                }
            }
            if (field == null) {
                logE(tag, "Failed to get iris feature HIDL")
            }
            return field
        }

    override fun isSupported(): Boolean {
        return iIris != null && iIrisFeature != null
    }

    override fun irisConfigureGetInternal(type: Int, values: IntArray): IrisGetResult {
        val arrayList = values.toCollection(ArrayList())
        val ret = IrisGetResult()
        try {
            iIris?.irisConfigureGet(type, arrayList) { result, valuesArr ->
                if (valuesArr != null) {
                    ret.ret = result
                    ret.values = valuesArr.toIntArray()
                }
            } ?: return IrisGetResult()
            return ret
        } catch (e: Exception) {
            logE(tag, "irisConfigureGet failed")
            return IrisGetResult()
        }
    }

    override fun irisConfigureSetInternal(type: Int, values: IntArray): Int {
        val arrayList = values.toCollection(ArrayList())
        return try {
            iIris?.irisConfigureSet(type, arrayList) ?: -1
        } catch (e: Exception) {
            logE(tag, "irisConfigureSet failed")
            -1
        }
    }

    override fun getChipFeatureInternal(): Int {
        val callback = object : IIrisFeature.GetFeatureCallback {
            var result = -1
            var feature = -1
            override fun onValues(result: Int, feature: Int) {
                this.result = result
                this.feature = feature
            }
        }
        try {
            iIrisFeature?.getFeature(callback) ?: return -1
            return if (callback.result >= 0) {
                callback.feature
            } else {
                -1
            }
        } catch (e: Exception) {
            logE(tag, "getChipFeature failed")
            return -1
        }
    }

    override fun registerCallbackInternal(callback: IIrisCallback.Stub) {
        try {
            iIris?.registerCallback2(IRIS_COOKIE, callback)
        } catch (e: Exception) {
            logE(tag, "registerCallback failed")
        }
    }
}
