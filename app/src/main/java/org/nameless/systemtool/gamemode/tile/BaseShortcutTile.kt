/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.tile

import android.os.UserHandle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import org.nameless.app.GameModeInfo
import org.nameless.systemtool.R
import org.nameless.systemtool.gamemode.util.GameModeListenerProxy
import org.nameless.systemtool.gamemode.util.Shared.service

abstract class BaseShortcutTile(
    private val defaultLabelRes: Int,
    private val defaultIconRes: Int,
    val longClickable: Boolean = false
) {

    private var shortcutRoot: View? = null
    private var shortcutIcon: ImageView? = null
    private var shortcutLabel: TextView? = null
    private var shortcutSecondaryLabel: TextView? = null

    private val gameModeChangedCallback = object : GameModeListenerProxy.Callback {
        override fun onGameModeInfoChanged() {
            GameModeListenerProxy.gameModeInfo?.let {
                this@BaseShortcutTile.onGameModeInfoChanged(it)
            }
        }
    }

    var state: Int = STATE_INACTIVE
        set(value) {
            field = value
            service.mainHandler.post {
                shortcutRoot?.apply {
                    background?.setTint(getBackgroundColor())
                    invalidate()
                }
                shortcutIcon?.apply {
                    setImageResource(getIconRes())
                    drawable?.setTint(getIconTint())
                    invalidate()
                }
                shortcutLabel?.apply {
                    text = service.getString(getLabelRes())
                    setTextColor(getLabelColor())
                    invalidate()
                }
                shortcutSecondaryLabel?.apply {
                    text = service.getString(getSecondaryLabelRes())
                    setTextColor(getSecondaryLabelColor())
                    invalidate()
                }
                onStateChanged()
            }
        }

    init {
        GameModeListenerProxy.addCallback(gameModeChangedCallback)
        state = getInitialState()
    }

    fun bind(root: View, icon: ImageView, label: TextView, secondaryLabel: TextView) {
        shortcutRoot = root
        shortcutIcon = icon
        shortcutLabel = label
        shortcutSecondaryLabel = secondaryLabel
        state = state
    }

    open fun destroy() {
        GameModeListenerProxy.gameModeInfo?.let {
            onGameModeInfoChanged(it)
        }
        GameModeListenerProxy.removeCallback(gameModeChangedCallback)
    }

    open fun onGameModeInfoChanged(info: GameModeInfo) {}

    open fun onStateChanged() {}

    open fun getInitialState() = STATE_INACTIVE

    open fun getBackgroundColor(): Int {
        return when (state) {
            STATE_INACTIVE -> service.getColor(R.color.game_panel_background_inactive_default)
            else -> service.getColor(android.R.color.system_accent1_600)
        }
    }

    open fun getIconRes() = defaultIconRes

    open fun getIconTint() = service.getColor(R.color.game_panel_tint_default)

    open fun getLabelRes() = defaultLabelRes

    open fun getLabelColor() = service.getColor(R.color.game_panel_tint_default)

    open fun getSecondaryLabelRes(): Int {
        return when (state) {
            STATE_INACTIVE -> R.string.game_tile_secondary_label_inactive
            else -> R.string.game_tile_secondary_label_active
        }
    }

    open fun getSecondaryLabelColor() = service.getColor(R.color.game_panel_tint_default)

    abstract fun onClicked()

    open fun onLongClicked() {}

    internal object SettingsHelper {
        internal object Secure {
            fun getBoolean(settings: String, defaultValue: Boolean): Boolean {
                return getInt(settings, if (defaultValue) 1 else 0) == 1
            }

            fun getInt(settings: String, defaultValue: Int): Int {
                return Settings.Secure.getIntForUser(
                    service.contentResolver, settings,
                    defaultValue, UserHandle.USER_CURRENT
                )
            }

            fun getString(settings: String): String {
                return Settings.Secure.getStringForUser(
                    service.contentResolver, settings,
                    UserHandle.USER_CURRENT
                )?: String()
            }

            fun putBoolean(settings: String, value: Boolean) {
                putInt(settings, if (value) 1 else 0)
            }

            fun putInt(settings: String, value: Int) {
                putString(settings, value.toString())
            }

            fun putString(settings: String, value: String) {
                Settings.Secure.putStringForUser(
                    service.contentResolver, settings,
                    value, UserHandle.USER_CURRENT
                )
            }
        }

        internal object System {
            fun getBoolean(settings: String, defaultValue: Boolean): Boolean {
                return getInt(settings, if (defaultValue) 1 else 0) == 1
            }

            fun getInt(settings: String, defaultValue: Int): Int {
                return Settings.System.getIntForUser(
                    service.contentResolver, settings,
                    defaultValue, UserHandle.USER_CURRENT
                )
            }

            fun getString(settings: String): String {
                return Settings.System.getStringForUser(
                    service.contentResolver, settings,
                    UserHandle.USER_CURRENT
                )?: String()
            }

            fun putBoolean(settings: String, value: Boolean) {
                putInt(settings, if (value) 1 else 0)
            }

            fun putInt(settings: String, value: Int) {
                putString(settings, value.toString())
            }

            fun putString(settings: String, value: String) {
                Settings.System.putStringForUser(
                    service.contentResolver, settings,
                    value, UserHandle.USER_CURRENT
                )
            }
        }

        internal object Global {
            fun getBoolean(settings: String, defaultValue: Boolean): Boolean {
                return getInt(settings, if (defaultValue) 1 else 0) == 1
            }

            fun getInt(settings: String, defaultValue: Int): Int {
                return Settings.Global.getInt(
                    service.contentResolver, settings,
                    defaultValue
                )
            }

            fun getString(settings: String): String {
                return Settings.Global.getString(
                    service.contentResolver, settings
                )?: String()
            }

            fun putBoolean(settings: String, value: Boolean) {
                putInt(settings, if (value) 1 else 0)
            }

            fun putInt(settings: String, value: Int) {
                putString(settings, value.toString())
            }

            fun putString(settings: String, value: String) {
                Settings.Global.putString(
                    service.contentResolver, settings,
                    value
                )
            }
        }
    }

    companion object {
        const val STATE_INACTIVE = 0
        const val STATE_ACTIVE = 1
    }
}
