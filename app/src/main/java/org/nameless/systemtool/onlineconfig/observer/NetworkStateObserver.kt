/*
 * Copyright (C) 2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.onlineconfig.observer

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.net.NetworkRequest
import android.os.Handler

import org.nameless.systemtool.onlineconfig.util.Constants.WIFI_AVAILABLE_UPDATE_DELAY
import org.nameless.systemtool.onlineconfig.util.Shared.connectivityManager
import org.nameless.systemtool.onlineconfig.util.Shared.updatePendingWifi
import org.nameless.systemtool.onlineconfig.util.Shared.updateScheduler
import org.nameless.systemtool.onlineconfig.util.Shared.wifiAvailable

class NetworkStateObserver(
    private val handler: Handler
) : ConnectivityManager.NetworkCallback() {

    var registered = false
        set(value) {
            if (field == value) {
                return
            }
            field = value
            if (value) {
                connectivityManager.registerNetworkCallback(
                    NetworkRequest.Builder().build(), this, handler)
            } else {
                connectivityManager.unregisterNetworkCallback(this)
            }
        }

    override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
        wifiAvailable = false
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        wifiAvailable = false
    }

    override fun onUnavailable() {
        super.onUnavailable()
        wifiAvailable = false
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        val wasWifiAvailable = wifiAvailable
        wifiAvailable = networkCapabilities.hasCapability(NET_CAPABILITY_VALIDATED)
                && networkCapabilities.hasTransport(TRANSPORT_WIFI)
        if (wifiAvailable && !wasWifiAvailable && updatePendingWifi) {
            updatePendingWifi = false
            updateScheduler.scheduler = WIFI_AVAILABLE_UPDATE_DELAY
        }
    }
}
