/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.gamemode.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.icu.lang.UCharacter
import android.icu.text.DateTimePatternGenerator
import android.net.Uri
import android.os.Handler
import android.os.SystemClock
import android.os.UserHandle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.format.DateFormat
import android.text.style.RelativeSizeSpan
import android.util.AttributeSet
import android.widget.TextView

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

import org.sun.provider.SettingsExt.System.STATUSBAR_CLOCK_AM_PM_STYLE
import org.sun.provider.SettingsExt.System.STATUSBAR_CLOCK_DATE_DISPLAY
import org.sun.provider.SettingsExt.System.STATUSBAR_CLOCK_DATE_FORMAT
import org.sun.provider.SettingsExt.System.STATUSBAR_CLOCK_DATE_POSITION
import org.sun.provider.SettingsExt.System.STATUSBAR_CLOCK_DATE_STYLE
import org.sun.provider.SettingsExt.System.STATUSBAR_CLOCK_SECONDS
import org.sun.systemtool.gamemode.util.Shared.service

@Suppress("DEPRECATION")
@SuppressLint("AppCompatCustomView")
class ClockView(
    context: Context,
    attrs: AttributeSet
) : TextView(context, attrs) {

    private var attached = false

    private var amPmStyle = AM_PM_STYLE_GONE
    private var clockDateDisplay = CLOCK_DATE_DISPLAY_GONE
    private var clockDatePosition = CLOCK_DATE_STYLE_LEFT
    private var clockDateStyle = CLOCK_DATE_STYLE_REGULAR
    private var showSeconds = false

    private var calendar: Calendar? = null
    private var clockDateFormat: String? = null
    private var clockFormat: SimpleDateFormat? = null
    private var contentDescriptionFormat: SimpleDateFormat? = null
    private var contentDescriptionFormatString: String? = null
    private var dateTimePatternGenerator: DateTimePatternGenerator? = null
    private var locale: Locale? = null
    private val secondsHandler by lazy { Handler() }

    private val secondTick: Runnable = object : Runnable {
        override fun run() {
            if (calendar != null) {
                updateClock()
            }
            secondsHandler.postAtTime(this, SystemClock.uptimeMillis() / 1000 * 1000 + 1000)
        }
    }

    private val intentReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_TIMEZONE_CHANGED -> {
                    calendar = Calendar.getInstance(TimeZone.getTimeZone(
                            intent.getStringExtra(Intent.EXTRA_TIMEZONE)))
                    clockFormat?.apply {
                        timeZone = calendar.timeZone
                    }
                }
                Intent.ACTION_CONFIGURATION_CHANGED -> {
                    resources.configuration.locale.let { newLocale ->
                        if (newLocale == locale) return@let
                        locale = newLocale
                        contentDescriptionFormatString = String()
                        dateTimePatternGenerator = null
                        updateSettings()
                    }
                }
            }
            updateClock()
        }
    }
    private val settingsObserver by lazy { SettingsObserver() }

    init {
        includeFontPadding = false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!attached) {
            attached = true

            service.registerReceiver(intentReceiver, IntentFilter().apply {
                addAction(Intent.ACTION_TIME_TICK)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
                addAction(Intent.ACTION_CONFIGURATION_CHANGED)
            })

            settingsObserver.register()
        }

        calendar = Calendar.getInstance(TimeZone.getDefault())
        contentDescriptionFormatString = String()
        dateTimePatternGenerator = null

        updateSettings()
        updateShowSeconds()
    }

    override fun onDetachedFromWindow() {
        secondsHandler.removeCallbacks(secondTick)
        if (attached) {
            service.unregisterReceiver(intentReceiver)
            settingsObserver.unregister()
            attached = false
        }
        super.onDetachedFromWindow()
    }

    private fun updateClock() {
        updateClock(false)
    }

    private fun updateClock(forceUpdate: Boolean) {
        calendar?.timeInMillis = System.currentTimeMillis()
        getSmallTime().let {
            if (forceUpdate || !TextUtils.equals(it, text)) {
                text = it
            }
        }
        contentDescription = calendar?.time?.let { contentDescriptionFormat?.format(it) } ?: String()
    }

    private fun updateSettings() {
        with(service.contentResolver) {
            amPmStyle = if (DateFormat.is24HourFormat(service)) {
                AM_PM_STYLE_GONE
            } else {
                Settings.System.getIntForUser(this,
                    STATUSBAR_CLOCK_AM_PM_STYLE,
                    AM_PM_STYLE_GONE,
                    UserHandle.USER_CURRENT
                )
            }
            clockDateDisplay = Settings.System.getIntForUser(this,
                STATUSBAR_CLOCK_DATE_DISPLAY,
                CLOCK_DATE_DISPLAY_GONE,
                UserHandle.USER_CURRENT
            )
            clockDateFormat = Settings.System.getStringForUser(this,
                STATUSBAR_CLOCK_DATE_FORMAT,
                UserHandle.USER_CURRENT
            )
            clockDatePosition = Settings.System.getIntForUser(this,
                STATUSBAR_CLOCK_DATE_POSITION,
                CLOCK_DATE_STYLE_LEFT,
                UserHandle.USER_CURRENT
            )
            clockDateStyle = Settings.System.getIntForUser(this,
                STATUSBAR_CLOCK_DATE_STYLE,
                CLOCK_DATE_STYLE_REGULAR,
                UserHandle.USER_CURRENT
            )
            showSeconds = Settings.System.getIntForUser(this,
                STATUSBAR_CLOCK_SECONDS,
                CLOCK_SECOND_DISABLED,
                UserHandle.USER_CURRENT
            ) != CLOCK_SECOND_DISABLED
        }

        contentDescriptionFormatString = String()
        dateTimePatternGenerator = null

        if (attached) {
            updateClock(true)
            updateShowSeconds()
        }
    }

    private fun updateShowSeconds() {
        if (showSeconds) {
            // Wait until we have a display to start trying to show seconds.
            secondsHandler.postAtTime(
                secondTick,
                SystemClock.uptimeMillis() / 1000 * 1000 + 1000
            )
        } else {
            secondsHandler.removeCallbacks(secondTick)
            updateClock()
        }
    }

    private fun getSmallTime(): CharSequence {
        val is24 = DateFormat.is24HourFormat(context, UserHandle.USER_CURRENT)
        if (dateTimePatternGenerator == null) {
            dateTimePatternGenerator = DateTimePatternGenerator.getInstance(
                context.resources.configuration.locale
            )
        }
        val formatSkeleton = if (showSeconds) {
            if (is24) {
                "Hms"
            } else {
                "hms"
            }
        } else if (is24) {
            "Hm"
        } else {
            "hm"
        }
        var format = dateTimePatternGenerator?.getBestPattern(formatSkeleton) ?: String()
        if (format != contentDescriptionFormatString) {
            contentDescriptionFormatString = format
            contentDescriptionFormat = SimpleDateFormat(format)

            if (amPmStyle != AM_PM_STYLE_NORMAL) {
                var a = -1
                var quoted = false
                for (i in format.indices) {
                    if (format[i] == '\'') {
                        quoted = !quoted
                    }
                    if (!quoted && format[i] == 'a') {
                        a = i
                        break
                    }
                }
                if (a >= 0) {
                    val b = a
                    while (a > 0 && UCharacter.isUWhiteSpace(format[a - 1].code)) {
                        a--
                    }
                    format = (format.substring(0, a) + MAGIC1 + format.substring(a, b)
                            + "a" + MAGIC2 + format.substring(b + 1))
                }
            }
            clockFormat = SimpleDateFormat(format)
        }
        var dateString: CharSequence? = null
        val dateResult: String
        val result: String
        val timeResult = calendar?.time?.let { clockFormat?.format(it) } ?: String()
        if (clockDateDisplay != CLOCK_DATE_DISPLAY_GONE) {
            val now = Date()
            dateString = if (clockDateFormat.isNullOrBlank()) {
                DateFormat.format("EEE", now)
            } else {
                DateFormat.format(clockDateFormat, now)
            }
            dateResult = if (clockDateStyle == CLOCK_DATE_STYLE_LOWERCASE) {
                dateString.toString().lowercase(Locale.getDefault())
            } else if (clockDateStyle == CLOCK_DATE_STYLE_UPPERCASE) {
                dateString.toString().uppercase(Locale.getDefault())
            } else {
                dateString.toString()
            }
            result = if (clockDatePosition == CLOCK_DATE_STYLE_LEFT) {
                "$dateResult $timeResult"
            } else {
                "$timeResult $dateResult"
            }
        } else {
            result = timeResult
        }
        val formatted = SpannableStringBuilder(result)
        if (clockDateDisplay != CLOCK_DATE_DISPLAY_NORMAL) {
            if (dateString != null) {
                val dateStringLen = dateString.length
                val timeStringOffset = if (clockDatePosition == CLOCK_DATE_STYLE_RIGHT) {
                    timeResult.length + 1
                } else {
                    0
                }
                if (clockDateDisplay == CLOCK_DATE_DISPLAY_GONE) {
                    formatted.delete(0, dateStringLen)
                } else if (clockDateDisplay == CLOCK_DATE_DISPLAY_SMALL) {
                    val style = RelativeSizeSpan(0.7f)
                    formatted.setSpan(
                        style, timeStringOffset,
                        timeStringOffset + dateStringLen,
                        SpannableStringBuilder.SPAN_EXCLUSIVE_INCLUSIVE
                    )
                }
            }
        }
        if (amPmStyle != AM_PM_STYLE_NORMAL) {
            val magic1 = result.indexOf(MAGIC1)
            val magic2 = result.indexOf(MAGIC2)
            if (magic1 in 0 until magic2) {
                if (amPmStyle == AM_PM_STYLE_GONE) {
                    formatted.delete(magic1, magic2 + 1)
                } else {
                    if (amPmStyle == AM_PM_STYLE_SMALL) {
                        val style = RelativeSizeSpan(0.7f)
                        formatted.setSpan(
                            style, magic1, magic2,
                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                        )
                    }
                    formatted.delete(magic2, magic2 + 1)
                    formatted.delete(magic1, magic1 + 1)
                }
            }
        }
        return formatted
    }

    private inner class SettingsObserver : ContentObserver(service.mainHandler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            updateSettings()
        }

        fun register() {
            with(service.contentResolver) {
                listOf(
                    STATUSBAR_CLOCK_AM_PM_STYLE,
                    STATUSBAR_CLOCK_SECONDS,
                    STATUSBAR_CLOCK_DATE_DISPLAY,
                    STATUSBAR_CLOCK_DATE_FORMAT,
                    STATUSBAR_CLOCK_DATE_POSITION,
                    STATUSBAR_CLOCK_DATE_STYLE
                ).forEach {
                    registerContentObserver(Settings.System.getUriFor(it),
                            false, this@SettingsObserver, UserHandle.USER_ALL)
                }
            }
        }

        fun unregister() {
            with(service.contentResolver) {
                unregisterContentObserver(this@SettingsObserver)
            }
        }
    }

    companion object {
        private const val MAGIC1 = '\uEF00'
        private const val MAGIC2 = '\uEF01'

        private const val AM_PM_STYLE_NORMAL = 0
        private const val AM_PM_STYLE_SMALL = 1
        private const val AM_PM_STYLE_GONE = 2

        private const val CLOCK_DATE_DISPLAY_GONE = 0
        private const val CLOCK_DATE_DISPLAY_SMALL = 0
        private const val CLOCK_DATE_DISPLAY_NORMAL = 0

        private const val CLOCK_DATE_STYLE_REGULAR = 0
        private const val CLOCK_DATE_STYLE_LOWERCASE = 1
        private const val CLOCK_DATE_STYLE_UPPERCASE = 2

        private const val CLOCK_DATE_STYLE_LEFT = 0
        private const val CLOCK_DATE_STYLE_RIGHT = 1

        private const val CLOCK_SECOND_DISABLED = 0
    }
}
