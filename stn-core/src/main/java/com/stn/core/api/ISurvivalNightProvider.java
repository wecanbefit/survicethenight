package com.stn.core.api;

/**
 * Interface for survival night state providers.
 * Implemented by stn-survival to expose survival night state to other mods.
 */
public interface ISurvivalNightProvider {

    /**
     * Check if a survival night is currently active.
     * @return true if survival night is active
     */
    boolean isSurvivalNightActive();

    /**
     * Get the current day in the survival night cycle.
     * @return days until next survival night
     */
    int getDaysUntilSurvivalNight();

    /**
     * Get the survival night interval (days between survival nights).
     * @return survival night interval in days
     */
    int getSurvivalNightInterval();

    /**
     * Force start a survival night (for commands/testing).
     */
    void forceStart();

    /**
     * Force stop a survival night (for commands/testing).
     */
    void forceStop();

    /**
     * Get the number of mobs spawned this survival night.
     * @return spawned mob count
     */
    int getMobsSpawned();

    /**
     * Get the target horde size for this survival night.
     * @return target horde size
     */
    int getTargetHordeSize();
}
