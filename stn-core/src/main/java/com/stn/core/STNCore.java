package com.stn.core;

import com.stn.core.api.STNEvents;
import com.stn.core.api.ISurvivalNightProvider;
import com.stn.core.api.IGamestageProvider;
import com.stn.core.api.ISoundEmitter;
import com.stn.core.api.IDurabilityProvider;
import com.stn.core.api.IBlockProtectionProvider;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
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
    private static IBlockProtectionProvider blockProtectionProvider;

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

    public static void registerBlockProtectionProvider(IBlockProtectionProvider provider) {
        if (blockProtectionProvider != null) {
            LOGGER.warn("BlockProtectionProvider already registered, overwriting!");
        }
        blockProtectionProvider = provider;
        LOGGER.info("BlockProtectionProvider registered: {}", provider.getClass().getSimpleName());
    }

    // API Getters - may return null if no provider registered
    @Nullable
    public static ISurvivalNightProvider getSurvivalNightProvider() {
        return survivalNightProvider;
    }

    @Nullable
    public static IGamestageProvider getGamestageProvider() {
        return gamestageProvider;
    }

    @Nullable
    public static ISoundEmitter getSoundEmitter() {
        return soundEmitter;
    }

    @Nullable
    public static IDurabilityProvider getDurabilityProvider() {
        return durabilityProvider;
    }

    @Nullable
    public static IBlockProtectionProvider getBlockProtectionProvider() {
        return blockProtectionProvider;
    }

    // Convenience methods
    public static boolean isSurvivalNightActive() {
        return survivalNightProvider != null && survivalNightProvider.isSurvivalNightActive();
    }

    public static int getWorldGamestage() {
        return gamestageProvider != null ? gamestageProvider.getWorldGamestage() : 0;
    }

    public static boolean isBlockProtected(World world, BlockPos pos) {
        return blockProtectionProvider != null && blockProtectionProvider.isProtected(world, pos);
    }
}
