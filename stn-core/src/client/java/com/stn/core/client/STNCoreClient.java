package com.stn.core.client;

import com.stn.core.STNCore;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side initializer for STN Core.
 */
public class STNCoreClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        STNCore.LOGGER.info("Survive The Night - Core (Client) initialized!");
    }
}
