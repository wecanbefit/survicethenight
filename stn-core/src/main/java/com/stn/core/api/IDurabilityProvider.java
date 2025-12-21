package com.stn.core.api;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Interface for block durability providers.
 * Implemented by stn-fortifications to expose durability system to other mods.
 */
public interface IDurabilityProvider {

    /**
     * Check if a block position is being tracked for durability.
     * @param world the world
     * @param pos block position
     * @return true if tracked
     */
    boolean isTracked(World world, BlockPos pos);

    /**
     * Get the current durability of a block.
     * @param world the world
     * @param pos block position
     * @return current durability, or -1 if not tracked
     */
    int getDurability(World world, BlockPos pos);

    /**
     * Get the maximum durability of a block.
     * @param world the world
     * @param pos block position
     * @return max durability, or -1 if not tracked
     */
    int getMaxDurability(World world, BlockPos pos);

    /**
     * Get durability as a percentage (0.0 - 1.0).
     * @param world the world
     * @param pos block position
     * @return durability percent, or 1.0 if not tracked
     */
    float getDurabilityPercent(World world, BlockPos pos);

    /**
     * Damage a block's durability.
     * @param world the world
     * @param pos block position
     * @param damage amount of damage
     * @return true if block should break
     */
    boolean damageBlock(World world, BlockPos pos, int damage);

    /**
     * Repair a block's durability.
     * @param world the world
     * @param pos block position
     * @param amount amount to repair
     * @return true if repair was successful
     */
    boolean repairBlock(World world, BlockPos pos, int amount);

    /**
     * Fully repair a block's durability.
     * @param world the world
     * @param pos block position
     * @return true if repair was successful
     */
    boolean fullyRepairBlock(World world, BlockPos pos);

    /**
     * Start tracking a block for durability.
     * @param world the world
     * @param pos block position
     */
    void trackBlock(ServerWorld world, BlockPos pos);

    /**
     * Stop tracking a block.
     * @param world the world
     * @param pos block position
     */
    void untrackBlock(World world, BlockPos pos);
}
