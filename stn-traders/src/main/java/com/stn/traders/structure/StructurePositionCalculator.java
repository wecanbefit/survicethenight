package com.stn.traders.structure;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

/**
 * Calculates valid positions for trader structures relative to villages.
 */
public class StructurePositionCalculator {

    private static final int MAX_ATTEMPTS = 16;
    private static final int DEFAULT_DISTANCE = 50;

    /**
     * Calculate a valid position for a structure near a village.
     * Only uses already-generated chunks to avoid triggering chunk generation cascades.
     *
     * @param world the server world
     * @param villageCenter center of the village (usually the bell)
     * @param distance distance from village center
     * @param structureSize size of the structure for validation
     * @return valid position, or null if none found
     */
    public static BlockPos calculate(ServerWorld world, BlockPos villageCenter, int distance, Vec3i structureSize) {
        Direction[] cardinalDirections = {
            Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
        };

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            // Pick a direction with some randomness
            Direction dir = cardinalDirections[world.getRandom().nextInt(cardinalDirections.length)];

            // Add random offset to avoid perfectly aligned structures
            int offsetX = dir.getOffsetX() * distance + world.getRandom().nextInt(21) - 10;
            int offsetZ = dir.getOffsetZ() * distance + world.getRandom().nextInt(21) - 10;

            // If direction has no X offset, add some X variance
            if (dir.getOffsetX() == 0) {
                offsetX = world.getRandom().nextInt(41) - 20;
            }
            // If direction has no Z offset, add some Z variance
            if (dir.getOffsetZ() == 0) {
                offsetZ = world.getRandom().nextInt(41) - 20;
            }

            BlockPos candidate = villageCenter.add(offsetX, 0, offsetZ);

            // Check if chunk is already generated - don't trigger generation
            ChunkPos chunkPos = new ChunkPos(candidate);
            if (!isChunkGenerated(world, chunkPos)) {
                continue; // Skip ungenerated chunks
            }

            // Get proper Y level from heightmap
            int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, candidate.getX(), candidate.getZ());
            candidate = candidate.withY(y);

            if (isValidPlacement(world, candidate, structureSize)) {
                return candidate;
            }
        }

        return null; // No valid position found
    }

    /**
     * Check if a chunk is already fully generated.
     */
    private static boolean isChunkGenerated(ServerWorld world, ChunkPos pos) {
        return world.getChunkManager().isChunkLoaded(pos.x, pos.z);
    }

    /**
     * Calculate with default distance (50 blocks).
     */
    public static BlockPos calculate(ServerWorld world, BlockPos villageCenter, Vec3i structureSize) {
        return calculate(world, villageCenter, DEFAULT_DISTANCE, structureSize);
    }

    /**
     * Check if a position is valid for structure placement.
     */
    private static boolean isValidPlacement(ServerWorld world, BlockPos pos, Vec3i structureSize) {
        // Check all chunks the structure would occupy are loaded
        int halfX = structureSize.getX() / 2;
        int halfZ = structureSize.getZ() / 2;

        ChunkPos minChunk = new ChunkPos(pos.add(-halfX, 0, -halfZ));
        ChunkPos maxChunk = new ChunkPos(pos.add(halfX, 0, halfZ));

        for (int cx = minChunk.x; cx <= maxChunk.x; cx++) {
            for (int cz = minChunk.z; cz <= maxChunk.z; cz++) {
                if (!world.getChunkManager().isChunkLoaded(cx, cz)) {
                    return false;
                }
            }
        }

        // Check ground is solid
        BlockPos below = pos.down();
        BlockState groundState = world.getBlockState(below);
        if (!groundState.isSolidBlock(world, below)) {
            return false;
        }

        // Check not in water
        if (world.getBlockState(pos).isLiquid()) {
            return false;
        }

        // Check height variation across structure footprint isn't too extreme
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int x = -halfX; x <= halfX; x += 2) {
            for (int z = -halfZ; z <= halfZ; z += 2) {
                int y = world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.getX() + x, pos.getZ() + z);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }

        // Don't place on steep terrain (more than 4 blocks difference)
        if (maxY - minY > 4) {
            return false;
        }

        return true;
    }
}
