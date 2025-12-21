package com.stn.wastelands.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages loading and saving of Wasteland configuration to JSON file.
 */
public class WastelandConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("STN-Wastelands");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("stn-wastelands.json");

    /**
     * Loads configuration from file, or creates default if not exists.
     */
    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                JsonObject config = GSON.fromJson(json, JsonObject.class);

                // Load village zombification settings
                if (config.has("enableVillageZombification")) {
                    WastelandConfig.enableVillageZombification = config.get("enableVillageZombification").getAsBoolean();
                }
                if (config.has("zombieVillagerChance")) {
                    WastelandConfig.zombieVillagerChance = config.get("zombieVillagerChance").getAsFloat();
                }

                // Load loot settings
                if (config.has("enableEnhancedLoot")) {
                    WastelandConfig.enableEnhancedLoot = config.get("enableEnhancedLoot").getAsBoolean();
                }
                if (config.has("minimumGamestageForBonusLoot")) {
                    WastelandConfig.minimumGamestageForBonusLoot = config.get("minimumGamestageForBonusLoot").getAsInt();
                }
                if (config.has("maxLootQualityMultiplier")) {
                    WastelandConfig.maxLootQualityMultiplier = config.get("maxLootQualityMultiplier").getAsFloat();
                }
                if (config.has("maxDistanceForBonus")) {
                    WastelandConfig.maxDistanceForBonus = config.get("maxDistanceForBonus").getAsDouble();
                }
                if (config.has("enableEnhancedEnchantments")) {
                    WastelandConfig.enableEnhancedEnchantments = config.get("enableEnhancedEnchantments").getAsBoolean();
                }
                if (config.has("rareLootChance")) {
                    WastelandConfig.rareLootChance = config.get("rareLootChance").getAsFloat();
                }
                if (config.has("extraZombieSpawnChance")) {
                    WastelandConfig.extraZombieSpawnChance = config.get("extraZombieSpawnChance").getAsFloat();
                }
                if (config.has("minExtraZombies")) {
                    WastelandConfig.minExtraZombies = config.get("minExtraZombies").getAsInt();
                }
                if (config.has("maxExtraZombies")) {
                    WastelandConfig.maxExtraZombies = config.get("maxExtraZombies").getAsInt();
                }

                LOGGER.info("Configuration loaded from {}", CONFIG_PATH);
            } catch (IOException e) {
                LOGGER.error("Failed to load config file, using defaults", e);
                save(); // Save defaults
            } catch (Exception e) {
                LOGGER.error("Failed to parse config file, using defaults", e);
                save(); // Save defaults
            }
        } else {
            LOGGER.info("Config file not found, creating default configuration");
            save();
        }
    }

    /**
     * Saves current configuration to file.
     */
    public static void save() {
        JsonObject config = new JsonObject();

        // Save village zombification settings
        config.addProperty("enableVillageZombification", WastelandConfig.enableVillageZombification);
        config.addProperty("zombieVillagerChance", WastelandConfig.zombieVillagerChance);

        // Save loot settings
        config.addProperty("enableEnhancedLoot", WastelandConfig.enableEnhancedLoot);
        config.addProperty("minimumGamestageForBonusLoot", WastelandConfig.minimumGamestageForBonusLoot);
        config.addProperty("maxLootQualityMultiplier", WastelandConfig.maxLootQualityMultiplier);
        config.addProperty("maxDistanceForBonus", WastelandConfig.maxDistanceForBonus);
        config.addProperty("enableEnhancedEnchantments", WastelandConfig.enableEnhancedEnchantments);
        config.addProperty("rareLootChance", WastelandConfig.rareLootChance);
        config.addProperty("extraZombieSpawnChance", WastelandConfig.extraZombieSpawnChance);
        config.addProperty("minExtraZombies", WastelandConfig.minExtraZombies);
        config.addProperty("maxExtraZombies", WastelandConfig.maxExtraZombies);

        try {
            String json = GSON.toJson(config);
            Files.writeString(CONFIG_PATH, json);
            LOGGER.info("Configuration saved to {}", CONFIG_PATH);
        } catch (IOException e) {
            LOGGER.error("Failed to save config file", e);
        }
    }

    /**
     * Gets the path to the config file.
     */
    public static Path getConfigPath() {
        return CONFIG_PATH;
    }
}
