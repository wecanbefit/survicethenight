package com.stn.survival;

import com.stn.core.STNCore;
import com.stn.survival.command.SurvivalNightCommand;
import com.stn.survival.config.STNSurvivalConfig;
import com.stn.survival.event.SurvivalNightManager;
import com.stn.survival.network.SurvivalNightSyncPayload;
import com.stn.survival.progression.GamestageManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Survive The Night - Survival Night
 * Handles survival night events, horde spawning, and gamestage progression.
 */
public class STNSurvival implements ModInitializer {
    public static final String MOD_ID = "stn_survival";
    public static final Logger LOGGER = LoggerFactory.getLogger("STN-Survival");

    private static SurvivalNightManager survivalNightManager;
    private static GamestageManager gamestageManager;

    @Override
    public void onInitialize() {
        LOGGER.info("Survive The Night - Survival Night initializing...");

        // Load configuration
        STNSurvivalConfig.init();

        // Register network payloads
        PayloadTypeRegistry.playS2C().register(SurvivalNightSyncPayload.ID, SurvivalNightSyncPayload.CODEC);

        // Set up server lifecycle events
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onServerStopping);

        // Register tick event
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

        // Register commands
        SurvivalNightCommand.register();

        LOGGER.info("Survive The Night - Survival Night initialized!");
    }

    private void onServerStarted(MinecraftServer server) {
        LOGGER.info("Setting up Survival Night systems...");

        // Create managers
        survivalNightManager = new SurvivalNightManager(server);
        gamestageManager = new GamestageManager(server);

        // Link managers
        survivalNightManager.setGamestageManager(gamestageManager);

        // Register with STNCore API
        STNCore.registerSurvivalNightProvider(survivalNightManager);
        STNCore.registerGamestageProvider(gamestageManager);

        LOGGER.info("Survival Night systems ready!");
    }

    private void onServerStopping(MinecraftServer server) {
        LOGGER.info("Saving Survival Night data...");

        if (gamestageManager != null) {
            gamestageManager.saveData();
        }

        survivalNightManager = null;
        gamestageManager = null;
    }

    private void onServerTick(MinecraftServer server) {
        if (survivalNightManager != null) {
            survivalNightManager.tick();
        }

        if (gamestageManager != null) {
            gamestageManager.tick();
        }
    }

    public static SurvivalNightManager getSurvivalNightManager() {
        return survivalNightManager;
    }

    public static GamestageManager getGamestageManager() {
        return gamestageManager;
    }
}
