/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.observer

import org.nameless.systemtool.common.Utils.logD
import org.nameless.systemtool.iris.util.Shared.appFocusManager
import org.nameless.view.IAppFocusObserver

abstract class TaskStackChangeListener {

    private var registered = false

    var topPackage = String()
    private var topActivity = String()

    private val appFocusObserver = object : IAppFocusObserver.Stub() {
        override fun onFullscreenFocusChanged(packageName: String?, activityName: String?) {
            topPackage = packageName?: String()
            logD(TAG, "Top package changed to $topPackage")

            topActivity = activityName?: String()
            logD(TAG, "Top activity changed to $topActivity")

            onTopStackChanged(topPackage, topActivity)
        }
    }

    fun forceCheckTopActivity() {
        appFocusManager.topFullscreenAppInfo?.let {
            topPackage = it.packageName
            topActivity = it.activityName
            onTopStackChanged(topPackage, topActivity)
        }
    }

    fun register() {
        if (registered) {
            return
        }
        registered = true
        appFocusManager.registerAppFocusObserver(appFocusObserver, true)
    }

    fun unregister() {
        if (!registered) {
            return
        }
        registered = false
        appFocusManager.unregisterAppFocusObserver(appFocusObserver)
        topPackage = String()
        topActivity = String()
    }

    abstract fun onTopStackChanged(packageName: String, activityName: String)

    companion object {
        private const val TAG = "SystemTool::Iris::TaskStackChangeListener"
    }
}
