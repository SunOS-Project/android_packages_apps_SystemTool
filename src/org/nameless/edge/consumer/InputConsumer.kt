/*
 * Copyright (C) 2023 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.edge.consumer

import android.view.MotionEvent

interface InputConsumer {

    fun onMotionEvent(ev: MotionEvent, fromLeft: Boolean) {}
}
