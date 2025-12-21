package com.stn.wastelands.loot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages loading and saving of loot configuration.
 * Config file is located at config/stn-wastelands/loot.json
 */
public class LootConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("STN-Wastelands");
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final String CONFIG_DIR = "stn-wastelands";
    private static final String CONFIG_FILE = "loot.json";

    private static LootConfig config;

    /**
     * Load configuration from file, or create default if not exists.
     */
    public static void load() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_DIR);
        Path configFile = configDir.resolve(CONFIG_FILE);

        try {
            // Create config directory if needed
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            if (Files.exists(configFile)) {
                // Load existing config
                String json = Files.readString(configFile);
                config = GSON.fromJson(json, LootConfig.class);
                LOGGER.info("Loaded loot config from {}", configFile);

                // Validate and fill in missing items from default
                LootConfig defaultConfig = LootConfig.createDefault();
                boolean needsSave = false;

                for (var entry : defaultConfig.items.entrySet()) {
                    if (!config.items.containsKey(entry.getKey())) {
                        config.items.put(entry.getKey(), entry.getValue());
                        needsSave = true;
                        LOGGER.info("Added missing item to config: {}", entry.getKey());
                    }
                }

                if (needsSave) {
                    save();
                }
            } else {
                // Create default config
                config = LootConfig.createDefault();
                save();
                LOGGER.info("Created default loot config at {}", configFile);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load loot config, using defaults", e);
            config = LootConfig.createDefault();
        }
    }

    /**
     * Save current configuration to file.
     */
    public static void save() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_DIR);
        Path configFile = configDir.resolve(CONFIG_FILE);

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            String json = GSON.toJson(config);
            Files.writeString(configFile, json);
            LOGGER.info("Saved loot config to {}", configFile);
        } catch (IOException e) {
            LOGGER.error("Failed to save loot config", e);
        }
    }

    /**
     * Reload configuration from file.
     */
    public static void reload() {
        load();
        LOGGER.info("Reloaded loot configuration");
    }

    /**
     * Get the current loot configuration.
     */
    public static LootConfig getConfig() {
        if (config == null) {
            load();
        }
        return config;
    }
}
