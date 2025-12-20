package com.stn.mobai.client;

import com.stn.mobai.STNMobAI;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side initializer for STN Mob AI.
 * Handles any client-specific registration.
 */
public class STNMobAIClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        STNMobAI.LOGGER.info("Survive The Night - Mob AI (Client) initialized!");
    }
}
