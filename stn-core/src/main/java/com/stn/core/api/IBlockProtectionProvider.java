package com.stn.core.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for block protection providers.
 * Implemented by stn-traders to protect trader structure blocks.
 */
public interface IBlockProtectionProvider {

    /**
     * Check if a block is protected from all interaction.
     * Protected blocks cannot be broken, used, or modified by players or mobs.
     * @param world the world
     * @param pos block position
     * @return true if block is protected
     */
    boolean isProtected(World world, BlockPos pos);

    /**
     * Get the protection type/reason for a protected block.
     * Useful for debugging and player feedback.
     * @param world the world
     * @param pos block position
     * @return protection type name (e.g., "Trader Structure"), or null if not protected
     */
    @Nullable
    String getProtectionType(World world, BlockPos pos);
}
