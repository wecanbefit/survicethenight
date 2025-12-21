package com.stn.mobai.entity;

/**
 * Interface for mobs that can use enhanced sensory detection.
 * Implement this on any MobEntity to enable sound, smell, light, heat, and village detection.
 *
 * All detection methods return configurable weights that affect targeting priority.
 */
public interface ISensoryMob {

    // ========== SOUND DETECTION ==========

    /**
     * Get the maximum range this mob can detect sounds.
     * @return detection range in blocks
     */
    default double getSoundDetectionRange() {
        return 32.0;
    }

    /**
     * Get the weight multiplier for sound detection scoring.
     * Higher values = sound is weighted more heavily in target selection.
     * @return weight multiplier (default 1.0)
     */
    default float getSoundWeight() {
        return 1.0f;
    }

    // ========== PLAYER AWARENESS ==========

    /**
     * Get the maximum range this mob can detect players directly.
     * This works at all times (day/night) and prioritizes players over other targets.
     * @return player detection range in blocks
     */
    default double getPlayerDetectionRange() {
        return 48.0;
    }

    /**
     * Get the weight multiplier for player detection scoring.
     * Higher values = players are prioritized over other targets.
     * @return weight multiplier (default 1.5 - players are high priority)
     */
    default float getPlayerWeight() {
        return 1.5f;
    }

    // ========== SMELL DETECTION ==========

    /**
     * Get the maximum range this mob can smell targets (through walls).
     * Only active during survival night by default.
     * @return smell range in blocks
     */
    default double getSmellRange() {
        return 64.0;
    }

    /**
     * Get the weight multiplier for smell detection scoring.
     * @return weight multiplier (default 0.8)
     */
    default float getSmellWeight() {
        return 0.8f;
    }

    /**
     * Check if this mob can use smell detection.
     * @return true if smell detection is enabled
     */
    default boolean canSmell() {
        return true;
    }

    // ========== LIGHT DETECTION ==========

    /**
     * Get the maximum range this mob can detect light sources.
     * @return detection range in blocks
     */
    default double getLightDetectionRange() {
        return 48.0;
    }

    /**
     * Get the weight multiplier for light detection scoring.
     * @return weight multiplier (default 0.6)
     */
    default float getLightWeight() {
        return 0.6f;
    }

    /**
     * Check if this mob can detect light sources.
     * @return true if light detection is enabled
     */
    default boolean canDetectLight() {
        return true;
    }

    // ========== HEAT DETECTION ==========

    /**
     * Get the maximum range this mob can detect heat sources (fire, lava).
     * Heat can be detected through walls.
     * @return detection range in blocks
     */
    default double getHeatDetectionRange() {
        return 32.0;
    }

    /**
     * Get the weight multiplier for heat detection scoring.
     * @return weight multiplier (default 0.7)
     */
    default float getHeatWeight() {
        return 0.7f;
    }

    /**
     * Check if this mob can detect heat sources.
     * @return true if heat detection is enabled
     */
    default boolean canDetectHeat() {
        return true;
    }

    // ========== VILLAGE DETECTION ==========

    /**
     * Get the maximum range this mob can detect village structures.
     * @return village detection range in blocks
     */
    default double getVillageDetectionRange() {
        return 96.0;
    }

    /**
     * Get the weight multiplier for village detection scoring.
     * @return weight multiplier (default 0.5)
     */
    default float getVillageWeight() {
        return 0.5f;
    }

    /**
     * Check if this mob can detect villages.
     * @return true if village detection is enabled
     */
    default boolean canTargetVillages() {
        return true;
    }
}
