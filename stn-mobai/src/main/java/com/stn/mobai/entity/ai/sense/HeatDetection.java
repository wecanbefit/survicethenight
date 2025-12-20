package com.stn.mobai.entity.ai.sense;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects heat sources (fire, lava, furnaces) that mobs can sense.
 * Heat can be detected through walls unlike light.
 * Uses caching to avoid expensive block iteration every query.
 */
public class HeatDetection {

    // Detection range
    public static final double MAX_HEAT_DETECTION_RANGE = 24.0; // Reduced from 32

    // Heat source weights (0.0 - 1.0)
    public static final float FIRE_HEAT = 1.0f;
    public static final float LAVA_HEAT = 0.95f;
    public static final float CAMPFIRE_HEAT = 0.8f;
    public static final float FURNACE_HEAT = 0.6f;
    public static final float SMOKER_HEAT = 0.5f;
    public static final float BLAST_FURNACE_HEAT = 0.7f;
    public static final float MAGMA_BLOCK_HEAT = 0.4f;
    public static final float SOUL_FIRE_HEAT = 0.7f;

    // Cache for heat sources per chunk region
    private static final Map<String, CachedHeatData> heatCache = new ConcurrentHashMap<>();
    private static final int CACHE_VALIDITY_TICKS = 200; // 10 seconds
    private static final int SAMPLE_STEP = 2; // Check every 2nd block

    /**
     * Find the hottest heat source in range using cached data.
     */
    public static HeatSource findHottestSource(ServerWorld world, BlockPos center, double range) {
        String cacheKey = getCacheKey(world, center);
        long currentTick = world.getTime();

        CachedHeatData cached = heatCache.get(cacheKey);
        if (cached != null && !cached.isExpired(currentTick)) {
            return findBestFromCache(cached.sources, center, range);
        }

        // Rebuild cache for this region
        List<HeatSource> sources = scanForHeatSources(world, center, range);
        heatCache.put(cacheKey, new CachedHeatData(sources, currentTick));

        return findBestFromCache(sources, center, range);
    }

    private static HeatSource findBestFromCache(List<HeatSource> sources, BlockPos center, double range) {
        HeatSource best = null;
        double bestScore = 0;

        for (HeatSource source : sources) {
            double distance = Math.sqrt(center.getSquaredDistance(source.position));
            if (distance > range) continue;

            double score = source.heat * (1.0 - distance / range);
            if (score > bestScore) {
                bestScore = score;
                best = new HeatSource(source.position, source.heat, distance);
            }
        }

        return best;
    }

    /**
     * Scan for heat sources with reduced iteration (sampling).
     */
    private static List<HeatSource> scanForHeatSources(ServerWorld world, BlockPos center, double range) {
        List<HeatSource> sources = new ArrayList<>();
        int searchRadius = (int) Math.ceil(range);

        // Sample blocks instead of checking every single one
        for (int x = -searchRadius; x <= searchRadius; x += SAMPLE_STEP) {
            for (int y = -searchRadius / 3; y <= searchRadius / 3; y += SAMPLE_STEP) {
                for (int z = -searchRadius; z <= searchRadius; z += SAMPLE_STEP) {
                    // Quick distance check before expensive getBlockState
                    if (x * x + y * y + z * z > searchRadius * searchRadius) continue;

                    BlockPos checkPos = center.add(x, y, z);
                    BlockState state = world.getBlockState(checkPos);
                    float heat = getHeatLevel(state);

                    if (heat > 0) {
                        double distance = Math.sqrt(center.getSquaredDistance(checkPos));
                        sources.add(new HeatSource(checkPos, heat, distance));
                    }
                }
            }
        }

        return sources;
    }

    private static String getCacheKey(ServerWorld world, BlockPos center) {
        // Cache by 32-block regions
        int regionX = center.getX() >> 5;
        int regionZ = center.getZ() >> 5;
        return world.getRegistryKey().getValue() + ":" + regionX + "," + regionZ;
    }

    /**
     * Calculate heat detection score for targeting.
     */
    public static double calculateHeatScore(BlockPos mobPos, HeatSource source, double maxRange, float baseWeight) {
        double distance = Math.sqrt(mobPos.getSquaredDistance(source.position));
        if (distance > maxRange) return 0.0;

        double proximity = 1.0 - (distance / maxRange);
        return source.heat * proximity * baseWeight * 100.0;
    }

    /**
     * Get the heat level for a block.
     */
    public static float getHeatLevel(BlockState state) {
        Block block = state.getBlock();

        // Fire
        if (block == Blocks.FIRE) {
            return FIRE_HEAT;
        }
        if (block == Blocks.SOUL_FIRE) {
            return SOUL_FIRE_HEAT;
        }

        // Lava
        if (block == Blocks.LAVA) {
            return LAVA_HEAT;
        }

        // Campfires (only when lit)
        if (block == Blocks.CAMPFIRE) {
            if (state.get(CampfireBlock.LIT)) {
                return CAMPFIRE_HEAT;
            }
            return 0.0f;
        }
        if (block == Blocks.SOUL_CAMPFIRE) {
            if (state.get(CampfireBlock.LIT)) {
                return CAMPFIRE_HEAT * 0.8f;
            }
            return 0.0f;
        }

        // Furnaces (when active - has light)
        if (block == Blocks.FURNACE) {
            if (state.getLuminance() > 0) {
                return FURNACE_HEAT;
            }
            return 0.0f;
        }
        if (block == Blocks.BLAST_FURNACE) {
            if (state.getLuminance() > 0) {
                return BLAST_FURNACE_HEAT;
            }
            return 0.0f;
        }
        if (block == Blocks.SMOKER) {
            if (state.getLuminance() > 0) {
                return SMOKER_HEAT;
            }
            return 0.0f;
        }

        // Magma blocks
        if (block == Blocks.MAGMA_BLOCK) {
            return MAGMA_BLOCK_HEAT;
        }

        return 0.0f;
    }

    /**
     * Clear expired cache entries. Call periodically.
     */
    public static void cleanupCache(long currentTick) {
        heatCache.entrySet().removeIf(entry -> entry.getValue().isExpired(currentTick));
    }

    /**
     * Represents a detected heat source.
     */
    public static class HeatSource {
        public final BlockPos position;
        public final float heat;
        public final double distance;

        public HeatSource(BlockPos position, float heat, double distance) {
            this.position = position;
            this.heat = heat;
            this.distance = distance;
        }

        public double getScore(double maxRange) {
            double proximity = 1.0 - (distance / maxRange);
            return heat * proximity * 100.0;
        }
    }

    private static class CachedHeatData {
        final List<HeatSource> sources;
        final long createdTick;

        CachedHeatData(List<HeatSource> sources, long createdTick) {
            this.sources = sources;
            this.createdTick = createdTick;
        }

        boolean isExpired(long currentTick) {
            return currentTick - createdTick > CACHE_VALIDITY_TICKS;
        }
    }
}
