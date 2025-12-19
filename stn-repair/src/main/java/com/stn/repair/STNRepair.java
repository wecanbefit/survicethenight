package com.stn.repair;

import com.stn.core.STNCore;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Survive The Night - Repair
 * Hammer tools for repairing fortified blocks.
 */
public class STNRepair implements ModInitializer {
    public static final String MOD_ID = "stn_repair";
    public static final Logger LOGGER = LoggerFactory.getLogger("STN-Repair");

    @Override
    public void onInitialize() {
        LOGGER.info("Survive The Night - Repair initializing...");

        // TODO: Register items

        LOGGER.info("Survive The Night - Repair initialized!");
    }
}
