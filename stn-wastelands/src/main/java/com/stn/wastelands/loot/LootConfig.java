package com.stn.wastelands.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for gamestage-based loot tables.
 * Each tier (0-9) corresponds to gamestage ranges: 0-9, 10-19, 20-29, etc.
 */
public class LootConfig {

    // Item pools - which items can appear at each tier
    public Map<String, ItemConfig> items = new HashMap<>();

    // Enchantment config per tier
    public EnchantConfig enchantments = new EnchantConfig();

    // Material pool rolls per tier
    public int[] materialPoolRolls = {1, 1, 1, 2, 2, 2, 2, 3, 3, 3};

    // Gear pool rolls per tier
    public int[] gearPoolRolls = {0, 0, 1, 1, 1, 1, 2, 2, 2, 2};

    // Book pool rolls per tier
    public int[] bookPoolRolls = {0, 0, 0, 1, 1, 1, 1, 1, 1, 1};

    /**
     * Configuration for a single item type.
     */
    public static class ItemConfig {
        public String itemId;           // e.g., "minecraft:iron_nugget"
        public int minTier = 0;         // First tier this item appears (0-9)
        public int maxTier = 9;         // Last tier this item appears (0-9)
        public int weight = 10;         // Loot weight (higher = more common)
        public boolean canEnchant = false;  // Whether this item can be enchanted
        public String category;         // "material", "gear", "food", "book"

        // Quantity per tier [tier0, tier1, ... tier9]
        public int[] minQuantity = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        public int[] maxQuantity = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1};

        public ItemConfig() {}

        public ItemConfig(String itemId, String category, int weight, int minTier, int maxTier,
                         int[] minQty, int[] maxQty, boolean canEnchant) {
            this.itemId = itemId;
            this.category = category;
            this.weight = weight;
            this.minTier = minTier;
            this.maxTier = maxTier;
            this.minQuantity = minQty;
            this.maxQuantity = maxQty;
            this.canEnchant = canEnchant;
        }
    }

    /**
     * Enchantment configuration per tier.
     */
    public static class EnchantConfig {
        // Chance to get an enchantment at each tier (0.0 to 1.0)
        public double[] enchantChance = {0.1, 0.15, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};

        // Minimum enchantment level multiplier per tier (0.0 to 1.0, applied to max level)
        public double[] minLevelMultiplier = {0.0, 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8};

        // Maximum enchantment level multiplier per tier (0.0 to 1.0, applied to max level)
        public double[] maxLevelMultiplier = {0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 1.0, 1.0};

        // Chance for a second enchantment per tier
        public double[] multiEnchantChance = {0.0, 0.0, 0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.4, 0.5};
    }

    /**
     * Creates default configuration with balanced loot tables.
     */
    public static LootConfig createDefault() {
        LootConfig config = new LootConfig();

        // ===== MATERIALS =====

        // Iron Nugget - early game, common
        config.items.put("iron_nugget", new ItemConfig(
            "minecraft:iron_nugget", "material", 15, 0, 4,
            new int[]{1, 2, 3, 4, 5, 0, 0, 0, 0, 0},
            new int[]{3, 5, 7, 9, 10, 0, 0, 0, 0, 0},
            false
        ));

        // Gold Nugget - early game
        config.items.put("gold_nugget", new ItemConfig(
            "minecraft:gold_nugget", "material", 12, 0, 4,
            new int[]{1, 1, 2, 3, 4, 0, 0, 0, 0, 0},
            new int[]{2, 3, 5, 6, 8, 0, 0, 0, 0, 0},
            false
        ));

        // Iron Ingot - progresses through tiers
        config.items.put("iron_ingot", new ItemConfig(
            "minecraft:iron_ingot", "material", 12, 1, 9,
            new int[]{0, 1, 2, 2, 3, 3, 4, 5, 6, 8},
            new int[]{0, 3, 4, 5, 6, 8, 10, 12, 15, 20},
            false
        ));

        // Gold Ingot
        config.items.put("gold_ingot", new ItemConfig(
            "minecraft:gold_ingot", "material", 10, 1, 9,
            new int[]{0, 1, 1, 2, 2, 3, 3, 4, 5, 6},
            new int[]{0, 2, 3, 4, 5, 6, 8, 10, 12, 15},
            false
        ));

        // Copper Ingot
        config.items.put("copper_ingot", new ItemConfig(
            "minecraft:copper_ingot", "material", 14, 0, 6,
            new int[]{2, 3, 4, 5, 6, 8, 10, 0, 0, 0},
            new int[]{5, 8, 10, 12, 15, 20, 25, 0, 0, 0},
            false
        ));

        // Coal
        config.items.put("coal", new ItemConfig(
            "minecraft:coal", "material", 15, 0, 5,
            new int[]{2, 4, 6, 8, 10, 12, 0, 0, 0, 0},
            new int[]{6, 10, 15, 20, 25, 32, 0, 0, 0, 0},
            false
        ));

        // Redstone
        config.items.put("redstone", new ItemConfig(
            "minecraft:redstone", "material", 10, 2, 7,
            new int[]{0, 0, 2, 4, 6, 8, 10, 12, 0, 0},
            new int[]{0, 0, 5, 8, 12, 16, 20, 25, 0, 0},
            false
        ));

        // Lapis
        config.items.put("lapis_lazuli", new ItemConfig(
            "minecraft:lapis_lazuli", "material", 8, 2, 8,
            new int[]{0, 0, 1, 2, 3, 4, 5, 6, 8, 0},
            new int[]{0, 0, 3, 5, 8, 10, 12, 15, 20, 0},
            false
        ));

        // Diamond - mid to late game
        config.items.put("diamond", new ItemConfig(
            "minecraft:diamond", "material", 5, 4, 9,
            new int[]{0, 0, 0, 0, 1, 1, 1, 2, 2, 3},
            new int[]{0, 0, 0, 0, 1, 2, 3, 4, 5, 6},
            false
        ));

        // Emerald
        config.items.put("emerald", new ItemConfig(
            "minecraft:emerald", "material", 6, 3, 9,
            new int[]{0, 0, 0, 1, 1, 2, 2, 3, 4, 5},
            new int[]{0, 0, 0, 2, 3, 4, 5, 6, 8, 10},
            false
        ));

        // Netherite Scrap - late game only
        config.items.put("netherite_scrap", new ItemConfig(
            "minecraft:netherite_scrap", "material", 2, 7, 9,
            new int[]{0, 0, 0, 0, 0, 0, 0, 1, 1, 1},
            new int[]{0, 0, 0, 0, 0, 0, 0, 1, 2, 3},
            false
        ));

        // Ancient Debris - very rare late game
        config.items.put("ancient_debris", new ItemConfig(
            "minecraft:ancient_debris", "material", 1, 8, 9,
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 2},
            false
        ));

        // ===== FOOD =====

        // Bread - common early
        config.items.put("bread", new ItemConfig(
            "minecraft:bread", "food", 12, 0, 5,
            new int[]{1, 2, 3, 4, 5, 6, 0, 0, 0, 0},
            new int[]{3, 5, 7, 9, 12, 15, 0, 0, 0, 0},
            false
        ));

        // Cooked Beef
        config.items.put("cooked_beef", new ItemConfig(
            "minecraft:cooked_beef", "food", 10, 1, 7,
            new int[]{0, 1, 2, 3, 4, 5, 6, 8, 0, 0},
            new int[]{0, 3, 5, 7, 9, 12, 15, 20, 0, 0},
            false
        ));

        // Golden Apple
        config.items.put("golden_apple", new ItemConfig(
            "minecraft:golden_apple", "food", 4, 3, 9,
            new int[]{0, 0, 0, 1, 1, 1, 1, 2, 2, 3},
            new int[]{0, 0, 0, 1, 2, 2, 3, 3, 4, 5},
            false
        ));

        // Enchanted Golden Apple - very rare
        config.items.put("enchanted_golden_apple", new ItemConfig(
            "minecraft:enchanted_golden_apple", "food", 1, 6, 9,
            new int[]{0, 0, 0, 0, 0, 0, 1, 1, 1, 1},
            new int[]{0, 0, 0, 0, 0, 0, 1, 1, 1, 2},
            false
        ));

        // Golden Carrot
        config.items.put("golden_carrot", new ItemConfig(
            "minecraft:golden_carrot", "food", 6, 2, 8,
            new int[]{0, 0, 1, 2, 3, 4, 5, 6, 8, 0},
            new int[]{0, 0, 3, 5, 7, 9, 12, 15, 20, 0},
            false
        ));

        // ===== WEAPONS =====

        // Stone Sword
        config.items.put("stone_sword", new ItemConfig(
            "minecraft:stone_sword", "gear", 8, 0, 2,
            new int[]{1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            new int[]{1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
            true
        ));

        // Iron Sword
        config.items.put("iron_sword", new ItemConfig(
            "minecraft:iron_sword", "gear", 10, 2, 6,
            new int[]{0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            new int[]{0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            true
        ));

        // Diamond Sword
        config.items.put("diamond_sword", new ItemConfig(
            "minecraft:diamond_sword", "gear", 5, 5, 9,
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            true
        ));

        // Netherite Sword
        config.items.put("netherite_sword", new ItemConfig(
            "minecraft:netherite_sword", "gear", 1, 8, 9,
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
            true
        ));

        // Iron Axe
        config.items.put("iron_axe", new ItemConfig(
            "minecraft:iron_axe", "gear", 8, 2, 6,
            new int[]{0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            new int[]{0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            true
        ));

        // Diamond Axe
        config.items.put("diamond_axe", new ItemConfig(
            "minecraft:diamond_axe", "gear", 4, 5, 9,
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            true
        ));

        // Bow
        config.items.put("bow", new ItemConfig(
            "minecraft:bow", "gear", 8, 1, 7,
            new int[]{0, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            new int[]{0, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            true
        ));

        // Crossbow
        config.items.put("crossbow", new ItemConfig(
            "minecraft:crossbow", "gear", 6, 3, 9,
            new int[]{0, 0, 0, 1, 1, 1, 1, 1, 1, 1},
            new int[]{0, 0, 0, 1, 1, 1, 1, 1, 1, 1},
            true
        ));

        // ===== TOOLS =====

        // Iron Pickaxe
        config.items.put("iron_pickaxe", new ItemConfig(
            "minecraft:iron_pickaxe", "gear", 10, 2, 6,
            new int[]{0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            new int[]{0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            true
        ));

        // Diamond Pickaxe
        config.items.put("diamond_pickaxe", new ItemConfig(
            "minecraft:diamond_pickaxe", "gear", 4, 5, 9,
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            true
        ));

        // Iron Shovel
        config.items.put("iron_shovel", new ItemConfig(
            "minecraft:iron_shovel", "gear", 8, 1, 5,
            new int[]{0, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            new int[]{0, 1, 1, 1, 1, 1, 0, 0, 0, 0},
            true
        ));

        // Diamond Shovel
        config.items.put("diamond_shovel", new ItemConfig(
            "minecraft:diamond_shovel", "gear", 3, 5, 9,
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            true
        ));

        // ===== ARMOR =====

        // Chainmail (mid-tier armor)
        config.items.put("chainmail_helmet", new ItemConfig(
            "minecraft:chainmail_helmet", "gear", 6, 1, 4,
            new int[]{0, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            new int[]{0, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            true
        ));
        config.items.put("chainmail_chestplate", new ItemConfig(
            "minecraft:chainmail_chestplate", "gear", 5, 1, 4,
            new int[]{0, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            new int[]{0, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            true
        ));
        config.items.put("chainmail_leggings", new ItemConfig(
            "minecraft:chainmail_leggings", "gear", 5, 1, 4,
            new int[]{0, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            new int[]{0, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            true
        ));
        config.items.put("chainmail_boots", new ItemConfig(
            "minecraft:chainmail_boots", "gear", 6, 1, 4,
            new int[]{0, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            new int[]{0, 1, 1, 1, 1, 0, 0, 0, 0, 0},
            true
        ));

        // Iron Armor
        config.items.put("iron_helmet", new ItemConfig(
            "minecraft:iron_helmet", "gear", 8, 2, 6,
            new int[]{0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            new int[]{0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            true
        ));
        config.items.put("iron_chestplate", new ItemConfig(
            "minecraft:iron_chestplate", "gear", 6, 2, 6,
            new int[]{0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            new int[]{0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            true
        ));
        config.items.put("iron_leggings", new ItemConfig(
            "minecraft:iron_leggings", "gear", 6, 2, 6,
            new int[]{0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            new int[]{0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            true
        ));
        config.items.put("iron_boots", new ItemConfig(
            "minecraft:iron_boots", "gear", 8, 2, 6,
            new int[]{0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            new int[]{0, 0, 1, 1, 1, 1, 1, 0, 0, 0},
            true
        ));

        // Diamond Armor
        config.items.put("diamond_helmet", new ItemConfig(
            "minecraft:diamond_helmet", "gear", 4, 5, 9,
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            true
        ));
        config.items.put("diamond_chestplate", new ItemConfig(
            "minecraft:diamond_chestplate", "gear", 3, 5, 9,
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            true
        ));
        config.items.put("diamond_leggings", new ItemConfig(
            "minecraft:diamond_leggings", "gear", 3, 5, 9,
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            true
        ));
        config.items.put("diamond_boots", new ItemConfig(
            "minecraft:diamond_boots", "gear", 4, 5, 9,
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            new int[]{0, 0, 0, 0, 0, 1, 1, 1, 1, 1},
            true
        ));

        // Netherite Armor - endgame
        config.items.put("netherite_helmet", new ItemConfig(
            "minecraft:netherite_helmet", "gear", 1, 8, 9,
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
            true
        ));
        config.items.put("netherite_chestplate", new ItemConfig(
            "minecraft:netherite_chestplate", "gear", 1, 9, 9,
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            true
        ));
        config.items.put("netherite_leggings", new ItemConfig(
            "minecraft:netherite_leggings", "gear", 1, 9, 9,
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            true
        ));
        config.items.put("netherite_boots", new ItemConfig(
            "minecraft:netherite_boots", "gear", 1, 8, 9,
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
            true
        ));

        // Shield
        config.items.put("shield", new ItemConfig(
            "minecraft:shield", "gear", 6, 1, 7,
            new int[]{0, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            new int[]{0, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            false
        ));

        // ===== BOOKS =====

        config.items.put("enchanted_book", new ItemConfig(
            "minecraft:enchanted_book", "book", 10, 0, 9,
            new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            new int[]{1, 1, 1, 1, 1, 2, 2, 2, 3, 3},
            true  // Special handling for books
        ));

        // ===== MISC =====

        // Arrows
        config.items.put("arrow", new ItemConfig(
            "minecraft:arrow", "material", 10, 0, 6,
            new int[]{4, 8, 12, 16, 20, 24, 32, 0, 0, 0},
            new int[]{12, 20, 28, 36, 44, 52, 64, 0, 0, 0},
            false
        ));

        // Spectral Arrows
        config.items.put("spectral_arrow", new ItemConfig(
            "minecraft:spectral_arrow", "material", 5, 3, 9,
            new int[]{0, 0, 0, 2, 4, 6, 8, 10, 12, 16},
            new int[]{0, 0, 0, 6, 10, 14, 18, 22, 28, 32},
            false
        ));

        // Ender Pearl
        config.items.put("ender_pearl", new ItemConfig(
            "minecraft:ender_pearl", "material", 4, 4, 9,
            new int[]{0, 0, 0, 0, 1, 1, 2, 2, 3, 4},
            new int[]{0, 0, 0, 0, 2, 3, 4, 5, 6, 8},
            false
        ));

        // Experience Bottle
        config.items.put("experience_bottle", new ItemConfig(
            "minecraft:experience_bottle", "material", 5, 3, 9,
            new int[]{0, 0, 0, 1, 2, 3, 4, 5, 6, 8},
            new int[]{0, 0, 0, 3, 5, 7, 9, 12, 15, 20},
            false
        ));

        // Name Tag
        config.items.put("name_tag", new ItemConfig(
            "minecraft:name_tag", "material", 3, 2, 8,
            new int[]{0, 0, 1, 1, 1, 1, 2, 2, 2, 0},
            new int[]{0, 0, 1, 2, 2, 3, 3, 4, 4, 0},
            false
        ));

        // Saddle
        config.items.put("saddle", new ItemConfig(
            "minecraft:saddle", "material", 4, 2, 7,
            new int[]{0, 0, 1, 1, 1, 1, 1, 1, 0, 0},
            new int[]{0, 0, 1, 1, 1, 1, 1, 1, 0, 0},
            false
        ));

        // Totem of Undying - very rare late game
        config.items.put("totem_of_undying", new ItemConfig(
            "minecraft:totem_of_undying", "material", 1, 8, 9,
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
            new int[]{0, 0, 0, 0, 0, 0, 0, 0, 1, 1},
            false
        ));

        return config;
    }

    /**
     * Get tier (0-9) from gamestage.
     */
    public static int getTierFromGamestage(int gamestage) {
        return Math.min(9, Math.max(0, gamestage / 10));
    }
}
