package com.stn.survival.progression;

import com.stn.survival.config.STNSurvivalConfig;

import java.util.UUID;

/**
 * Tracks gamestage progression for individual players.
 */
public class PlayerGamestage {
    private final UUID playerUuid;
    private final String playerName;

    private int daysSurvived = 0;
    private int zombieKills = 0;
    private int survivalNightsSurvived = 0;
    private int deathCount = 0;

    public PlayerGamestage(UUID playerUuid, String playerName) {
        this.playerUuid = playerUuid;
        this.playerName = playerName;
    }

    /**
     * Calculate the player's current gamestage.
     * Formula: (Days * 2) + (Kills / 50) + (Survival Nights * 5) - (Deaths * 2)
     */
    public int calculateGamestage() {
        int gamestage = 0;

        gamestage += daysSurvived * STNSurvivalConfig.GAMESTAGE_DAYS_MULTIPLIER;
        gamestage += zombieKills / STNSurvivalConfig.GAMESTAGE_ZOMBIE_KILLS_DIVISOR;
        gamestage += survivalNightsSurvived * STNSurvivalConfig.GAMESTAGE_SURVIVAL_NIGHT_BONUS;
        gamestage -= deathCount * STNSurvivalConfig.GAMESTAGE_DEATH_PENALTY;

        return Math.max(0, gamestage);
    }

    public void incrementDaysSurvived() {
        daysSurvived++;
    }

    public void addZombieKill() {
        zombieKills++;
    }

    public void addSurvivalNightSurvived() {
        survivalNightsSurvived++;
    }

    public void onDeath() {
        deathCount++;
    }

    // Getters
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getDaysSurvived() {
        return daysSurvived;
    }

    public int getZombieKills() {
        return zombieKills;
    }

    public int getSurvivalNightsSurvived() {
        return survivalNightsSurvived;
    }

    public int getDeathCount() {
        return deathCount;
    }

    // Setters for loading saved data
    public void setDaysSurvived(int days) {
        this.daysSurvived = days;
    }

    public void setZombieKills(int kills) {
        this.zombieKills = kills;
    }

    public void setSurvivalNightsSurvived(int count) {
        this.survivalNightsSurvived = count;
    }

    public void setDeathCount(int deaths) {
        this.deathCount = deaths;
    }
}
