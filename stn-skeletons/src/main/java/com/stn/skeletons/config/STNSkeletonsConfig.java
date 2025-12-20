package com.stn.skeletons.config;

/**
 * Configuration for STN Skeleton variants.
 * All values can be adjusted for balancing.
 */
public class STNSkeletonsConfig {

    // === Marksman Skeleton (Precision ranged) ===
    public static final double MARKSMAN_HEALTH = 20.0;
    public static final double MARKSMAN_SPEED = 0.23;
    public static final double MARKSMAN_ARROW_DAMAGE = 8.0; // Higher damage
    public static final int MARKSMAN_FIRE_COOLDOWN = 60; // 3 seconds between shots (slower)
    public static final float MARKSMAN_ACCURACY = 0.0f; // Perfect accuracy (0 = no spread)

    // === Suppressor Skeleton (Area control) ===
    public static final double SUPPRESSOR_HEALTH = 20.0;
    public static final double SUPPRESSOR_SPEED = 0.23;
    public static final double SUPPRESSOR_ARROW_DAMAGE = 3.0; // Lower damage
    public static final int SUPPRESSOR_FIRE_COOLDOWN = 15; // Fast fire rate
    public static final int SUPPRESSOR_SLOWNESS_DURATION = 40; // 2 seconds of slowness
    public static final int SUPPRESSOR_SLOWNESS_LEVEL = 1; // Slowness II

    // === Flame Archer Skeleton (DOT) ===
    public static final double FLAME_ARCHER_HEALTH = 20.0;
    public static final double FLAME_ARCHER_SPEED = 0.23;
    public static final double FLAME_ARCHER_ARROW_DAMAGE = 4.0;
    public static final int FLAME_ARCHER_FIRE_COOLDOWN = 30; // 1.5 seconds
    public static final int FLAME_ARCHER_BURN_DURATION = 100; // 5 seconds of fire

    // === Vanguard Skeleton (Frontline melee) ===
    public static final double VANGUARD_HEALTH = 40.0; // Tanky
    public static final double VANGUARD_SPEED = 0.2; // Slower
    public static final double VANGUARD_DAMAGE = 8.0; // Heavy hits
    public static final double VANGUARD_ARMOR = 4.0;
    public static final double VANGUARD_KNOCKBACK_RESISTANCE = 0.6;
    public static final int VANGUARD_ATTACK_COOLDOWN = 30; // 1.5 second swing

    // === Duelist Skeleton (Aggressive melee) ===
    public static final double DUELIST_HEALTH = 22.0;
    public static final double DUELIST_SPEED = 0.32; // Fast
    public static final double DUELIST_DAMAGE = 5.0;
    public static final int DUELIST_DASH_COOLDOWN = 100; // 5 seconds
    public static final double DUELIST_DASH_VELOCITY = 1.2;
    public static final double DUELIST_DASH_RANGE = 6.0; // Dash when within 6 blocks

    // === Reaper Skeleton (Executioner) ===
    public static final double REAPER_HEALTH = 25.0;
    public static final double REAPER_SPEED = 0.26;
    public static final double REAPER_DAMAGE = 5.0;
    public static final float REAPER_LOW_HEALTH_THRESHOLD = 0.3f; // 30% HP
    public static final double REAPER_EXECUTE_DAMAGE_MULTIPLIER = 2.0; // Double damage to low HP
    public static final int REAPER_KILL_SPEED_DURATION = 100; // 5 seconds speed boost
    public static final int REAPER_KILL_SPEED_LEVEL = 1; // Speed II
}
