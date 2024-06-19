/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.gamemode.bean

import android.app.ActivityTaskManager.INVALID_TASK_ID

data class GameInfo(
    val packageName: String = "",
    val taskId: Int = INVALID_TASK_ID
)
