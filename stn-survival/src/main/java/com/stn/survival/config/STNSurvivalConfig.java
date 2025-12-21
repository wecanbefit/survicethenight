package com.stn.survival.config;

import com.stn.survival.STNSurvival;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for Survive The Night - Survival Night events.
 */
public class STNSurvivalConfig {

    // Mob Spawn Control - Set of entity IDs that should NOT spawn
    public static Set<String> DISABLED_MOB_SPAWNS = new HashSet<>();

    // Survival Night Settings
    public static int SURVIVAL_NIGHT_INTERVAL = 10; // Every 10 days
    public static int SURVIVAL_NIGHT_START_HOUR = 22; // 10 PM in-game time
    public static int SURVIVAL_NIGHT_END_HOUR = 6; // 6 AM - sunrise

    // Horde Settings
    public static int BASE_HORDE_SIZE = 20;
    public static float HORDE_SIZE_MULTIPLIER_PER_DAY = 0.1f; // 10% more zombies per day
    public static int MAX_HORDE_SIZE = 200;
    public static int SPAWN_RADIUS_MIN = 24;
    public static int SPAWN_RADIUS_MAX = 48;
    public static int SPAWN_WAVE_INTERVAL = 600; // 30 seconds between waves (in ticks)

    // Zombie Behavior Settings
    public static boolean ZOMBIES_BREAK_BLOCKS = true;
    public static float BLOCK_BREAK_SPEED_MULTIPLIER = 1.0f;
    public static boolean ZOMBIES_TARGET_BEDS = true;
    public static boolean ZOMBIES_SENSE_THROUGH_WALLS = true;
    public static int ZOMBIE_SENSE_RANGE = 48;

    // Zombie Sensory System Settings
    public static int ZOMBIE_SOUND_DETECTION_RANGE = 32;
    public static int ZOMBIE_SMELL_RANGE = 64;
    public static int ZOMBIE_VILLAGE_DETECTION_RANGE = 96;
    public static int SOUND_DECAY_TICKS = 100;
    public static int SENSE_CHECK_INTERVAL = 20;

    // Zombie Variant Spawn Weights (higher = more common)
    public static int FERAL_ZOMBIE_WEIGHT = 30;
    public static int BLOATED_ZOMBIE_WEIGHT = 15;
    public static int SPRINTER_ZOMBIE_WEIGHT = 25;
    public static int DEMOLISHER_ZOMBIE_WEIGHT = 10;
    public static int NORMAL_ZOMBIE_WEIGHT = 20;

    // Day/Night Behavior
    public static float NIGHT_SPEED_MULTIPLIER = 1.5f;
    public static float NIGHT_DAMAGE_MULTIPLIER = 1.25f;
    public static float SURVIVAL_NIGHT_SPEED_MULTIPLIER = 2.0f;
    public static float SURVIVAL_NIGHT_DAMAGE_MULTIPLIER = 1.5f;

    // Difficulty Scaling
    public static boolean SCALE_WITH_PLAYER_COUNT = true;
    public static float PLAYER_COUNT_MULTIPLIER = 0.5f;

    // Gamestage Settings
    public static boolean ENABLE_GAMESTAGE = true;
    public static double GAMESTAGE_PER_NIGHT = 0.15;           // +0.15 per night survived
    public static double GAMESTAGE_SURVIVAL_NIGHT_BONUS = 2.0; // +2 per survival night event
    public static double GAMESTAGE_PER_ZOMBIE_KILL = 0.005;    // 200 kills = 1 gamestage (0.25 per 50)
    public static double GAMESTAGE_DEATH_PENALTY = 2.0;        // -2 per death (only affects kill bonus)

    // Gamestage Thresholds
    public static int GAMESTAGE_FERAL_THRESHOLD = 10;
    public static int GAMESTAGE_SPRINTER_THRESHOLD = 25;
    public static int GAMESTAGE_DEMOLISHER_THRESHOLD = 50;
    public static int GAMESTAGE_SCREAMER_THRESHOLD = 75;
    public static int GAMESTAGE_SPIDER_JOCKEY_THRESHOLD = 25;

    // Screamer Settings
    public static int SCREAMER_SPAWN_WEIGHT = 10;
    public static int SCREAMER_HORDE_SIZE = 8;
    public static int SCREAMER_SCREAM_COOLDOWN = 1200;
    public static int SCREAMER_DETECTION_RANGE = 64;

    // Spider Jockey Settings
    public static int SPIDER_JOCKEY_SPAWN_WEIGHT = 10;
    public static int CAVE_SPIDER_JOCKEY_SPAWN_WEIGHT = 5;
    public static int BABY_JOCKEY_SPAWN_WEIGHT = 3;

    // Fortification Settings
    public static float SPIKE_DAMAGE_MULTIPLIER = 1.0f;
    public static boolean ELECTRIC_FENCE_ENABLED = true;

    // Visual Effects Settings
    public static boolean ENABLE_SURVIVAL_NIGHT_VISUALS = true;
    public static boolean ENABLE_RED_MOON = true;
    public static boolean ENABLE_RED_AMBIENT = true;
    public static boolean ENABLE_RED_LIGHT_TINT = true;
    public static float RED_MOON_INTENSITY = 1.0f;
    public static float RED_FOG_INTENSITY = 0.3f;
    public static float RED_SKY_GLOW_ALPHA = 0.2f;
    public static float RED_LIGHT_TINT_INTENSITY = 0.7f;

    // HUD Settings
    public static String HAMMER_HUD_POSITION = "bm";

    public static void init() {
        STNSurvival.LOGGER.info("STN Survival config loaded with default values");
        STNSurvival.LOGGER.info("Survival Night occurs every {} days", SURVIVAL_NIGHT_INTERVAL);
        STNSurvival.LOGGER.info("Base horde size: {}", BASE_HORDE_SIZE);

        if (!DISABLED_MOB_SPAWNS.isEmpty()) {
            STNSurvival.LOGGER.info("Disabled mob spawns: {}", DISABLED_MOB_SPAWNS);
        }
    }

    public static void disableMobSpawn(String entityId) {
        DISABLED_MOB_SPAWNS.add(entityId);
        STNSurvival.LOGGER.info("Disabled spawning for: {}", entityId);
    }

    public static void enableMobSpawn(String entityId) {
        DISABLED_MOB_SPAWNS.remove(entityId);
        STNSurvival.LOGGER.info("Enabled spawning for: {}", entityId);
    }

    public static boolean isMobSpawnAllowed(EntityType<?> entityType) {
        Identifier id = Registries.ENTITY_TYPE.getId(entityType);
        return !DISABLED_MOB_SPAWNS.contains(id.toString());
    }

    public static int calculateHordeSize(int gameDay, int playerCount) {
        float dayMultiplier = 1.0f + (gameDay * HORDE_SIZE_MULTIPLIER_PER_DAY);
        float playerMultiplier = 1.0f + ((playerCount - 1) * PLAYER_COUNT_MULTIPLIER);

        int hordeSize = (int) (BASE_HORDE_SIZE * dayMultiplier * playerMultiplier);
        return Math.min(hordeSize, MAX_HORDE_SIZE);
    }

    public static boolean isSurvivalNightDay(long gameDay) {
        return gameDay > 0 && gameDay % SURVIVAL_NIGHT_INTERVAL == 0;
    }
}
