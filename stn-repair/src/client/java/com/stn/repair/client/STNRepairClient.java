package com.stn.repair.client;

import com.stn.repair.STNRepair;
import com.stn.repair.client.hud.HammerHudOverlay;
import com.stn.repair.config.STNRepairConfig;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side initializer for STN Repair.
 * Handles hammer HUD overlay.
 */
public class STNRepairClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        STNRepair.LOGGER.info("Survive The Night - Repair (Client) initializing...");

        // Load config
        STNRepairConfig.init();

        // Register hammer HUD overlay
        HammerHudOverlay.register();

        STNRepair.LOGGER.info("Survive The Night - Repair (Client) initialized!");
    }
}
