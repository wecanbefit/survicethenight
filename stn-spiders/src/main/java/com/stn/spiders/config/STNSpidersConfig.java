package com.stn.spiders.config;

/**
 * Configuration for STN Spider variants.
 * All values can be adjusted for balancing.
 */
public class STNSpidersConfig {

    // === Stalker Spider (Ambush, 0.9x scale) ===
    public static final double STALKER_HEALTH = 16.0;
    public static final double STALKER_SPEED = 0.32;
    public static final double STALKER_DAMAGE = 3.0;
    public static final double STALKER_AMBUSH_BONUS_DAMAGE = 4.0; // Extra damage on first hit
    public static final int STALKER_INVISIBILITY_LIGHT_LEVEL = 7; // Invisible below this light level

    // === Webspinner Spider (Control, 1.1x scale) ===
    public static final double WEBSPINNER_HEALTH = 18.0;
    public static final double WEBSPINNER_SPEED = 0.28;
    public static final double WEBSPINNER_DAMAGE = 2.0;
    public static final int WEBSPINNER_SLOWNESS_DURATION = 60; // 3 seconds
    public static final int WEBSPINNER_SLOWNESS_LEVEL = 1; // Slowness II

    // === Leaper Spider (Burst mobility, 1.0x scale) ===
    public static final double LEAPER_HEALTH = 16.0;
    public static final double LEAPER_SPEED = 0.35;
    public static final double LEAPER_DAMAGE = 3.0;
    public static final double LEAPER_LEAP_VELOCITY = 1.0;
    public static final double LEAPER_LEAP_HEIGHT = 0.6;
    public static final int LEAPER_LEAP_COOLDOWN = 60; // 3 seconds
    public static final double LEAPER_LEAP_RANGE = 10.0;

    // === Broodmother Spider (Summoner, 1.6x scale) ===
    public static final double BROODMOTHER_HEALTH = 60.0;
    public static final double BROODMOTHER_SPEED = 0.2;
    public static final double BROODMOTHER_DAMAGE = 4.0;
    public static final double BROODMOTHER_ARMOR = 4.0;
    public static final int BROODMOTHER_SPAWN_COOLDOWN = 200; // 10 seconds
    public static final int BROODMOTHER_MAX_SPIDERLINGS = 6;

    // === Venom Spider (DOT, 1.15x scale) ===
    public static final double VENOM_HEALTH = 18.0;
    public static final double VENOM_SPEED = 0.3;
    public static final double VENOM_DAMAGE = 2.0;
    public static final int VENOM_POISON_DURATION = 100; // 5 seconds base
    public static final int VENOM_POISON_LEVEL = 1; // Poison II
    public static final int VENOM_POISON_STACK_BONUS = 40; // Additional ticks per stack

    // === Burden Spider (Debuffer/Tank, 1.4x scale) ===
    public static final double BURDEN_HEALTH = 40.0;
    public static final double BURDEN_SPEED = 0.22;
    public static final double BURDEN_DAMAGE = 4.0;
    public static final double BURDEN_ARMOR = 8.0;
    public static final double BURDEN_KNOCKBACK_RESISTANCE = 0.5;
    public static final int BURDEN_WEAKNESS_DURATION = 100; // 5 seconds
    public static final int BURDEN_FATIGUE_DURATION = 100; // 5 seconds
}
