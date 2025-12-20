package com.stn.skeletons;

import com.stn.skeletons.registry.STNSkeletonEntities;
import com.stn.survival.spawn.HordeMobRegistry;
import com.stn.survival.spawn.MobCategory;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STN Skeletons - Adds skeleton variants to Survive The Night.
 *
 * Bow Skeletons:
 * - Marksman: High damage, slow fire, perfect accuracy
 * - Suppressor: Fast fire, applies slowness
 * - Flame Archer: Shoots flaming arrows
 *
 * Black Skeletons:
 * - Vanguard: Tanky frontline with heavy sword
 * - Duelist: Fast and aggressive with dash
 * - Reaper: Executes low HP targets, gains speed on kills
 */
public class STNSkeletons implements ModInitializer {

    public static final String MOD_ID = "stn_skeletons";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing STN Skeletons...");

        // Register all skeleton entities
        STNSkeletonEntities.register();

        // Register with horde spawn system
        registerHordeMobs();

        LOGGER.info("STN Skeletons initialized with 6 skeleton variants!");
    }

    private void registerHordeMobs() {
        // Bow Skeletons - Early to mid game
        HordeMobRegistry.registerCustomMob("suppressor_skeleton", STNSkeletonEntities.SUPPRESSOR_SKELETON, 10, 15, MobCategory.GROUND);
        HordeMobRegistry.registerCustomMob("marksman_skeleton", STNSkeletonEntities.MARKSMAN_SKELETON, 8, 20, MobCategory.GROUND);
        HordeMobRegistry.registerCustomMob("flame_archer_skeleton", STNSkeletonEntities.FLAME_ARCHER_SKELETON, 8, 30, MobCategory.GROUND);

        // Black Skeletons - Mid to late game
        HordeMobRegistry.registerCustomMob("duelist_skeleton", STNSkeletonEntities.DUELIST_SKELETON, 6, 35, MobCategory.GROUND);
        HordeMobRegistry.registerCustomMob("vanguard_skeleton", STNSkeletonEntities.VANGUARD_SKELETON, 6, 40, MobCategory.GROUND);
        HordeMobRegistry.registerCustomMob("reaper_skeleton", STNSkeletonEntities.REAPER_SKELETON, 4, 60, MobCategory.GROUND);

        LOGGER.info("Registered 6 skeleton variants with horde spawn system");
    }
}
