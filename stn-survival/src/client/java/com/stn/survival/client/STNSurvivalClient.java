package com.stn.survival.client;

import com.stn.survival.STNSurvival;
import com.stn.survival.client.hud.GamestageHudOverlay;
import com.stn.survival.network.GamestageHudPayload;
import com.stn.survival.network.SurvivalNightSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Client-side initializer for STN Survival.
 * Handles visual effects, HUD elements, and client rendering.
 */
public class STNSurvivalClient implements ClientModInitializer {

    private static boolean survivalNightActive = false;

    @Override
    public void onInitializeClient() {
        STNSurvival.LOGGER.info("Survive The Night - Survival Night (Client) initialized!");

        // Register client network handler for survival night sync
        ClientPlayNetworking.registerGlobalReceiver(SurvivalNightSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                survivalNightActive = payload.isActive();
                STNSurvival.LOGGER.debug("Survival Night state updated: {}", survivalNightActive);
            });
        });

        // Register client network handler for gamestage HUD sync
        ClientPlayNetworking.registerGlobalReceiver(GamestageHudPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                GamestageHudOverlay.updateData(
                    payload.currentDay(),
                    payload.worldGamestage(),
                    payload.playerDeaths(),
                    payload.globalDeaths()
                );
            });
        });

        // Register the gamestage HUD overlay
        GamestageHudOverlay.register();

        STNSurvival.LOGGER.info("Gamestage HUD registered");
    }

    public static boolean isSurvivalNightActive() {
        return survivalNightActive;
    }

    public static void setSurvivalNightActive(boolean active) {
        survivalNightActive = active;
    }
}
