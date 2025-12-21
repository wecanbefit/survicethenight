package com.stn.traders.client;

import com.stn.traders.network.QuestSyncPayload;
import com.stn.traders.quest.Quest;
import com.stn.traders.quest.QuestConfigManager;
import com.stn.traders.quest.QuestInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side storage for quest data received from the server.
 */
public class ClientQuestData {
    private static List<String> offeredQuestIds = new ArrayList<>();
    private static List<QuestSyncPayload.ActiveQuestEntry> activeQuestEntries = new ArrayList<>();

    /**
     * Update the cached quest data from a sync payload.
     */
    public static void update(QuestSyncPayload payload) {
        offeredQuestIds = new ArrayList<>(payload.offeredQuestIds());
        activeQuestEntries = new ArrayList<>(payload.activeQuests());
    }

    /**
     * Get offered quests by looking up IDs in the config.
     */
    public static List<Quest> getOfferedQuests() {
        List<Quest> quests = new ArrayList<>();
        for (String id : offeredQuestIds) {
            Quest quest = QuestConfigManager.getQuest(id);
            if (quest != null) {
                quests.add(quest);
            }
        }
        return quests;
    }

    /**
     * Get active quest instances from the synced data.
     */
    public static List<QuestInstance> getActiveQuests() {
        List<QuestInstance> instances = new ArrayList<>();
        for (QuestSyncPayload.ActiveQuestEntry entry : activeQuestEntries) {
            instances.add(new QuestInstance(
                entry.questId(),
                entry.progress(),
                entry.targetCount(),
                entry.gamestage(),
                entry.canTurnIn()
            ));
        }
        return instances;
    }

    /**
     * Clear the cached data.
     */
    public static void clear() {
        offeredQuestIds.clear();
        activeQuestEntries.clear();
    }
}
