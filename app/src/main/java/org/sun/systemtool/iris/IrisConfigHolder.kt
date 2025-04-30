/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.iris

import android.os.SystemProperties
import android.util.Xml

import java.io.File
import java.io.FileNotFoundException
import java.io.FileReader

import org.sun.content.IOnlineConfigurable
import org.sun.systemtool.common.Utils.logD
import org.sun.systemtool.common.Utils.logE
import org.sun.systemtool.iris.util.Constants.LOCAL_IRIS_CONFIG_PATH
import org.sun.systemtool.iris.util.Constants.SYSTEM_IRIS_CONFIG_PATH

import org.xmlpull.v1.XmlPullParser

object IrisConfigHolder : IOnlineConfigurable.Stub() {

    private const val TAG = "SystemTool::IrisConfigHolder"

    private const val VERSION = 1

    private val overrideFrameRateAppSet = mutableSetOf<String>()

    private val memcCommandMap = mutableMapOf<String, String>()
    private val sdr2HdrCommandMap = mutableMapOf<String, String>()

    private var initialized = false

    override fun getVersion() = VERSION

    override fun getOnlineConfigUri(): String =
            SystemProperties.get("persist.sys.sun.uri.iris")

    override fun getSystemConfigPath() = SYSTEM_IRIS_CONFIG_PATH

    override fun getLocalConfigPath() = LOCAL_IRIS_CONFIG_PATH

    override fun onConfigUpdated() {
        initialized = false
        // We can use local config here safely because this is called after verification.
        initConfig(LOCAL_IRIS_CONFIG_PATH)
        initialized = true
    }

    private fun getConfigInfo(path: String): Pair<Int, Long> {
        try {
            val fr = FileReader(File(path))
            val parser = Xml.newPullParser().apply {
                setInput(fr)
            }
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    if ("info" == parser.name) {
                        val versionStr = parser.getAttributeValue(null, "version")
                        val timestampStr = parser.getAttributeValue(null, "timestamp")
                        return Pair(versionStr.toInt(), timestampStr.toLong())
                    }
                }
                event = parser.next()
            }
            fr.close()
        } catch (e: Exception) {
            if (e !is FileNotFoundException) {
                logE(TAG, "exception on get config info", e)
            }
        }
        return Pair(Int.MAX_VALUE, -1L)
    }

    private fun compareConfigTimestamp(): String {
        val systemConfigInfo = getConfigInfo(SYSTEM_IRIS_CONFIG_PATH)
        val localConfigInfo = getConfigInfo(LOCAL_IRIS_CONFIG_PATH)

        // Online config requires higher framework version. Fallback to system config.
        if (localConfigInfo.first > VERSION) {
            return SYSTEM_IRIS_CONFIG_PATH
        }

        return if (localConfigInfo.second > systemConfigInfo.second) {
            LOCAL_IRIS_CONFIG_PATH
        } else {
            SYSTEM_IRIS_CONFIG_PATH
        }
    }

    fun initConfig(path: String = compareConfigTimestamp()) {
        overrideFrameRateAppSet.clear()
        memcCommandMap.clear()
        sdr2HdrCommandMap.clear()

        try {
            val fr = FileReader(File(path))
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
            initialized = true
        } catch (e: Exception) {
            logE(TAG, "Failed to init config", e)
        }
    }

    fun needOverrideRefreshRate(packageName: String?): Boolean {
        if (!initialized) {
            return false
        }
        return overrideFrameRateAppSet.contains(packageName)
    }

    fun getMemcCommand(activityName: String?): String {
        if (!initialized) {
            return String()
        }
        return memcCommandMap.getOrDefault(activityName, String())
    }

    fun getSDR2HDRCommand(packageName: String?): String {
        if (!initialized) {
            return String()
        }
        return sdr2HdrCommandMap.getOrDefault(packageName, String())
    }
}
