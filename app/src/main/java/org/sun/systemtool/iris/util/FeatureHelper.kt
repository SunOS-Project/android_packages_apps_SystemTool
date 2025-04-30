/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.sun.systemtool.iris.util

import org.sun.display.DisplayFeatureManager

import vendor.sun.hardware.displayfeature.Feature.MEMC_FHD
import vendor.sun.hardware.displayfeature.Feature.MEMC_QHD
import vendor.sun.hardware.displayfeature.Feature.SDR2HDR
import vendor.sun.hardware.displayfeature.Feature.VIDEO_OSIE

object FeatureHelper {

    val SDR2HDR_SUPPORTED =
            DisplayFeatureManager.getInstance().hasFeature(SDR2HDR)
    val MEMC_FHD_SUPPORTED =
            DisplayFeatureManager.getInstance().hasFeature(MEMC_FHD)
    val MEMC_QHD_SUPPORTED =
            DisplayFeatureManager.getInstance().hasFeature(MEMC_QHD)
    val VIDEO_OSIE_SUPPORTED =
            DisplayFeatureManager.getInstance().hasFeature(VIDEO_OSIE)

    val irisSupported = SDR2HDR_SUPPORTED || MEMC_FHD_SUPPORTED || MEMC_QHD_SUPPORTED
}
