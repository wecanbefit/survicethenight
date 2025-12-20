package com.stn.mobai.entity;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * Interface for mobs that can break blocks.
 * Implement this on any MobEntity to enable block breaking behavior.
 */
public interface IBlockBreaker extends BlockBreakAnimatable {

    /**
     * Get the block break speed multiplier for this mob.
     * Higher values = faster breaking.
     * @return speed multiplier (1.0 = normal)
     */
    default double getBlockBreakSpeed() {
        return 1.0;
    }

    /**
     * Get the base time in ticks to break a block.
     * Lower values = faster breaking.
     * @return base break time in ticks
     */
    default int getBaseBreakTime() {
        return 40;
    }

    /**
     * Check if this mob can break a specific block type.
     * Override to customize which blocks this mob can break.
     * @param state the block state to check
     * @param pos the position of the block
     * @return true if the mob can break this block
     */
    default boolean canBreakBlock(BlockState state, BlockPos pos) {
        return true;
    }

    /**
     * Get the damage multiplier for breaking wooden blocks.
     * Some mobs (like lumberjacks) might break wood faster.
     * @return wood break multiplier (1.0 = normal)
     */
    default double getWoodBreakMultiplier() {
        return 1.0;
    }

    /**
     * Get the damage multiplier for breaking stone blocks.
     * Some mobs might break stone faster/slower.
     * @return stone break multiplier (1.0 = normal)
     */
    default double getStoneBreakMultiplier() {
        return 1.0;
    }
}
