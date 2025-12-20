package com.stn.zombies.config;

/**
 * Configuration for STN Zombies.
 */
public class STNZombiesConfig {

    // Bruiser Zombie
    public static double BRUISER_HEALTH = 60.0;
    public static double BRUISER_DAMAGE = 8.0;
    public static double BRUISER_SPEED = 0.2;
    public static int BRUISER_WINDUP_TICKS = 20; // 1 second wind-up

    // Sprinter Zombie
    public static double SPRINTER_HEALTH = 15.0;
    public static double SPRINTER_DAMAGE = 4.0;
    public static double SPRINTER_SPEED = 0.26;
    public static double SPRINTER_BURST_SPEED = 0.45;
    public static double SPRINTER_BURST_RANGE = 8.0;

    // Spitter Zombie
    public static double SPITTER_HEALTH = 20.0;
    public static double SPITTER_DAMAGE = 2.0;
    public static double SPITTER_SPEED = 0.22;
    public static double SPITTER_PROJECTILE_DAMAGE = 4.0;
    public static int SPITTER_ATTACK_COOLDOWN = 60; // 3 seconds
    public static int SPITTER_POISON_DURATION = 100; // 5 seconds

    // Zombabie
    public static double ZOMBABIE_HEALTH = 8.0;
    public static double ZOMBABIE_DAMAGE = 2.0;
    public static double ZOMBABIE_SPEED = 0.3;
    public static double ZOMBABIE_LEAP_VELOCITY = 0.5;
    public static int ZOMBABIE_LEAP_COOLDOWN = 40; // 2 seconds

    // Howler Zombie
    public static double HOWLER_HEALTH = 25.0;
    public static double HOWLER_DAMAGE = 3.0;
    public static double HOWLER_SPEED = 0.23;
    public static int HOWLER_HOWL_COOLDOWN = 400; // 20 seconds
    public static double HOWLER_HOWL_RANGE = 32.0;
    public static int HOWLER_BUFF_DURATION = 200; // 10 seconds

    // Plague Zombie
    public static double PLAGUE_HEALTH = 25.0;
    public static double PLAGUE_DAMAGE = 3.0;
    public static double PLAGUE_SPEED = 0.2;
    public static int PLAGUE_SICKNESS_DURATION = 200; // 10 seconds per stack
    public static int PLAGUE_MAX_STACKS = 5;

    // Shielded Zombie
    public static double SHIELDED_HEALTH = 30.0;
    public static double SHIELDED_DAMAGE = 5.0;
    public static double SHIELDED_SPEED = 0.2;
    public static float SHIELDED_BLOCK_ANGLE = 90.0f; // Degrees from front

    // Electric Zombie
    public static double ELECTRIC_HEALTH = 25.0;
    public static double ELECTRIC_DAMAGE = 4.0;
    public static double ELECTRIC_SPEED = 0.22;
    public static double ELECTRIC_LIGHTNING_DAMAGE = 6.0;
    public static double ELECTRIC_AOE_RADIUS = 3.0;
    public static int ELECTRIC_LIGHTNING_COOLDOWN = 100; // 5 seconds

    // Leech Zombie
    public static double LEECH_HEALTH = 30.0;
    public static double LEECH_DAMAGE = 4.0;
    public static double LEECH_SPEED = 0.23;
    public static float LEECH_HEAL_PERCENT = 0.5f; // Heals 50% of damage dealt

    // Lumberjack Zombie
    public static double LUMBERJACK_HEALTH = 35.0;
    public static double LUMBERJACK_DAMAGE = 5.0;
    public static double LUMBERJACK_SPEED = 0.22;
    public static double LUMBERJACK_WOOD_BREAK_MULTIPLIER = 3.0;
    public static int LUMBERJACK_BLEED_DURATION = 100; // 5 seconds

    public static void init() {
        // TODO: Load from config file
    }
}
