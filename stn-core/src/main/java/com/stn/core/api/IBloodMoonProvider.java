package com.stn.core.api;

/**
 * Interface for blood moon state providers.
 * Implemented by stn-bloodmoon to expose blood moon state to other mods.
 */
public interface IBloodMoonProvider {

    /**
     * Check if a blood moon is currently active.
     * @return true if blood moon is active
     */
    boolean isBloodMoonActive();

    /**
     * Get the current day in the blood moon cycle.
     * @return days since last blood moon (0 = blood moon day)
     */
    int getDaysUntilBloodMoon();

    /**
     * Get the blood moon interval (days between blood moons).
     * @return blood moon interval in days
     */
    int getBloodMoonInterval();

    /**
     * Force start a blood moon (for commands/testing).
     */
    void forceStart();

    /**
     * Force stop a blood moon (for commands/testing).
     */
    void forceStop();

    /**
     * Get the number of mobs spawned this blood moon.
     * @return spawned mob count
     */
    int getMobsSpawned();

    /**
     * Get the target horde size for this blood moon.
     * @return target horde size
     */
    int getTargetHordeSize();
}
