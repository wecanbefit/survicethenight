package com.stn.traders.protection;

import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * Represents a protected region (trader structure).
 * All blocks within the bounds are fully protected from interaction.
 */
public record ProtectedRegion(
    UUID id,
    String structureType,  // e.g., "booth", "shop", "bunker"
    BlockBox bounds,
    long createdTime
) {
    /**
     * Check if a block position is within this protected region.
     */
    public boolean contains(BlockPos pos) {
        return bounds.contains(pos);
    }

    /**
     * Check if coordinates are within this protected region.
     */
    public boolean contains(int x, int y, int z) {
        return bounds.contains(x, y, z);
    }
}
