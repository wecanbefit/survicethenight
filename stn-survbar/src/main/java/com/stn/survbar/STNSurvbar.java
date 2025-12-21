package com.stn.survbar;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point for STN Survival Bar.
 * This mod displays a HUD showing days until the next survival night.
 */
public class STNSurvbar implements ModInitializer {

    public static final String MOD_ID = "stn_survbar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Survive The Night - Survival Bar initializing...");
        LOGGER.info("Survive The Night - Survival Bar initialized!");
    }
}
