package com.stn.core.api;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

/**
 * Interface for gamestage progression providers.
 * Implemented by stn-bloodmoon to expose gamestage data to other mods.
 */
public interface IGamestageProvider {

    /**
     * Get the world-wide gamestage (average of all players).
     * @return world gamestage level
     */
    int getWorldGamestage();

    /**
     * Get a specific player's gamestage.
     * @param player the player
     * @return player's gamestage level
     */
    int getPlayerGamestage(ServerPlayerEntity player);

    /**
     * Get a specific player's gamestage by UUID.
     * @param playerId player UUID
     * @return player's gamestage level, or 0 if not found
     */
    int getPlayerGamestage(UUID playerId);

    /**
     * Add gamestage points to a player (for kills, achievements, etc.).
     * @param player the player
     * @param amount points to add
     */
    void addGamestage(ServerPlayerEntity player, int amount);

    /**
     * Remove gamestage points from a player (for deaths, penalties, etc.).
     * @param player the player
     * @param amount points to remove
     */
    void removeGamestage(ServerPlayerEntity player, int amount);

    /**
     * Get the horde size multiplier based on current gamestage.
     * @return multiplier (1.0 = normal)
     */
    float getHordeSizeMultiplier();

    /**
     * Check if a feature should be unlocked at current world gamestage.
     * @param requiredGamestage the gamestage threshold
     * @return true if world gamestage >= required
     */
    default boolean isUnlocked(int requiredGamestage) {
        return getWorldGamestage() >= requiredGamestage;
    }
}
