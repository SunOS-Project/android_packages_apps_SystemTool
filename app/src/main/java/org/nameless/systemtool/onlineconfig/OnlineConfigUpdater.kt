/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.onlineconfig

import android.util.Xml

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

import org.nameless.content.IOnlineConfigurable
import org.nameless.systemtool.common.Utils.PACKAGE_NAME
import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.common.Utils.logE
import org.nameless.systemtool.onlineconfig.util.Constants.DEBUG_MODE_UPDATE_INTERVAL
import org.nameless.systemtool.onlineconfig.util.Constants.UPDATE_INTERVAL
import org.nameless.systemtool.onlineconfig.util.Shared.debugMode
import org.nameless.systemtool.onlineconfig.util.Shared.onlineConfigManager
import org.nameless.systemtool.onlineconfig.util.Shared.updateScheduler

import org.xmlpull.v1.XmlPullParser

object OnlineConfigUpdater {

    private const val TAG = "SystemTool::OnlineConfigUpdater"

    fun update() {
        val clients = onlineConfigManager.registeredClients
        if (clients == null) {
            logD(TAG, "No online config clients!")
            setNextScheduler()
            return
        }
        logD(TAG,"Start to update, clients.size=${clients.size}")
        clients.filter { downloadOnlineConfig(it) }.forEach {
            logD(TAG,"Start verification for ${it.localConfigPath}")
            getConfigInfo(it.localConfigPath + ".tmp").let { info ->
                val systemTimestamp = getConfigInfo(it.systemConfigPath).second
                val localTimestamp = getConfigInfo(it.localConfigPath).second
                if (info.first > it.version) {
                    // Online config requires higher framework config version, skip update
                    logD(TAG, "online config version ${info.first} > framework config " +
                            "version ${it.version}, skip update")
                    cleanTmpLocalConfig(it)
                } else if (info.second <= systemTimestamp) {
                    // Online config isn't newer than system config, skip update
                    logD(TAG, "online config timestamp ${info.second} <= framework config " +
                            "timestamp $systemTimestamp, skip update")
                    cleanTmpLocalConfig(it)
                } else if (info.second <= localTimestamp) {
                    // Online config isn't newer than existing local config
                    // Online update may has been processed before
                    // Or it's possible that user modified config
                    logD(TAG, "online config timestamp ${info.first} <= existing local config " +
                            "timestamp $localTimestamp, skip update")
                    cleanTmpLocalConfig(it)
                } else {
                    logD(TAG, "Update verified for ${it.localConfigPath}")
                    renameTmpLocalConfig(it)
                    it.onConfigUpdated()
                }
            }
        }
        setNextScheduler()
    }

    private fun downloadOnlineConfig(configurable: IOnlineConfigurable): Boolean {
        var conn: HttpURLConnection? = null
        var reader: BufferedReader? = null
        var bw: BufferedWriter? = null
        try {
            conn = URL(configurable.onlineConfigUri).openConnection().apply {
                setRequestProperty("User-Agent", PACKAGE_NAME)
                connectTimeout = 5000
                readTimeout = 5000
                useCaches = false
                connect()
            } as HttpURLConnection
            reader = BufferedReader(InputStreamReader(conn.inputStream, "utf-8"))
            bw = BufferedWriter(OutputStreamWriter(FileOutputStream(File(
                    configurable.localConfigPath + ".tmp"))))

            var line = reader.readLine()
            while (line != null) {
                bw.write(line)
                bw.newLine()
                line = reader.readLine()
            }
        } catch (e: Exception) {
            logE(TAG, "Exception on downloading config ${configurable.onlineConfigUri}", e)
            return false
        } finally {
            bw?.close()
            reader?.close()
            conn?.disconnect()
        }
        return true
    }

    private fun cleanTmpLocalConfig(configurable: IOnlineConfigurable) {
        File(configurable.localConfigPath + ".tmp").let {
            if (it.exists()) {
                it.delete()
            }
        }
    }

    private fun renameTmpLocalConfig(configurable: IOnlineConfigurable) {
        File(configurable.localConfigPath).let {
            if (it.exists()) {
                it.delete()
            }
            File(configurable.localConfigPath + ".tmp").renameTo(it)
        }
    }

    private fun getConfigInfo(path: String): Pair<Int, Long> {
        var version = Int.MAX_VALUE
        var timestamp = -1L
        try {
            val fr = FileReader(File(path))
            val parser = Xml.newPullParser().apply {
                setInput(fr)
            }
            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG && parser.name == "info") {
                    version = parser.getAttributeValue(null, "version").toInt()
                    timestamp = parser.getAttributeValue(null, "timestamp").toLong()
                    break
                }
                event = parser.next()
            }
            fr.close()
        } catch (e: Exception) {
            logE(TAG, "Failed to get config info for $path", e)
        }
        return Pair(version, timestamp)
    }

    private fun setNextScheduler() {
        updateScheduler.scheduler = if (debugMode) {
            DEBUG_MODE_UPDATE_INTERVAL
        } else {
            UPDATE_INTERVAL
        }
    }
}
