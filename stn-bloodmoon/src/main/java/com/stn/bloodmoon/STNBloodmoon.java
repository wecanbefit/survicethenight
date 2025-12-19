package com.stn.bloodmoon;

import com.stn.core.STNCore;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Survive The Night - Blood Moon
 * Handles blood moon events, horde spawning, and gamestage progression.
 */
public class STNBloodmoon implements ModInitializer {
    public static final String MOD_ID = "stn_bloodmoon";
    public static final Logger LOGGER = LoggerFactory.getLogger("STN-Bloodmoon");

    @Override
    public void onInitialize() {
        LOGGER.info("Survive The Night - Blood Moon initializing...");

        // TODO: Load configuration
        // TODO: Register commands
        // TODO: Register network packets
        // TODO: Set up server lifecycle events
        // TODO: Register API providers with STNCore

        LOGGER.info("Survive The Night - Blood Moon initialized!");
    }
}
