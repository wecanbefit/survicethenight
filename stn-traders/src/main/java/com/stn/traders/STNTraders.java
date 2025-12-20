package com.stn.traders;

import com.stn.core.STNCore;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Survive The Night - Traders
 * Trading mechanics and merchant entities.
 */
public class STNTraders implements ModInitializer {
    public static final String MOD_ID = "stn_traders";
    public static final Logger LOGGER = LoggerFactory.getLogger("STN-Traders");

    @Override
    public void onInitialize() {
        LOGGER.info("Survive The Night - Traders initializing...");

        // TODO: Register traders and trading mechanics

        LOGGER.info("Survive The Night - Traders initialized!");
    }
}
