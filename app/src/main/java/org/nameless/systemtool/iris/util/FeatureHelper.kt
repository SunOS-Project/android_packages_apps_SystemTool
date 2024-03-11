/*
 * Copyright (C) 2023-2024 The Nameless-AOSP Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.nameless.systemtool.iris.util

import org.nameless.display.DisplayFeatureManager

import vendor.nameless.hardware.displayfeature.V1_0.Feature.MEMC_FHD
import vendor.nameless.hardware.displayfeature.V1_0.Feature.MEMC_QHD
import vendor.nameless.hardware.displayfeature.V1_0.Feature.SDR2HDR
import vendor.nameless.hardware.displayfeature.V1_0.Feature.VIDEO_OSIE

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
