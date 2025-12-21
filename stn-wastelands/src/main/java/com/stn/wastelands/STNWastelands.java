package com.stn.wastelands;

import com.stn.core.STNCore;
import com.stn.wastelands.config.WastelandConfig;
import com.stn.wastelands.config.WastelandConfigManager;
import com.stn.wastelands.loot.WastelandLootModifier;
import com.stn.wastelands.village.VillageZombifier;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Survive The Night - Wastelands
 * Turns all villages into zombie-infested wasteland towns with enhanced loot.
 * Loot quality scales with game stage and distance from spawn.
 */
public class STNWastelands implements ModInitializer {
    public static final String MOD_ID = "stn_wastelands";
    public static final Logger LOGGER = LoggerFactory.getLogger("STN-Wastelands");

    @Override
    public void onInitialize() {
        LOGGER.info("Survive The Night - Wastelands initializing...");

        // Load configuration
        WastelandConfigManager.load();

        // Initialize village zombification system
        if (WastelandConfig.enableVillageZombification) {
            VillageZombifier.init();
            LOGGER.info("Village zombification enabled");
        }

        // Initialize enhanced loot system
        if (WastelandConfig.enableEnhancedLoot) {
            WastelandLootModifier.init();
            LOGGER.info("Enhanced wasteland loot enabled");
        }

        LOGGER.info("Survive The Night - Wastelands initialized!");
    }
}
