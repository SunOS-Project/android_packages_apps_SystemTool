/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.pixelworks

import java.util.ArrayList

import vendor.pixelworks.hardware.display.V1_0.IIris

class IrisConfigureGetCallback : IIris.irisConfigureGetCallback {

    var result = -1
    var values: ArrayList<Int>? = null

    override fun onValues(result: Int, values: ArrayList<Int>?) {
        this.result = result
        this.values = values
    }
}
