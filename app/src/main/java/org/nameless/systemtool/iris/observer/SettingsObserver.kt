/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.observer

import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.UserHandle
import android.provider.Settings

import com.android.internal.util.nameless.UserSwitchReceiver

import org.nameless.provider.SettingsExt.System.IRIS_MEMC_ENABLED
import org.nameless.provider.SettingsExt.System.IRIS_VIDEO_COLOR_BOOST
import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.iris.util.FeatureHelper.MEMC_FHD_SUPPORTED
import org.nameless.systemtool.iris.util.FeatureHelper.MEMC_QHD_SUPPORTED
import org.nameless.systemtool.iris.util.FeatureHelper.SDR2HDR_SUPPORTED
import org.nameless.systemtool.iris.util.FeatureHelper.VIDEO_OSIE_SUPPORTED
import org.nameless.systemtool.iris.util.Shared.service
import org.nameless.view.DisplayResolutionManager.FHD_WIDTH
import org.nameless.view.DisplayResolutionManager.QHD_WIDTH

abstract class SettingsObserver(
    handler: Handler,
) : ContentObserver(handler) {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                service.contentResolver.apply {
                    if (MEMC_FHD_SUPPORTED || MEMC_QHD_SUPPORTED) {
                        registerContentObserver(
                            Settings.System.getUriFor(IRIS_MEMC_ENABLED),
                            false, this@SettingsObserver, UserHandle.USER_ALL)
                    }
                    if (SDR2HDR_SUPPORTED && !VIDEO_OSIE_SUPPORTED) {
                        registerContentObserver(
                            Settings.System.getUriFor(IRIS_VIDEO_COLOR_BOOST),
                            false, this@SettingsObserver, UserHandle.USER_ALL)
                    }
                }
                userSwitchReceiver.setListening(true)
                updateAll()
            } else {
                userSwitchReceiver.setListening(false)
                service.contentResolver.unregisterContentObserver(this)
            }
        }

    var memcEnabled = false
        get() {
            return field && displayWidthSupportMemc()
        }
        set(value) {
            field = value
            logD(TAG, "memcEnabled changed to $memcEnabled")
            onMemcEnabledChanged()
        }

    var sdr2hdrEnabled = false
        get() {
            return field && SDR2HDR_SUPPORTED
        }
        set(value) {
            field = value
            logD(TAG, "sdr2hdrEnabled changed to $sdr2hdrEnabled")
            onSDR2HDREnabledChanged()
        }

    private val userSwitchReceiver = object: UserSwitchReceiver(service) {
        override fun onUserSwitched() {
            updateAll()
        }
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        when (uri?.lastPathSegment) {
            IRIS_MEMC_ENABLED -> {
                updateMemcEnabled()
            }
            IRIS_VIDEO_COLOR_BOOST -> {
                updateSDR2HDREnabled()
            }
        }
    }

    private fun loadSystemSettings(key: String, default: Int): Int {
        return Settings.System.getIntForUser(service.contentResolver,
            key, default, UserHandle.USER_CURRENT)
    }

    private fun loadBooleanSettings(key: String, default: Boolean): Boolean {
        return loadSystemSettings(key, if (default) 1 else 0) == 1
    }

    private fun updateMemcEnabled() {
        if (!MEMC_FHD_SUPPORTED && !MEMC_QHD_SUPPORTED) {
            return
        }
        loadBooleanSettings(IRIS_MEMC_ENABLED, false).let {
            if (memcEnabled != it) {
                memcEnabled = it
            }
        }
    }

    private fun updateSDR2HDREnabled() {
        if (!SDR2HDR_SUPPORTED || VIDEO_OSIE_SUPPORTED) {
            return
        }
        loadBooleanSettings(IRIS_VIDEO_COLOR_BOOST, false).let {
            if (sdr2hdrEnabled != it) {
                sdr2hdrEnabled = it
            }
        }
    }

    private fun displayWidthSupportMemc(): Boolean {
        if (MEMC_FHD_SUPPORTED && MEMC_QHD_SUPPORTED) {
            return true
        }
        if (MEMC_FHD_SUPPORTED) {
            return service.resolutionListener.displayWidth == FHD_WIDTH
        }
        if (MEMC_QHD_SUPPORTED) {
            return service.resolutionListener.displayWidth == QHD_WIDTH
        }
        return false
    }

    private fun updateAll() {
        updateMemcEnabled()
        updateSDR2HDREnabled()
    }

    abstract fun onMemcEnabledChanged()
    abstract fun onSDR2HDREnabledChanged()

    companion object {
        private const val TAG = "SystemTool::Iris::SettingsObserver"
    }
}
