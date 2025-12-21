package com.stn.traders.quest;

import net.minecraft.util.Identifier;

/**
 * Definition of a quest loaded from config.
 */
public record Quest(
    String id,
    QuestType type,
    Identifier target,      // Entity type ID or Item ID
    int baseCount,          // Base amount required (scales with gamestage)
    double weight,          // Selection weight (0.1 - 10.0)
    int minGamestage,       // Minimum gamestage to appear
    int maxGamestage,       // Maximum gamestage (-1 for no limit)
    String description,     // Display text (%count% placeholder)
    QuestReward reward      // Base rewards
) {
    /**
     * Get the scaled count based on gamestage.
     * Formula: baseCount + (gamestage / 10) * baseCount * 0.5
     */
    public int getScaledCount(int gamestage) {
        return baseCount + (int) ((gamestage / 10.0) * baseCount * 0.5);
    }

    /**
     * Get the formatted description with count placeholder replaced.
     */
    public String getFormattedDescription(int gamestage) {
        return description.replace("%count%", String.valueOf(getScaledCount(gamestage)));
    }

    /**
     * Check if this quest is available at the given gamestage.
     */
    public boolean isAvailableAt(int gamestage) {
        if (gamestage < minGamestage) return false;
        if (maxGamestage >= 0 && gamestage > maxGamestage) return false;
        return true;
    }
}
