package com.stn.zombies;

import com.stn.survival.spawn.HordeMobRegistry;
import com.stn.survival.spawn.MobCategory;
import com.stn.zombies.config.STNZombiesConfig;
import com.stn.zombies.entity.PlagueZombieEntity;
import com.stn.zombies.registry.STNZombieEntities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Survive The Night - Zombies
 * Adds zombie variants with unique abilities and roles.
 */
public class STNZombies implements ModInitializer {
    public static final String MOD_ID = "stn_zombies";
    public static final Logger LOGGER = LoggerFactory.getLogger("STN-Zombies");

    @Override
    public void onInitialize() {
        LOGGER.info("Survive The Night - Zombies initializing...");

        // Load configuration
        STNZombiesConfig.init();

        // Register entities
        STNZombieEntities.register();

        // Register with horde spawn system
        registerHordeMobs();

        // Clean up plague stacks when player disconnects to prevent memory leak
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            PlagueZombieEntity.clearPlagueStacks(handler.player.getUuid());
        });

        LOGGER.info("Survive The Night - Zombies initialized!");
    }

    private void registerHordeMobs() {
        // Tier 1: Early game (Gamestage 5-20)
        HordeMobRegistry.registerCustomMob("zombabie", STNZombieEntities.ZOMBABIE, 15, 5, MobCategory.GROUND);
        HordeMobRegistry.registerCustomMob("sprinter_zombie", STNZombieEntities.SPRINTER_ZOMBIE, 12, 15, MobCategory.GROUND);
        HordeMobRegistry.registerCustomMob("lumberjack_zombie", STNZombieEntities.LUMBERJACK_ZOMBIE, 10, 20, MobCategory.GROUND);

        // Tier 2: Mid game (Gamestage 25-45)
        HordeMobRegistry.registerCustomMob("bruiser_zombie", STNZombieEntities.BRUISER_ZOMBIE, 8, 25, MobCategory.GROUND);
        HordeMobRegistry.registerCustomMob("spitter_zombie", STNZombieEntities.SPITTER_ZOMBIE, 8, 30, MobCategory.GROUND);
        HordeMobRegistry.registerCustomMob("plague_zombie", STNZombieEntities.PLAGUE_ZOMBIE, 8, 35, MobCategory.GROUND);
        HordeMobRegistry.registerCustomMob("howler_zombie", STNZombieEntities.HOWLER_ZOMBIE, 6, 40, MobCategory.GROUND);
        HordeMobRegistry.registerCustomMob("leech_zombie", STNZombieEntities.LEECH_ZOMBIE, 6, 45, MobCategory.GROUND);

        // Tier 3: Late game (Gamestage 50+)
        HordeMobRegistry.registerCustomMob("shielded_zombie", STNZombieEntities.SHIELDED_ZOMBIE, 5, 50, MobCategory.GROUND);
        HordeMobRegistry.registerCustomMob("electric_zombie", STNZombieEntities.ELECTRIC_ZOMBIE, 4, 60, MobCategory.GROUND);

        LOGGER.info("Registered 10 zombie variants with horde spawn system");
    }
}
