/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.iris

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast

import com.android.internal.util.sun.ScreenStateListener

import java.util.ArrayList

import org.sun.systemtool.R
import org.sun.systemtool.common.Utils.logD
import org.sun.systemtool.common.Utils.logE
import org.sun.systemtool.iris.observer.CommandReceiver
import org.sun.systemtool.iris.observer.DisplayResolutionChangeListener
import org.sun.systemtool.iris.observer.SettingsObserver
import org.sun.systemtool.iris.observer.SystemStateReceiver
import org.sun.systemtool.iris.observer.TaskStackChangeListener
import org.sun.systemtool.iris.pixelworks.IrisHelper
import org.sun.systemtool.iris.util.FeatureHelper.VIDEO_OSIE_SUPPORTED
import org.sun.systemtool.iris.util.RefreshRateHelper
import org.sun.systemtool.iris.util.Shared.layoutInflater
import org.sun.systemtool.iris.util.Shared.onlineConfigManager
import org.sun.systemtool.iris.util.Shared.powerSaveMode
import org.sun.systemtool.iris.util.Shared.service
import org.sun.view.DisplayResolutionManager.QHD_WIDTH

class IrisService : Service() {

    val resolutionListener by lazy {
        object : DisplayResolutionChangeListener(handler) {
            override fun onDisplayWidthChanged() {
                handleDisplayWidthChange()
            }
        }
    }
    private val settingsObserver by lazy {
        object : SettingsObserver(handler) {
            override fun onMemcEnabledChanged() {
                handleMemcStatusChange()
            }

            override fun onSDR2HDREnabledChanged() {
                handleSDR2HDRStatusChange()
            }
        }
    }
    private val taskStackChangeListener by lazy {
        object : TaskStackChangeListener() {
            override fun onFullscreenTaskChanged(packageName: String, activityName: String, taskId: Int) {
                memcCommand = IrisConfigHolder.getMemcCommand(activityName)
                sdr2hdrCommand = IrisConfigHolder.getSDR2HDRCommand(packageName)

                inMemcList = memcCommand.isNotBlank()
                inSDR2HDRList = sdr2hdrCommand.isNotBlank()

                logD(TAG, "memcCommand: ${if (inMemcList) memcCommand else "null"}, " +
                        "sdr2hdrCommand: ${if (inSDR2HDRList) sdr2hdrCommand else "null"}")

                if (VIDEO_OSIE_SUPPORTED) {
                    return
                }

                val memcAvailable = settingsObserver.memcEnabled && inMemcList && !powerSaveMode
                val sdr2hdrAvailable = settingsObserver.sdr2hdrEnabled && inSDR2HDRList && !powerSaveMode

                if (memcAvailable || sdr2hdrAvailable) {
                    if (needOverrideRefreshRate(topPackageName)) {
                        handler.sendEmptyMessageDelayed(MSG_SET_REFRESH_RATE, 400L)
                    }
                } else {
                    removeAllMessages()
                    switchBypassMode()
                    handler.sendEmptyMessage(MSG_RESTORE_REFRESH_RATE)
                    hasShownToast = false
                }
                if (sdr2hdrAvailable) {
                    handler.sendEmptyMessageDelayed(MSG_SET_SDR2HDR_PARAMETERS, 600L)
                }
                if (memcAvailable) {
                    handler.sendEmptyMessageDelayed(MSG_SET_MEMC_PARAMETERS, 600L)
                }
            }
        }
    }
    private val commandReceiver by lazy {
        object : CommandReceiver(handler) {
            override fun onGetCommand(type: Int): Int {
                return IrisHelper.getIrisCommand(type)
            }

            override fun onSetCommand(command: String): Int {
                return IrisHelper.setIrisCommand(command)
            }
        }
    }
    private val screenStateListener by lazy {
        object : ScreenStateListener(this, handler) {
            override fun onScreenOff() {
                removeAllMessages()
                switchBypassMode()
                handler.sendEmptyMessage(MSG_RESTORE_REFRESH_RATE)
                hasShownToast = false
            }

            override fun onScreenOn() {}

            override fun onScreenUnlocked() {
                checkTopActivity()
            }
        }
    }
    private val systemStateReceiver by lazy {
        object : SystemStateReceiver(handler) {
            override fun onCloseSystemDialog(reason: String) {
                if (SYSTEM_DIALOG_REASON_HOME_KEY == reason ||
                        SYSTEM_DIALOG_RECENT_APPS_KEY == reason) {
                    removeAllMessages()
                    removeSdr2hdrParameters()
                }
            }

            override fun onPowerSaveModeChanged() {
                checkTopActivity()
            }
        }
    }

    private val handlerThread by lazy {
        HandlerThread(TAG).apply { start() }
    }
    private val handler by lazy { IrisHandler(handlerThread.looper) }

    private var memcCommand = String()
    private var sdr2hdrCommand = String()

    private var inMemcList = false
    private var inSDR2HDRList = false

    private var hasShownToast = false

    private var inBypassMode = false
    private var inMemcMode = false
    private var inSDR2HDRMode = false

    private var orientation = Configuration.ORIENTATION_PORTRAIT

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        service = this

        IrisConfigHolder.initConfig()
        onlineConfigManager.registerOnlineConfigurable(IrisConfigHolder)

        handler.sendMessage(handler.obtainMessage(MSG_REGISTER_CALLBACK))

        inBypassMode = IrisHelper.getIrisCommand(56) == 1
        inMemcMode = IrisHelper.getIrisCommand(258) != 0
        inSDR2HDRMode = !VIDEO_OSIE_SUPPORTED && IrisHelper.getIrisCommand(267) != 0

        resolutionListener.registered = true
        settingsObserver.registered = true
        taskStackChangeListener.registered = true
        commandReceiver.registered = true
        screenStateListener.setListening(true)
        systemStateReceiver.registered = true
    }

    override fun onDestroy() {
        commandReceiver.registered = false
        screenStateListener.setListening(false)
        systemStateReceiver.registered = false
        taskStackChangeListener.registered = false
        settingsObserver.registered = false
        resolutionListener.registered = false

        restoreRefreshRate()

        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        if (!VIDEO_OSIE_SUPPORTED) {
            return
        }
        newConfig.orientation.takeIf { orientation != it }?.let {
            orientation = it
            val enable = settingsObserver.memcEnabled
                    && inMemcList
                    && !powerSaveMode
                    && orientation == Configuration.ORIENTATION_LANDSCAPE
            if (inMemcMode == enable) {
                return
            }
            removeAllMessages()
            if (enable) {
                if (needOverrideRefreshRate(taskStackChangeListener.topPackageName)) {
                    handler.sendEmptyMessage(MSG_SET_REFRESH_RATE)
                }
                handler.sendEmptyMessageDelayed(MSG_SET_MEMC_PARAMETERS, 600L)
            } else {
                switchBypassMode()
                handler.sendEmptyMessage(MSG_RESTORE_REFRESH_RATE)
                hasShownToast = false
            }
        }
    }

    private fun registerCallback() {
        IrisHelper.registerCallback(object : IrisCallback {
            override fun onFeatureChanged(type: Int, values: ArrayList<Int>) {
                logD(TAG, "onFeatureChanged, type: $type")
                handler.sendMessage(handler.obtainMessage(MSG_FEATURE_CHANGE).apply {
                    data = Bundle().apply {
                        putInt("type", type)
                        putIntegerArrayList("values", values)
                    }
                })
            }
        })
    }

    private fun checkTopActivity() {
        taskStackChangeListener.forceCheck()
    }

    private fun handleDisplayWidthChange() {
        checkTopActivity()
    }

    private fun handleMemcStatusChange() {
        hasShownToast = false
        if (inMemcList) {
            removeAllMessages()
            if (settingsObserver.memcEnabled) {
                if (needOverrideRefreshRate(taskStackChangeListener.topPackageName)) {
                    handler.sendEmptyMessage(MSG_SET_REFRESH_RATE)
                }
                handler.sendEmptyMessage(MSG_SET_MEMC_PARAMETERS)
            } else {
                switchBypassMode()
                if (inSDR2HDRList && settingsObserver.sdr2hdrEnabled) {
                    handler.sendEmptyMessage(MSG_SET_SDR2HDR_PARAMETERS)
                } else {
                    handler.sendEmptyMessage(MSG_RESTORE_REFRESH_RATE)
                }
            }
        }
    }

    private fun handleSDR2HDRStatusChange() {
        if (inSDR2HDRList) {
            removeAllMessages()
            if (settingsObserver.sdr2hdrEnabled) {
                if (needOverrideRefreshRate(taskStackChangeListener.topPackageName)) {
                    handler.sendEmptyMessage(MSG_SET_REFRESH_RATE)
                }
                handler.sendEmptyMessage(MSG_SET_SDR2HDR_PARAMETERS)
            } else {
                switchBypassMode()
                if (inMemcList && settingsObserver.memcEnabled) {
                    handler.sendEmptyMessage(MSG_SET_MEMC_PARAMETERS)
                } else {
                    handler.sendEmptyMessage(MSG_RESTORE_REFRESH_RATE)
                }
            }
        }
    }

    private fun removeAllMessages() {
        handler.removeMessages(MSG_SET_REFRESH_RATE)
        handler.removeMessages(MSG_SET_MEMC_PARAMETERS)
        handler.removeMessages(MSG_SET_SDR2HDR_PARAMETERS)
        handler.removeMessages(MSG_SWITCH_BYPASS)
    }

    private fun removeSdr2hdrParameters() {
        if (inSDR2HDRMode) {
            if (IrisHelper.setIrisCommand("267-3-0") >= 0) {
                inSDR2HDRMode = false
            }
        }
    }

    private fun setTempRefreshRate() {
        RefreshRateHelper.memcRefreshRate = 60
    }

    private fun restoreRefreshRate() {
        RefreshRateHelper.memcRefreshRate = -1
    }

    private fun setMemcParameters() {
        if (inMemcList) {
            switchPtMode()
            if (IrisHelper.setIrisCommand(memcCommand) >= 0) {
                inMemcMode = true
            }
        }
    }

    private fun setSDR2HDRParameters() {
        if (!inSDR2HDRList) {
            return;
        }
        if (IrisHelper.setIrisCommand(sdr2hdrCommand) >= 0) {
            inSDR2HDRMode = true
        }
    }

    private fun switchBypassMode() {
        if (inBypassMode) {
            return
        }
        if (inSDR2HDRMode) {
            if (IrisHelper.setIrisCommand("267-3-0") >= 0) {
                inSDR2HDRMode = false
            }
        }
        if (inMemcMode) {
            if (IrisHelper.setIrisCommand("258-0") >= 0) {
                inMemcMode = false
            }
        }
        if (IrisHelper.setIrisCommand("56-1") >= 0) {
            inBypassMode = true
        }
    }

    private fun switchPtMode() {
        if (inBypassMode) {
            if (IrisHelper.setIrisCommand("56-0") >= 0) {
                inBypassMode = false
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun showMemcToast() {
        val v = layoutInflater.inflate(R.layout.transient_memc_notification, null)
        if (v == null) {
            logE(TAG, "Failed to show memc toast, v is null")
            return
        }
        val tv = v.findViewById<TextView>(R.id.memc_message)!!
        tv.text = service.getText(com.android.internal.R.string.motion_graphics_smoothing_starting)
        Toast(service, null).apply {
            view = v
            duration = Toast.LENGTH_LONG
            setGravity(Gravity.TOP, 0, (service.resources.displayMetrics.density * 24.0f).toInt())
        }.show()
    }

    private fun handleFeatureChanged() {
        if (!hasShownToast) {
            showMemcToast()
            hasShownToast = true
        }
    }

    private fun needOverrideRefreshRate(packageName: String): Boolean {
        return (resolutionListener.displayWidth == QHD_WIDTH ||
                IrisConfigHolder.needOverrideRefreshRate(packageName))
                && RefreshRateHelper.memcRefreshRate <= 0
    }

    private inner class IrisHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REGISTER_CALLBACK -> {
                    registerCallback()
                }
                MSG_SET_REFRESH_RATE -> {
                    setTempRefreshRate()
                }
                MSG_RESTORE_REFRESH_RATE -> {
                    restoreRefreshRate()
                }
                MSG_SET_MEMC_PARAMETERS -> {
                    setMemcParameters()
                }
                MSG_SET_SDR2HDR_PARAMETERS -> {
                    setSDR2HDRParameters()
                }
                MSG_SWITCH_BYPASS -> {
                    switchBypassMode()
                }
                MSG_FEATURE_CHANGE -> {
                    handleFeatureChanged()
                }
            }
        }
    }

    interface IrisCallback {
        fun onFeatureChanged(type: Int, values: ArrayList<Int>)
    }

    companion object {
        private const val TAG = "SystemTool::IrisService"

        private const val MSG_REGISTER_CALLBACK = 0
        private const val MSG_SET_REFRESH_RATE = 1
        private const val MSG_RESTORE_REFRESH_RATE = 2
        private const val MSG_SET_MEMC_PARAMETERS = 3
        private const val MSG_SET_SDR2HDR_PARAMETERS = 4
        private const val MSG_SWITCH_BYPASS = 5
        private const val MSG_FEATURE_CHANGE = 6

        private const val SYSTEM_DIALOG_REASON_HOME_KEY = "homekey"
        private const val SYSTEM_DIALOG_RECENT_APPS_KEY = "recentapps"
    }
}
