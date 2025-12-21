package com.stn.repair.config;

import com.stn.repair.STNRepair;

/**
 * Configuration for STN Repair module.
 * Currently stores HUD position and other repair-related settings.
 */
public class STNRepairConfig {

    // HUD position: tl (top-left), tm (top-middle), tr (top-right),
    //               ml (middle-left), mr (middle-right),
    //               bl (bottom-left), bm (bottom-middle), br (bottom-right)
    public static String HAMMER_HUD_POSITION = "bm"; // Default: bottom-middle (below crosshair)

    public static void init() {
        STNRepair.LOGGER.info("STN Repair config loaded with defaults");
        // TODO: Load from config file in future if needed
    }
}
