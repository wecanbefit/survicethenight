package com.stn.bloodmoon.client;

import com.stn.bloodmoon.STNBloodmoon;
import net.fabricmc.api.ClientModInitializer;

/**
 * Client-side initializer for STN Bloodmoon.
 * Handles visual effects, HUD elements, and client rendering.
 */
public class STNBloodmoonClient implements ClientModInitializer {

    private static boolean bloodMoonActive = false;

    @Override
    public void onInitializeClient() {
        STNBloodmoon.LOGGER.info("Survive The Night - Blood Moon (Client) initialized!");

        // TODO: Register blood moon visual effects
        // TODO: Register client network handlers
    }

    public static boolean isBloodMoonActive() {
        return bloodMoonActive;
    }

    public static void setBloodMoonActive(boolean active) {
        bloodMoonActive = active;
    }
}
