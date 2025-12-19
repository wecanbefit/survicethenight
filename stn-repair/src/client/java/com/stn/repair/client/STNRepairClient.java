package com.stn.repair.client;

import com.stn.repair.STNRepair;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side initializer for STN Repair.
 * Handles hammer HUD overlay.
 */
public class STNRepairClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        STNRepair.LOGGER.info("Survive The Night - Repair (Client) initialized!");

        // TODO: Register hammer HUD overlay
    }
}
