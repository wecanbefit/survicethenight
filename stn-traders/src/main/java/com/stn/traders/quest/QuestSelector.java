package com.stn.traders.quest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Selects quests using weighted random selection based on gamestage.
 */
public class QuestSelector {
    private static final Random random = new Random();

    /**
     * Get a list of available quests for the given gamestage.
     */
    public static List<Quest> getAvailableQuests(int gamestage) {
        return QuestConfigManager.getQuests().stream()
            .filter(q -> q.isAvailableAt(gamestage))
            .toList();
    }

    /**
     * Select a random quest using weighted selection.
     *
     * @param gamestage current world gamestage
     * @param excludeIds quest IDs to exclude (e.g., player's active quests)
     * @return selected quest, or null if none available
     */
    public static Quest selectRandom(int gamestage, List<String> excludeIds) {
        List<Quest> eligible = getAvailableQuests(gamestage).stream()
            .filter(q -> !excludeIds.contains(q.id()))
            .toList();

        if (eligible.isEmpty()) {
            return null;
        }

        // Calculate total weight
        double totalWeight = eligible.stream()
            .mapToDouble(Quest::weight)
            .sum();

        // Weighted random selection
        double roll = random.nextDouble() * totalWeight;
        double cumulative = 0;

        for (Quest quest : eligible) {
            cumulative += quest.weight();
            if (roll < cumulative) {
                return quest;
            }
        }

        // Fallback to last quest (shouldn't happen)
        return eligible.get(eligible.size() - 1);
    }

    /**
     * Select multiple unique quests.
     *
     * @param count number of quests to select
     * @param gamestage current world gamestage
     * @param excludeIds quest IDs to exclude
     * @return list of selected quests
     */
    public static List<Quest> selectMultiple(int count, int gamestage, List<String> excludeIds) {
        List<Quest> selected = new ArrayList<>();
        List<String> allExcluded = new ArrayList<>(excludeIds);

        for (int i = 0; i < count; i++) {
            Quest quest = selectRandom(gamestage, allExcluded);
            if (quest == null) break;

            selected.add(quest);
            allExcluded.add(quest.id());
        }

        return selected;
    }
}
