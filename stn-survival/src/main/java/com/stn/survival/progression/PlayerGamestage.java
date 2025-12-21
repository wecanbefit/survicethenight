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
     *
     * Time-based progression (cannot be reduced by deaths):
     *   - Each night survived: +0.15 gamestage
     *   - Each survival night event: +2.0 gamestage
     *
     * Kill bonus (can be reduced by deaths):
     *   - Each zombie kill: +0.005 gamestage (200 kills = 1 gamestage)
     *
     * Death penalty:
     *   - Each death: -2.0 gamestage (only reduces kill bonus, floor is time-based gamestage)
     */
    public int calculateGamestage() {
        // Time-based gamestage (protected floor - deaths cannot reduce below this)
        double timeBasedGamestage = (daysSurvived * STNSurvivalConfig.GAMESTAGE_PER_NIGHT)
                + (survivalNightsSurvived * STNSurvivalConfig.GAMESTAGE_SURVIVAL_NIGHT_BONUS);

        // Kill bonus (can be reduced by deaths)
        double killBonus = zombieKills * STNSurvivalConfig.GAMESTAGE_PER_ZOMBIE_KILL;

        // Death penalty only affects kill bonus
        double deathPenalty = deathCount * STNSurvivalConfig.GAMESTAGE_DEATH_PENALTY;
        double effectiveKillBonus = Math.max(0, killBonus - deathPenalty);

        // Total gamestage = floor (time-based) + effective kill bonus
        double totalGamestage = timeBasedGamestage + effectiveKillBonus;

        return (int) Math.floor(totalGamestage);
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
