package com.stn.wastelands.loot;

import com.stn.wastelands.config.WastelandConfig;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Modifies loot tables in wasteland towns based on game stage.
 * All loot is configurable via config/stn-wastelands/loot.json
 */
public class WastelandLootModifier {
    private static final Logger LOGGER = LoggerFactory.getLogger("STN-Wastelands");

    // Village chest loot table identifiers (Set for O(1) lookup)
    private static final Set<Identifier> VILLAGE_CHEST_LOOT_TABLES = Set.of(
            Identifier.ofVanilla("chests/village/village_armorer"),
            Identifier.ofVanilla("chests/village/village_butcher"),
            Identifier.ofVanilla("chests/village/village_cartographer"),
            Identifier.ofVanilla("chests/village/village_desert_house"),
            Identifier.ofVanilla("chests/village/village_fisher"),
            Identifier.ofVanilla("chests/village/village_fletcher"),
            Identifier.ofVanilla("chests/village/village_mason"),
            Identifier.ofVanilla("chests/village/village_plains_house"),
            Identifier.ofVanilla("chests/village/village_savanna_house"),
            Identifier.ofVanilla("chests/village/village_shepherd"),
            Identifier.ofVanilla("chests/village/village_snowy_house"),
            Identifier.ofVanilla("chests/village/village_taiga_house"),
            Identifier.ofVanilla("chests/village/village_tannery"),
            Identifier.ofVanilla("chests/village/village_temple"),
            Identifier.ofVanilla("chests/village/village_toolsmith"),
            Identifier.ofVanilla("chests/village/village_weaponsmith")
    );

    public static void init() {
        // Load loot configuration
        LootConfigManager.load();

        // Register our custom loot functions
        Registry.register(
                Registries.LOOT_FUNCTION_TYPE,
                Identifier.of("stn_wastelands", "gamestage_enchant"),
                GamestageEnchantLootFunction.TYPE
        );
        Registry.register(
                Registries.LOOT_FUNCTION_TYPE,
                Identifier.of("stn_wastelands", "gamestage_count"),
                GamestageCountLootFunction.TYPE
        );

        if (!WastelandConfig.enableEnhancedLoot) {
            LOGGER.info("Enhanced loot is disabled in config");
            return;
        }

        // Modify village chest loot tables
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            Identifier id = key.getValue();

            // Check if this is a village chest
            if (VILLAGE_CHEST_LOOT_TABLES.contains(id)) {
                LootConfig config = LootConfigManager.getConfig();

                // Add loot pools from config
                LootPool.Builder materialPool = createPoolFromConfig(config, "material");
                LootPool.Builder gearPool = createPoolFromConfig(config, "gear");
                LootPool.Builder foodPool = createPoolFromConfig(config, "food");
                LootPool.Builder bookPool = createPoolFromConfig(config, "book");

                if (materialPool != null) {
                    tableBuilder.pool(materialPool.rolls(UniformLootNumberProvider.create(1.0f, 3.0f)));
                }
                if (gearPool != null) {
                    tableBuilder.pool(gearPool.rolls(UniformLootNumberProvider.create(0.0f, 2.0f)));
                }
                if (foodPool != null) {
                    tableBuilder.pool(foodPool.rolls(UniformLootNumberProvider.create(0.0f, 2.0f)));
                }
                if (bookPool != null) {
                    tableBuilder.pool(bookPool.rolls(UniformLootNumberProvider.create(0.0f, 1.0f)));
                }

                LOGGER.debug("Enhanced loot table: {}", id);
            }
        });

        LOGGER.info("Wasteland loot modifier initialized with {} items configured",
                LootConfigManager.getConfig().items.size());
    }

    /**
     * Creates a loot pool from config items of the specified category.
     */
    private static LootPool.Builder createPoolFromConfig(LootConfig config, String category) {
        List<LootPoolEntry.Builder<?>> entries = new ArrayList<>();

        for (Map.Entry<String, LootConfig.ItemConfig> entry : config.items.entrySet()) {
            String itemKey = entry.getKey();
            LootConfig.ItemConfig itemConfig = entry.getValue();

            // Skip items not in this category
            if (!category.equals(itemConfig.category)) {
                continue;
            }

            // Get the item from registry
            Identifier itemId = Identifier.tryParse(itemConfig.itemId);
            if (itemId == null) {
                LOGGER.warn("Invalid item ID in loot config: {}", itemConfig.itemId);
                continue;
            }

            Item item = Registries.ITEM.get(itemId);
            if (item == null) {
                LOGGER.warn("Item not found in registry: {}", itemConfig.itemId);
                continue;
            }

            // Build the entry with weight and functions
            ItemEntry.Builder<?> itemEntry = ItemEntry.builder(item)
                    .weight(itemConfig.weight)
                    .apply(GamestageCountLootFunction.builder(itemKey));

            // Add enchantment function for enchantable items
            if (itemConfig.canEnchant) {
                itemEntry.apply(GamestageEnchantLootFunction.builder());
            }

            entries.add(itemEntry);
        }

        if (entries.isEmpty()) {
            return null;
        }

        LootPool.Builder pool = LootPool.builder();
        for (LootPoolEntry.Builder<?> entry : entries) {
            pool.with(entry);
        }

        return pool;
    }
}
