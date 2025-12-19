package com.stn.survival.spawn;

/**
 * Categories for horde mobs that determine spawn position logic.
 */
public enum MobCategory {
    /**
     * Ground-based mobs that spawn on solid surfaces.
     */
    GROUND,

    /**
     * Aerial mobs that spawn above players (phantoms, ghasts).
     */
    AERIAL,

    /**
     * Mobs that can phase through blocks (vex).
     */
    PHASING,

    /**
     * Aquatic mobs that need water (guardians, drowned).
     */
    AQUATIC
}
