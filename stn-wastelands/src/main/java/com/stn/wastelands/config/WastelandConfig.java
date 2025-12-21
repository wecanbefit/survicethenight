package com.stn.wastelands.config;

/**
 * Configuration for Wasteland features.
 * TODO: Integrate with a proper config system (JSON, TOML, etc.)
 */
public class WastelandConfig {

    /**
     * Enable village zombification (replaces villagers with zombies).
     */
    public static boolean enableVillageZombification = true;

    /**
     * Enable enhanced loot in wasteland towns.
     */
    public static boolean enableEnhancedLoot = true;

    /**
     * Minimum game stage required for bonus loot to appear.
     */
    public static int minimumGamestageForBonusLoot = 10;

    /**
     * Maximum loot quality multiplier (based on game stage + distance).
     */
    public static float maxLootQualityMultiplier = 2.5f;

    /**
     * Distance from spawn (in blocks) where maximum distance bonus is reached.
     */
    public static double maxDistanceForBonus = 10000.0;

    /**
     * Chance for a villager to become a zombie villager instead of regular zombie (0.0 to 1.0).
     */
    public static float zombieVillagerChance = 0.6f;

    /**
     * Enable enhanced enchantments on loot items.
     */
    public static boolean enableEnhancedEnchantments = true;

    /**
     * Chance for finding rare loot (enchanted golden apples, netherite, etc.).
     */
    public static float rareLootChance = 0.15f;

    /**
     * Chance to spawn extra zombies when converting a villager (0.0 to 1.0).
     */
    public static float extraZombieSpawnChance = 0.3f;

    /**
     * Minimum number of extra zombies to spawn.
     */
    public static int minExtraZombies = 1;

    /**
     * Maximum number of extra zombies to spawn.
     */
    public static int maxExtraZombies = 2;
}
