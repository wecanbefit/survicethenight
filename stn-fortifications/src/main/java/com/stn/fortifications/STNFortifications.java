package com.stn.fortifications;

import com.stn.core.STNCore;
import com.stn.fortifications.registry.STNBlocks;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Survive The Night - Fortifications
 * Defensive blocks, spikes, electric fences, and block durability system.
 */
public class STNFortifications implements ModInitializer {
    public static final String MOD_ID = "stn_fortifications";
    public static final Logger LOGGER = LoggerFactory.getLogger("STN-Fortifications");

    @Override
    public void onInitialize() {
        LOGGER.info("Survive The Night - Fortifications initializing...");

        // Register blocks and items
        STNBlocks.register();

        LOGGER.info("Survive The Night - Fortifications initialized!");
    }
}
