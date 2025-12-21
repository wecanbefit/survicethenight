package com.stn.survbar.client;

import com.stn.survbar.STNSurvbar;
import com.stn.survbar.client.command.SurvbarCommand;
import com.stn.survbar.client.hud.SurvivalBarHudOverlay;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side initializer for STN Survival Bar.
 * Registers the survival bar HUD overlay and commands.
 */
public class STNSurvbarClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        STNSurvbar.LOGGER.info("Survive The Night - Survival Bar (Client) initializing...");

        // Register survival bar HUD overlay
        SurvivalBarHudOverlay.register();

        // Register /stnsb command for adjusting position
        SurvbarCommand.register();

        STNSurvbar.LOGGER.info("Survive The Night - Survival Bar (Client) initialized!");
    }
}
