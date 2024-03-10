/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris

import android.util.Xml

import java.io.File
import java.io.FileReader

import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.common.Utils.logE
import org.nameless.systemtool.iris.util.Constants.IRIS_CONFIG_PATH

import org.xmlpull.v1.XmlPullParser

object IrisConfigHolder {

    private const val TAG = "SystemTool::IrisConfigHolder"

    private val overrideFrameRateAppSet = mutableSetOf<String>()

    private val memcCommandMap = mutableMapOf<String, String>()
    private val sdr2HdrCommandMap = mutableMapOf<String, String>()

    fun initConfig(): Boolean {
        try {
            val fr = FileReader(File(IRIS_CONFIG_PATH))
            val parser = Xml.newPullParser().apply {
                setInput(fr)
            }
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "mConfigPackage" -> {
                            val rate = parser.getAttributeValue(null, "rate")
                            val type = parser.getAttributeValue(null, "type")
                            parser.next()
                            val packageName = parser.text
                            if ("60" == rate) {
                                overrideFrameRateAppSet.add(packageName)
                            }
                            sdr2HdrCommandMap[packageName] = type
                            logD(TAG, "Added package: $packageName, rate: $rate, type: $type")
                        }
                        "mConfigActivity" -> {
                            val type = parser.getAttributeValue(null, "type")
                            parser.next()
                            val componentName = parser.text
                            val activity = componentName.substring(componentName.indexOf('/') + 1)
                            if (!memcCommandMap.containsKey(activity)) {
                                memcCommandMap[activity] = type
                                logD(TAG, "Added activity: $activity, type: $type")
                            }
                        }
                    }
                }
                event = parser.next()
            }
            fr.close()
        } catch (e: Exception) {
            logE(TAG, "Failed to init config", e)
            return false
        }
        return true
    }

    fun needOverrideRefreshRate(packageName: String?): Boolean {
        return overrideFrameRateAppSet.contains(packageName)
    }

    fun getMemcCommand(activityName: String?): String {
        return memcCommandMap.getOrDefault(activityName, String())
    }

    fun getSDR2HDRCommand(packageName: String?): String {
        return sdr2HdrCommandMap.getOrDefault(packageName, String())
    }
}
