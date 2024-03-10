/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.util

import android.os.SystemProperties

object Constants {

    const val IRIS_COOKIE = -2138930830L

    const val INTENT_DEBUG_GET_COMMAND = "org.nameless.systemtool.intent.IRIS_GET_COMMAND"
    const val INTENT_DEBUG_SET_COMMAND = "org.nameless.systemtool.intent.IRIS_SET_COMMAND"

    val IRIS_CONFIG_PATH: String = SystemProperties.get(
            "persist.sys.nameless.display.iris.config",
            "/system_ext/etc/pixelworks_apps.xml")
}
