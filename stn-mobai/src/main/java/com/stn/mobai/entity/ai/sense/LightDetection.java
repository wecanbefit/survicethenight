package com.stn.mobai.entity.ai.sense;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects light sources that mobs can see and target.
 * Uses caching to avoid expensive block iteration every query.
 */
public class LightDetection {

    // Detection thresholds
    public static final int MIN_LIGHT_LEVEL = 10;
    public static final double MAX_DETECTION_RANGE = 32.0; // Reduced from 48

    // Light source attraction weights (0.0 - 1.0)
    public static final float TORCH_WEIGHT = 0.6f;
    public static final float LANTERN_WEIGHT = 0.7f;
    public static final float CAMPFIRE_WEIGHT = 0.9f;
    public static final float FIRE_WEIGHT = 1.0f;
    public static final float LAVA_WEIGHT = 0.8f;
    public static final float GLOWSTONE_WEIGHT = 0.5f;
    public static final float SEA_LANTERN_WEIGHT = 0.5f;
    public static final float JACK_O_LANTERN_WEIGHT = 0.6f;
    public static final float DEFAULT_LIGHT_WEIGHT = 0.4f;

    // Cache for light sources per chunk region
    private static final Map<String, CachedLightData> lightCache = new ConcurrentHashMap<>();
    private static final int CACHE_VALIDITY_TICKS = 200; // 10 seconds
    private static final int SAMPLE_STEP = 2; // Check every 2nd block to reduce iterations

    /**
     * Find the most attractive light source in range using cached data.
     */
    public static LightSource findBrightestLight(ServerWorld world, BlockPos center, double range) {
        String cacheKey = getCacheKey(world, center);
        long currentTick = world.getTime();

        CachedLightData cached = lightCache.get(cacheKey);
        if (cached != null && !cached.isExpired(currentTick)) {
            return findBestFromCache(cached.sources, center, range);
        }

        // Rebuild cache for this region
        List<LightSource> sources = scanForLightSources(world, center, range);
        lightCache.put(cacheKey, new CachedLightData(sources, currentTick));

        return findBestFromCache(sources, center, range);
    }

    private static LightSource findBestFromCache(List<LightSource> sources, BlockPos center, double range) {
        LightSource best = null;
        double bestScore = 0;

        for (LightSource source : sources) {
            double distance = Math.sqrt(center.getSquaredDistance(source.position));
            if (distance > range) continue;

            double score = source.weight * (1.0 - distance / range);
            if (score > bestScore) {
                bestScore = score;
                best = new LightSource(source.position, source.weight, distance);
            }
        }

        return best;
    }

    /**
     * Scan for light sources with reduced iteration (sampling).
     */
    private static List<LightSource> scanForLightSources(ServerWorld world, BlockPos center, double range) {
        List<LightSource> sources = new ArrayList<>();
        int searchRadius = (int) Math.ceil(range);

        // Sample blocks instead of checking every single one
        for (int x = -searchRadius; x <= searchRadius; x += SAMPLE_STEP) {
            for (int y = -searchRadius / 3; y <= searchRadius / 3; y += SAMPLE_STEP) {
                for (int z = -searchRadius; z <= searchRadius; z += SAMPLE_STEP) {
                    BlockPos checkPos = center.add(x, y, z);

                    // Quick distance check before expensive getBlockState
                    if (x * x + y * y + z * z > searchRadius * searchRadius) continue;

                    BlockState state = world.getBlockState(checkPos);
                    float weight = getLightSourceWeight(state);

                    if (weight > 0) {
                        double distance = Math.sqrt(center.getSquaredDistance(checkPos));
                        sources.add(new LightSource(checkPos, weight, distance));
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
     * Calculate light detection score for targeting.
     */
    public static double calculateLightScore(BlockPos mobPos, LightSource source, double maxRange, float baseWeight) {
        double distance = Math.sqrt(mobPos.getSquaredDistance(source.position));
        if (distance > maxRange) return 0.0;

        double proximity = 1.0 - (distance / maxRange);
        return source.weight * proximity * baseWeight * 100.0;
    }

    /**
     * Get the attraction weight for a block that emits light.
     */
    public static float getLightSourceWeight(BlockState state) {
        Block block = state.getBlock();

        // Fire sources - very attractive
        if (block == Blocks.FIRE || block == Blocks.SOUL_FIRE) {
            return FIRE_WEIGHT;
        }

        // Campfires
        if (block == Blocks.CAMPFIRE || block == Blocks.SOUL_CAMPFIRE) {
            return CAMPFIRE_WEIGHT;
        }

        // Lava
        if (block == Blocks.LAVA) {
            return LAVA_WEIGHT;
        }

        // Lanterns
        if (block == Blocks.LANTERN || block == Blocks.SOUL_LANTERN) {
            return LANTERN_WEIGHT;
        }

        // Torches
        if (block == Blocks.TORCH || block == Blocks.WALL_TORCH ||
            block == Blocks.SOUL_TORCH || block == Blocks.SOUL_WALL_TORCH) {
            return TORCH_WEIGHT;
        }

        // Jack o'lantern
        if (block == Blocks.JACK_O_LANTERN) {
            return JACK_O_LANTERN_WEIGHT;
        }

        // Glowstone/Sea Lantern
        if (block == Blocks.GLOWSTONE) {
            return GLOWSTONE_WEIGHT;
        }
        if (block == Blocks.SEA_LANTERN) {
            return SEA_LANTERN_WEIGHT;
        }

        // Shroomlight, End Rod, etc.
        if (block == Blocks.SHROOMLIGHT || block == Blocks.END_ROD) {
            return DEFAULT_LIGHT_WEIGHT;
        }

        // Only check luminance for blocks that might be light sources
        // Avoid calling getLuminance() on every block
        return 0.0f;
    }

    /**
     * Clear expired cache entries. Call periodically.
     */
    public static void cleanupCache(long currentTick) {
        lightCache.entrySet().removeIf(entry -> entry.getValue().isExpired(currentTick));
    }

    /**
     * Represents a detected light source.
     */
    public static class LightSource {
        public final BlockPos position;
        public final float weight;
        public final double distance;

        public LightSource(BlockPos position, float weight, double distance) {
            this.position = position;
            this.weight = weight;
            this.distance = distance;
        }

        public double getScore(double maxRange) {
            double proximity = 1.0 - (distance / maxRange);
            return weight * proximity * 100.0;
        }
    }

    private static class CachedLightData {
        final List<LightSource> sources;
        final long createdTick;

        CachedLightData(List<LightSource> sources, long createdTick) {
            this.sources = sources;
            this.createdTick = createdTick;
        }

        boolean isExpired(long currentTick) {
            return currentTick - createdTick > CACHE_VALIDITY_TICKS;
        }
    }
}
