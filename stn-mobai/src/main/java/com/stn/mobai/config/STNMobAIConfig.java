package com.stn.mobai.config;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration values for stn-mobai.
 * TODO: Replace with proper config file loading.
 */
public class STNMobAIConfig {

    // Sound Detection
    public static int SOUND_DECAY_TICKS = 100; // 5 seconds
    public static double DEFAULT_SOUND_DETECTION_RANGE = 32.0;
    public static double DEFAULT_SMELL_RANGE = 64.0;
    public static double DEFAULT_VILLAGE_DETECTION_RANGE = 96.0;

    // Block Breaking
    public static boolean MOBS_BREAK_BLOCKS = true;
    public static float BLOCK_BREAK_SPEED_MULTIPLIER = 1.0f;

    // Speed Multipliers
    public static float NIGHT_SPEED_MULTIPLIER = 1.2f;
    public static float SURVIVAL_NIGHT_SPEED_MULTIPLIER = 1.5f;

    // AI Injection - which mobs get sensory AI
    public static boolean INJECT_ALL_HOSTILE_MOBS = true;

    // Mobs to exclude from AI injection (by entity ID)
    public static Set<String> EXCLUDED_MOBS = new HashSet<>();

    static {
        // Default exclusions - mobs that shouldn't have sensory AI
        // (e.g., flying mobs, ranged mobs that already have good targeting)
        // EXCLUDED_MOBS.add("minecraft:ghast");
        // EXCLUDED_MOBS.add("minecraft:phantom");
    }

    /**
     * Check if a mob should have sensory AI injected.
     */
    public static boolean shouldInjectAI(String entityId) {
        if (!INJECT_ALL_HOSTILE_MOBS) {
            return false;
        }
        return !EXCLUDED_MOBS.contains(entityId);
    }
}
