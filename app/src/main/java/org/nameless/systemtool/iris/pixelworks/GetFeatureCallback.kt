/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.pixelworks

import vendor.pixelworks.hardware.feature.V1_0.IIrisFeature

class GetFeatureCallback : IIrisFeature.GetFeatureCallback {

    var result = -1
    var feature = -1

    override fun onValues(result: Int, feature: Int) {
        this.result = result
        this.feature = feature
    }
}
