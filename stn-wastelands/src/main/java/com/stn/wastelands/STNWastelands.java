package com.stn.wastelands;

import com.stn.core.STNCore;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Survive The Night - Wastelands
 * Wasteland biomes and environmental hazards.
 */
public class STNWastelands implements ModInitializer {
    public static final String MOD_ID = "stn_wastelands";
    public static final Logger LOGGER = LoggerFactory.getLogger("STN-Wastelands");

    @Override
    public void onInitialize() {
        LOGGER.info("Survive The Night - Wastelands initializing...");

        // TODO: Register biomes, features, and hazards

        LOGGER.info("Survive The Night - Wastelands initialized!");
    }
}
