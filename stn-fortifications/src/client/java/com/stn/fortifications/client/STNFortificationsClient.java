package com.stn.fortifications.client;

import com.stn.fortifications.STNFortifications;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side initializer for STN Fortifications.
 * Handles block rendering, damage overlays, and durability HUD.
 */
public class STNFortificationsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        STNFortifications.LOGGER.info("Survive The Night - Fortifications (Client) initialized!");

        // TODO: Register damage overlay renderer
        // TODO: Register durability HUD
        // TODO: Register client network handlers
    }
}
