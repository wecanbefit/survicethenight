package com.stn.mobai;

import com.stn.mobai.command.DebugCommand;
import com.stn.mobai.debug.SenseDebugger;
import com.stn.mobai.entity.ai.sense.SenseManager;
import com.stn.mobai.event.MobSpawnHandler;
import com.stn.mobai.event.SoundEventHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Survive The Night - Mob AI
 * Enhanced AI system for hostile mobs with sensory detection and block breaking.
 * Can be used standalone or as a dependency for mob addon mods.
 */
public class STNMobAI implements ModInitializer {
    public static final String MOD_ID = "stn_mobai";
    public static final Logger LOGGER = LoggerFactory.getLogger("STN-MobAI");

    @Override
    public void onInitialize() {
        LOGGER.info("Survive The Night - Mob AI initializing...");

        // Initialize the sense manager
        SenseManager.getInstance().init();

        // Register sound event handlers (block break, etc.)
        SoundEventHandler.register();

        // Register mob spawn handler for AI injection
        MobSpawnHandler.register();

        // Register commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            DebugCommand.register(dispatcher);
        });

        // Register server tick event
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world instanceof ServerWorld serverWorld) {
                SenseManager.getInstance().tick(serverWorld);
                SenseDebugger.tick(serverWorld);
            }
        });

        LOGGER.info("Survive The Night - Mob AI initialized!");
        LOGGER.info("Use /mobai debug to toggle debug mode");
    }

    /**
     * Get the SenseManager instance for emitting sounds.
     * Other mods can use this to register sounds that mobs will detect.
     */
    public static SenseManager getSenseManager() {
        return SenseManager.getInstance();
    }
}
