package com.stn.traders.quest;

import java.util.UUID;

/**
 * An active quest instance for a player.
 */
public class QuestInstance {
    private final String questId;
    private final UUID playerId;
    private final int targetCount;
    private final int gamestageAtAccept;
    private final long startTime;
    private int progress;
    private boolean canTurnIn; // Client-side only, synced from server

    public QuestInstance(String questId, UUID playerId, int targetCount, int gamestageAtAccept, long startTime) {
        this.questId = questId;
        this.playerId = playerId;
        this.targetCount = targetCount;
        this.gamestageAtAccept = gamestageAtAccept;
        this.startTime = startTime;
        this.progress = 0;
        this.canTurnIn = false;
    }

    /**
     * Client-side constructor for display purposes only.
     */
    public QuestInstance(String questId, int progress, int targetCount, int gamestageAtAccept, boolean canTurnIn) {
        this.questId = questId;
        this.playerId = null;
        this.targetCount = targetCount;
        this.gamestageAtAccept = gamestageAtAccept;
        this.startTime = 0;
        this.progress = progress;
        this.canTurnIn = canTurnIn;
    }

    public String getQuestId() {
        return questId;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public int getGamestageAtAccept() {
        return gamestageAtAccept;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getProgress() {
        return progress;
    }

    public void addProgress(int amount) {
        this.progress = Math.min(targetCount, this.progress + amount);
    }

    public void setProgress(int progress) {
        this.progress = Math.min(targetCount, progress);
    }

    public boolean isComplete() {
        return progress >= targetCount;
    }

    /**
     * Returns true if this quest can be turned in (synced from server).
     * For GATHER/FETCH quests, this checks inventory on the server side.
     */
    public boolean canTurnIn() {
        return canTurnIn;
    }

    public float getProgressPercentage() {
        return targetCount > 0 ? (float) progress / targetCount : 0f;
    }
}
