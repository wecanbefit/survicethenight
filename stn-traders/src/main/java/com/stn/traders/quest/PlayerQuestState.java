package com.stn.traders.quest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stn.traders.STNTraders;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Persistent state for player quest progress.
 * Tracks active quests (max 3 per player) and completed quest history.
 */
public class PlayerQuestState extends PersistentState {
    private static final String DATA_KEY = "stn_traders_player_quests";
    private static final int MAX_ACTIVE_QUESTS = 3;

    // Map of player UUID -> list of active quest instances
    private final Map<UUID, List<QuestInstance>> activeQuests = new ConcurrentHashMap<>();

    // Map of player UUID -> set of completed quest IDs (for daily limit tracking)
    private final Map<UUID, Set<String>> completedToday = new ConcurrentHashMap<>();

    // Last day tick (for resetting daily completions)
    private long lastDayReset = 0;

    // Serialization record for quest instances
    private record QuestEntry(
        String questId,
        String playerId,
        int targetCount,
        int gamestageAtAccept,
        long startTime,
        int progress
    ) {
        private static final Codec<QuestEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.STRING.fieldOf("questId").forGetter(QuestEntry::questId),
                Codec.STRING.fieldOf("playerId").forGetter(QuestEntry::playerId),
                Codec.INT.fieldOf("targetCount").forGetter(QuestEntry::targetCount),
                Codec.INT.fieldOf("gamestageAtAccept").forGetter(QuestEntry::gamestageAtAccept),
                Codec.LONG.fieldOf("startTime").forGetter(QuestEntry::startTime),
                Codec.INT.fieldOf("progress").forGetter(QuestEntry::progress)
            ).apply(instance, QuestEntry::new)
        );

        static QuestEntry fromInstance(QuestInstance instance) {
            return new QuestEntry(
                instance.getQuestId(),
                instance.getPlayerId().toString(),
                instance.getTargetCount(),
                instance.getGamestageAtAccept(),
                instance.getStartTime(),
                instance.getProgress()
            );
        }

        QuestInstance toInstance() {
            QuestInstance instance = new QuestInstance(
                questId,
                UUID.fromString(playerId),
                targetCount,
                gamestageAtAccept,
                startTime
            );
            instance.setProgress(progress);
            return instance;
        }
    }

    // Codec for the state
    private static final Codec<PlayerQuestState> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.list(QuestEntry.CODEC).fieldOf("quests").forGetter(state ->
                state.activeQuests.values().stream()
                    .flatMap(List::stream)
                    .map(QuestEntry::fromInstance)
                    .collect(Collectors.toList())
            ),
            Codec.LONG.fieldOf("lastDayReset").forGetter(state -> state.lastDayReset)
        ).apply(instance, (entries, lastDay) -> {
            PlayerQuestState state = new PlayerQuestState();
            state.lastDayReset = lastDay;
            for (QuestEntry entry : entries) {
                QuestInstance qi = entry.toInstance();
                state.activeQuests.computeIfAbsent(qi.getPlayerId(), k -> new ArrayList<>()).add(qi);
            }
            STNTraders.LOGGER.info("Loaded {} active quests for {} players",
                entries.size(), state.activeQuests.size());
            return state;
        })
    );

    private static final PersistentStateType<PlayerQuestState> TYPE = new PersistentStateType<>(
        DATA_KEY,
        PlayerQuestState::new,
        CODEC,
        null
    );

    public PlayerQuestState() {
    }

    /**
     * Get player's active quests.
     */
    public List<QuestInstance> getActiveQuests(UUID playerId) {
        return activeQuests.getOrDefault(playerId, new ArrayList<>());
    }

    /**
     * Check if player can accept more quests.
     */
    public boolean canAcceptQuest(UUID playerId) {
        return getActiveQuests(playerId).size() < MAX_ACTIVE_QUESTS;
    }

    /**
     * Accept a new quest for a player.
     *
     * @return the created QuestInstance, or null if can't accept
     */
    public QuestInstance acceptQuest(UUID playerId, Quest quest, int gamestage, long worldTime) {
        if (!canAcceptQuest(playerId)) {
            return null;
        }

        // Check if player already has this quest
        if (hasActiveQuest(playerId, quest.id())) {
            return null;
        }

        int targetCount = quest.getScaledCount(gamestage);
        QuestInstance instance = new QuestInstance(quest.id(), playerId, targetCount, gamestage, worldTime);

        activeQuests.computeIfAbsent(playerId, k -> new ArrayList<>()).add(instance);
        markDirty();

        STNTraders.LOGGER.debug("Player {} accepted quest {} (target: {})", playerId, quest.id(), targetCount);
        return instance;
    }

    /**
     * Check if player has an active quest with the given ID.
     */
    public boolean hasActiveQuest(UUID playerId, String questId) {
        return getActiveQuests(playerId).stream()
            .anyMatch(q -> q.getQuestId().equals(questId));
    }

    /**
     * Get a specific active quest instance.
     */
    public QuestInstance getActiveQuest(UUID playerId, String questId) {
        return getActiveQuests(playerId).stream()
            .filter(q -> q.getQuestId().equals(questId))
            .findFirst()
            .orElse(null);
    }

    /**
     * Add progress to a player's quest.
     */
    public void addProgress(UUID playerId, String questId, int amount) {
        QuestInstance instance = getActiveQuest(playerId, questId);
        if (instance != null) {
            instance.addProgress(amount);
            markDirty();
        }
    }

    /**
     * Complete a quest (remove from active list).
     *
     * @return true if quest was found and completed
     */
    public boolean completeQuest(UUID playerId, String questId) {
        List<QuestInstance> quests = activeQuests.get(playerId);
        if (quests == null) return false;

        boolean removed = quests.removeIf(q -> q.getQuestId().equals(questId));
        if (removed) {
            completedToday.computeIfAbsent(playerId, k -> new HashSet<>()).add(questId);
            markDirty();
            STNTraders.LOGGER.debug("Player {} completed quest {}", playerId, questId);
        }
        return removed;
    }

    /**
     * Fail all quests for a player (called on death).
     */
    public void failAllQuests(UUID playerId) {
        List<QuestInstance> quests = activeQuests.remove(playerId);
        if (quests != null && !quests.isEmpty()) {
            markDirty();
            STNTraders.LOGGER.debug("Player {} failed {} quests on death", playerId, quests.size());
        }
    }

    /**
     * Get list of active quest IDs for exclusion from selection.
     */
    public List<String> getActiveQuestIds(UUID playerId) {
        return getActiveQuests(playerId).stream()
            .map(QuestInstance::getQuestId)
            .toList();
    }

    /**
     * Check and reset daily completions if a new day.
     */
    public void checkDayReset(long worldTime) {
        long currentDay = worldTime / 24000L;
        long lastDay = lastDayReset / 24000L;

        if (currentDay > lastDay) {
            completedToday.clear();
            lastDayReset = worldTime;
            markDirty();
            STNTraders.LOGGER.debug("Reset daily quest completions (day {})", currentDay);
        }
    }

    /**
     * Get the quest state for a world.
     */
    public static PlayerQuestState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    /**
     * Get the quest state from the overworld (for cross-dimension access).
     */
    public static PlayerQuestState get(MinecraftServer server) {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        return get(overworld);
    }
}
