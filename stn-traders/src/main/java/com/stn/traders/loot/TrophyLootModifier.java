package com.stn.traders.loot;

import com.stn.traders.STNTraders;
import com.stn.traders.registry.STNTraderItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.KilledByPlayerLootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Modifies entity loot tables to add trophy drops.
 */
public class TrophyLootModifier {

    // Map of entity loot table ID -> (trophy item, drop chance)
    private static final Map<String, TrophyDrop> TROPHY_DROPS = new HashMap<>();

    private record TrophyDrop(Item item, float chance) {}

    static {
        // Zombie trophies
        TROPHY_DROPS.put("stn_zombies:entities/bruiser_zombie", new TrophyDrop(STNTraderItems.BRUISER_FANG, 0.10f));
        TROPHY_DROPS.put("stn_zombies:entities/howler_zombie", new TrophyDrop(STNTraderItems.HOWLER_VOCAL_CORD, 0.08f));
        TROPHY_DROPS.put("stn_zombies:entities/sprinter_zombie", new TrophyDrop(STNTraderItems.SPRINTER_HEART, 0.12f));
        TROPHY_DROPS.put("stn_zombies:entities/spitter_zombie", new TrophyDrop(STNTraderItems.SPITTER_GLAND, 0.10f));
        TROPHY_DROPS.put("stn_zombies:entities/plague_zombie", new TrophyDrop(STNTraderItems.PLAGUE_SAMPLE, 0.05f));
        TROPHY_DROPS.put("stn_zombies:entities/electric_zombie", new TrophyDrop(STNTraderItems.CHARGED_CORE, 0.08f));
        TROPHY_DROPS.put("stn_zombies:entities/lumberjack_zombie", new TrophyDrop(STNTraderItems.LUMBERJACK_AXE_HEAD, 0.15f));
        TROPHY_DROPS.put("stn_zombies:entities/leech_zombie", new TrophyDrop(STNTraderItems.LEECH_FANG, 0.10f));
        TROPHY_DROPS.put("stn_zombies:entities/shielded_zombie", new TrophyDrop(STNTraderItems.SHATTERED_SHIELD, 0.12f));

        // Skeleton trophies
        TROPHY_DROPS.put("stn_skeletons:entities/marksman_skeleton", new TrophyDrop(STNTraderItems.MARKSMAN_EYE, 0.08f));
        TROPHY_DROPS.put("stn_skeletons:entities/flame_archer_skeleton", new TrophyDrop(STNTraderItems.FLAME_ARROW, 0.10f));
        TROPHY_DROPS.put("stn_skeletons:entities/reaper_skeleton", new TrophyDrop(STNTraderItems.REAPER_BONE, 0.06f));
        TROPHY_DROPS.put("stn_skeletons:entities/vanguard_skeleton", new TrophyDrop(STNTraderItems.VANGUARD_PLATE, 0.10f));

        // Spider trophies
        TROPHY_DROPS.put("stn_spiders:entities/broodmother_spider", new TrophyDrop(STNTraderItems.BROODMOTHER_EGG, 0.05f));
        TROPHY_DROPS.put("stn_spiders:entities/venom_spider", new TrophyDrop(STNTraderItems.VENOM_SAC, 0.12f));
        TROPHY_DROPS.put("stn_spiders:entities/leaper_spider", new TrophyDrop(STNTraderItems.LEAPER_LEG, 0.10f));
        TROPHY_DROPS.put("stn_spiders:entities/webspinner_spider", new TrophyDrop(STNTraderItems.WEB_GLAND, 0.10f));
    }

    /**
     * Register loot table modifications.
     */
    public static void register() {
        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            // Only modify built-in tables, not data packs
            if (!source.isBuiltin()) return;

            String tableId = key.getValue().toString();
            TrophyDrop drop = TROPHY_DROPS.get(tableId);

            if (drop != null) {
                // Add trophy drop pool
                LootPool.Builder pool = LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .with(ItemEntry.builder(drop.item()))
                    .conditionally(KilledByPlayerLootCondition.builder())
                    .conditionally(RandomChanceLootCondition.builder(drop.chance()));

                tableBuilder.pool(pool);
                STNTraders.LOGGER.debug("Added trophy drop to {}", tableId);
            }
        });

        STNTraders.LOGGER.info("Registered trophy loot modifiers for {} mob types", TROPHY_DROPS.size());
    }
}
