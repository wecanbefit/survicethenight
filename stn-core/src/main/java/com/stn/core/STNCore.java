package com.stn.core;

import com.stn.core.api.STNEvents;
import com.stn.core.api.ISurvivalNightProvider;
import com.stn.core.api.IGamestageProvider;
import com.stn.core.api.ISoundEmitter;
import com.stn.core.api.IDurabilityProvider;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Survive The Night - Core
 * Base mod providing shared API and utilities for all STN mods.
 */
public class STNCore implements ModInitializer {
    public static final String MOD_ID = "stn_core";
    public static final Logger LOGGER = LoggerFactory.getLogger("STN");

    // API providers - set by addon mods
    private static ISurvivalNightProvider survivalNightProvider;
    private static IGamestageProvider gamestageProvider;
    private static ISoundEmitter soundEmitter;
    private static IDurabilityProvider durabilityProvider;

    @Override
    public void onInitialize() {
        LOGGER.info("Survive The Night - Core initializing...");

        // Register events
        STNEvents.init();

        LOGGER.info("Survive The Night - Core initialized!");
    }

    // API Registration Methods
    public static void registerSurvivalNightProvider(ISurvivalNightProvider provider) {
        if (survivalNightProvider != null) {
            LOGGER.warn("SurvivalNightProvider already registered, overwriting!");
        }
        survivalNightProvider = provider;
        LOGGER.info("SurvivalNightProvider registered: {}", provider.getClass().getSimpleName());
    }

    public static void registerGamestageProvider(IGamestageProvider provider) {
        if (gamestageProvider != null) {
            LOGGER.warn("GamestageProvider already registered, overwriting!");
        }
        gamestageProvider = provider;
        LOGGER.info("GamestageProvider registered: {}", provider.getClass().getSimpleName());
    }

    public static void registerSoundEmitter(ISoundEmitter emitter) {
        if (soundEmitter != null) {
            LOGGER.warn("SoundEmitter already registered, overwriting!");
        }
        soundEmitter = emitter;
        LOGGER.info("SoundEmitter registered: {}", emitter.getClass().getSimpleName());
    }

    public static void registerDurabilityProvider(IDurabilityProvider provider) {
        if (durabilityProvider != null) {
            LOGGER.warn("DurabilityProvider already registered, overwriting!");
        }
        durabilityProvider = provider;
        LOGGER.info("DurabilityProvider registered: {}", provider.getClass().getSimpleName());
    }

    // API Getters
    public static ISurvivalNightProvider getSurvivalNightProvider() {
        return survivalNightProvider;
    }

    public static IGamestageProvider getGamestageProvider() {
        return gamestageProvider;
    }

    public static ISoundEmitter getSoundEmitter() {
        return soundEmitter;
    }

    public static IDurabilityProvider getDurabilityProvider() {
        return durabilityProvider;
    }

    // Convenience methods
    public static boolean isSurvivalNightActive() {
        return survivalNightProvider != null && survivalNightProvider.isSurvivalNightActive();
    }

    public static int getWorldGamestage() {
        return gamestageProvider != null ? gamestageProvider.getWorldGamestage() : 0;
    }
}
