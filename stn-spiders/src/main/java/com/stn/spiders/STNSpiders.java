package com.stn.spiders;

import com.stn.spiders.registry.STNSpiderEntities;
import com.stn.survival.spawn.HordeMobRegistry;
import com.stn.survival.spawn.MobCategory;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * STN Spiders - Adds spider variants to Survive The Night.
 *
 * Spider Types:
 * - Stalker: Ambush predator, semi-invisible in darkness
 * - Webspinner: Control, places cobwebs on hit
 * - Leaper: Extended leap, targets elevated players
 * - Broodmother: Summoner mini-boss, spawns spiderlings
 * - Venom: Stacking poison DOT
 * - Burden: Armored debuffer, applies weakness/fatigue
 */
public class STNSpiders implements ModInitializer {

    public static final String MOD_ID = "stn_spiders";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing STN Spiders...");

        STNSpiderEntities.register();

        // Register with horde spawn system
        registerHordeMobs();

        LOGGER.info("STN Spiders initialized with 6 spider variants!");
    }

    private void registerHordeMobs() {
        // Early game spiders
        HordeMobRegistry.registerCustomMob("stalker_spider", STNSpiderEntities.STALKER_SPIDER, 10, 15, MobCategory.GROUND);
        HordeMobRegistry.registerCustomMob("leaper_spider", STNSpiderEntities.LEAPER_SPIDER, 10, 20, MobCategory.GROUND);
        HordeMobRegistry.registerCustomMob("webspinner_spider", STNSpiderEntities.WEBSPINNER_SPIDER, 8, 25, MobCategory.GROUND);

        // Mid game spiders
        HordeMobRegistry.registerCustomMob("venom_spider", STNSpiderEntities.VENOM_SPIDER, 8, 30, MobCategory.GROUND);

        // Late game spiders
        HordeMobRegistry.registerCustomMob("burden_spider", STNSpiderEntities.BURDEN_SPIDER, 5, 50, MobCategory.GROUND);

        // Mini-boss tier
        HordeMobRegistry.registerCustomMob("broodmother_spider", STNSpiderEntities.BROODMOTHER_SPIDER, 3, 75, MobCategory.GROUND);

        LOGGER.info("Registered 6 spider variants with horde spawn system");
    }
}
