package com.stn.zombies.client;

import com.stn.zombies.STNZombies;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side initializer for STN Zombies.
 * Handles entity renderers and models.
 */
public class STNZombiesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        STNZombies.LOGGER.info("Survive The Night - Zombies (Client) initialized!");

        // TODO: Register entity renderers
        // TODO: Register GeckoLib models
    }
}
