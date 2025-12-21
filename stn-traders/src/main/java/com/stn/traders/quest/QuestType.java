package com.stn.traders.quest;

/**
 * Types of quests available.
 */
public enum QuestType {
    KILL("Kill"),      // Kill X entities
    GATHER("Gather"),  // Collect X items (consume on turn-in)
    FETCH("Fetch");    // Bring X items (keep after turn-in check)

    private final String displayName;

    QuestType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
