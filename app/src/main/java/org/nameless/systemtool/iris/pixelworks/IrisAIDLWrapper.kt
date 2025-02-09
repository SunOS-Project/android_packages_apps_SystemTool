/*
 * Copyright (C) 2025 The Nameless-CLO Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.pixelworks

import android.os.Binder
import android.os.ServiceManager

import org.nameless.systemtool.common.Utils.logE
import org.nameless.systemtool.iris.util.Constants.IRIS_COOKIE

import vendor.pixelworks.hardware.display.IIris
import vendor.pixelworks.hardware.display.IIrisCallback

@Suppress("DEPRECATION")
class IrisAIDLWrapper : BaseIrisWrapper<IIrisCallback.Default>() {

    override val tag = "SystemTool::IrisAIDLWrapper"

    private var iIris: IIris? = null
        get() {
            if (field == null) {
                try {
                    ServiceManager.getService(IRIS_SERVICE_NAME)?.let { service ->
                        field = IIris.Stub.asInterface(Binder.allowBlocking(service))
                    }
                } catch (_: Exception) {
                }
            }
            if (field == null) {
                logE(tag, "Failed to get iris AIDL")
            }
            return field
        }

    override fun isSupported(): Boolean {
        return iIris != null
    }

    override fun irisConfigureGetInternal(type: Int, values: IntArray): IrisGetResult {
        try {
            iIris?.irisConfigureGet(type, values)?.let {
                return IrisGetResult(0, it)
            }
        } catch (e: Exception) {
            logE(tag, "irisConfigureGet failed")
        }
        return IrisGetResult()
    }

    override fun irisConfigureSetInternal(type: Int, values: IntArray): Int {
        try {
            iIris?.irisConfigureSet(type, values)?.let {
                return it
            }
        } catch (e: Exception) {
            logE(tag, "irisConfigureSet failed")
        }
        return -1
    }

    override fun getChipFeatureInternal(): Int {
        return 1
    }

    override fun registerCallbackInternal(callback: IIrisCallback.Default) {
        try {
            iIris?.registerCallback(IRIS_COOKIE, callback)
        } catch (e: Exception) {
            logE(tag, "registerCallback failed")
        }
    }

    companion object {
        private const val IRIS_SERVICE_NAME = "vendor.pixelworks.hardware.display.IIris/default"
    }
}
