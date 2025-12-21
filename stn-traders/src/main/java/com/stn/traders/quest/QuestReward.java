package com.stn.traders.quest;

import net.minecraft.util.Identifier;

/**
 * Reward options for a quest.
 * Player chooses ONE of these on completion.
 */
public record QuestReward(
    int emeralds,           // Emerald reward amount
    int experience,         // XP reward amount
    Identifier item,        // Item reward ID (nullable)
    int itemCount,          // Number of items
    boolean itemEnchanted   // Whether item gets gamestage-scaled enchants
) {
    /**
     * Get scaled emerald reward based on gamestage.
     * Formula: base + (gamestage / 5)
     */
    public int getScaledEmeralds(int gamestage) {
        return emeralds + (gamestage / 5);
    }

    /**
     * Get scaled experience reward based on gamestage.
     * Formula: base * (1 + gamestage / 50)
     */
    public int getScaledExperience(int gamestage) {
        return (int) (experience * (1.0 + gamestage / 50.0));
    }

    /**
     * Get enchantment level for item rewards based on gamestage.
     */
    public int getEnchantLevel(int gamestage) {
        if (!itemEnchanted) return 0;
        // Scale from 1-30 based on gamestage
        return Math.min(30, 5 + (gamestage / 4));
    }
}
