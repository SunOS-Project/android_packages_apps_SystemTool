/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.tile

import android.os.PowerManager

import org.nameless.systemtool.R
import org.nameless.systemtool.common.Utils.logE
import org.nameless.systemtool.gamemode.util.Shared.powerManager

@Suppress("DEPRECATION")
class KeepWakeTIle : BaseShortcutTile(
    R.string.game_tile_keep_awake_title,
    R.drawable.ic_keep_awake
) {

    private val wakeLock by lazy {
        powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, TAG)
    }
    private var keepWake = false

    override fun getInitialState() = STATE_INACTIVE

    override fun onClicked() {
        if (keepWake) {
            try {
                wakeLock.release()
            } catch (e: Exception) {
                logE(TAG, "Exception on releasing wake lock", e)
            }
        } else {
            wakeLock.acquire()
        }
        keepWake = !keepWake.also { switchState() }
    }

    override fun onDetach() {
        if (keepWake) {
            try {
                wakeLock.release()
            } catch (e: Exception) {
                logE(TAG, "Exception on releasing wake lock", e)
            }
        }
        super.onDetach()
    }

    private fun switchState() {
        when (state) {
            STATE_ACTIVE -> STATE_INACTIVE
            STATE_INACTIVE -> STATE_ACTIVE
            else -> null
        }?.let {
            state = it
        }
    }

    companion object {
        private const val TAG = "SystemTool::KeepWakeTIle"
    }
}
