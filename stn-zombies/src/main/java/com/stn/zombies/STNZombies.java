package com.stn.zombies;

import com.stn.core.STNCore;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Survive The Night - Zombies
 * Custom zombie entities, AI behaviors, and sensory detection systems.
 */
public class STNZombies implements ModInitializer {
    public static final String MOD_ID = "stn_zombies";
    public static final Logger LOGGER = LoggerFactory.getLogger("STN-Zombies");

    @Override
    public void onInitialize() {
        LOGGER.info("Survive The Night - Zombies initializing...");

        // TODO: Load configuration
        // TODO: Register entities
        // TODO: Register sound event handler

        LOGGER.info("Survive The Night - Zombies initialized!");
    }
}
