package com.stn.traders.protection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stn.traders.STNTraders;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Persistent state for tracking protected trader structure regions.
 * Uses chunk-based spatial indexing for fast lookups.
 */
public class TraderProtectionState extends PersistentState {

    private static final String DATA_NAME = "stn_trader_protection";

    // All protected regions by UUID
    private final Map<UUID, ProtectedRegion> regions = new ConcurrentHashMap<>();

    // Spatial index: chunk key -> set of region UUIDs that overlap that chunk
    private final Map<Long, Set<UUID>> chunkIndex = new ConcurrentHashMap<>();

    // Serialization record for a single region
    private record RegionEntry(
        String id,
        String type,
        int minX, int minY, int minZ,
        int maxX, int maxY, int maxZ,
        long created
    ) {
        private static final Codec<RegionEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.STRING.fieldOf("id").forGetter(RegionEntry::id),
                Codec.STRING.fieldOf("type").forGetter(RegionEntry::type),
                Codec.INT.fieldOf("minX").forGetter(RegionEntry::minX),
                Codec.INT.fieldOf("minY").forGetter(RegionEntry::minY),
                Codec.INT.fieldOf("minZ").forGetter(RegionEntry::minZ),
                Codec.INT.fieldOf("maxX").forGetter(RegionEntry::maxX),
                Codec.INT.fieldOf("maxY").forGetter(RegionEntry::maxY),
                Codec.INT.fieldOf("maxZ").forGetter(RegionEntry::maxZ),
                Codec.LONG.fieldOf("created").forGetter(RegionEntry::created)
            ).apply(instance, RegionEntry::new)
        );

        static RegionEntry fromRegion(ProtectedRegion region) {
            BlockBox b = region.bounds();
            return new RegionEntry(
                region.id().toString(),
                region.structureType(),
                b.getMinX(), b.getMinY(), b.getMinZ(),
                b.getMaxX(), b.getMaxY(), b.getMaxZ(),
                region.createdTime()
            );
        }

        ProtectedRegion toRegion() {
            return new ProtectedRegion(
                UUID.fromString(id),
                type,
                new BlockBox(minX, minY, minZ, maxX, maxY, maxZ),
                created
            );
        }
    }

    // Codec for the state
    private static final Codec<TraderProtectionState> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.list(RegionEntry.CODEC).fieldOf("regions").forGetter(state ->
                state.regions.values().stream()
                    .map(RegionEntry::fromRegion)
                    .collect(Collectors.toList())
            )
        ).apply(instance, entries -> {
            TraderProtectionState state = new TraderProtectionState();
            for (RegionEntry entry : entries) {
                state.addRegionInternal(entry.toRegion());
            }
            STNTraders.LOGGER.info("Loaded {} protected trader regions", state.regions.size());
            return state;
        })
    );

    private static final PersistentStateType<TraderProtectionState> TYPE = new PersistentStateType<>(
        DATA_NAME,
        TraderProtectionState::new,
        CODEC,
        null
    );

    public TraderProtectionState() {
        super();
    }

    /**
     * Get the protection state for a world.
     */
    public static TraderProtectionState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    /**
     * Add a new protected region.
     */
    public void addRegion(ProtectedRegion region) {
        addRegionInternal(region);
        markDirty();
    }

    private void addRegionInternal(ProtectedRegion region) {
        regions.put(region.id(), region);
        indexRegion(region);
    }

    /**
     * Remove a protected region by ID.
     */
    public void removeRegion(UUID id) {
        ProtectedRegion removed = regions.remove(id);
        if (removed != null) {
            unindexRegion(removed);
            markDirty();
        }
    }

    /**
     * Check if a block position is protected.
     */
    public boolean isProtected(BlockPos pos) {
        // Quick chunk-based lookup
        long chunkKey = getChunkKey(pos.getX() >> 4, pos.getZ() >> 4);
        Set<UUID> candidateIds = chunkIndex.get(chunkKey);

        if (candidateIds == null || candidateIds.isEmpty()) {
            return false;
        }

        // Check each candidate region
        for (UUID id : candidateIds) {
            ProtectedRegion region = regions.get(id);
            if (region != null && region.contains(pos)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the protected region at a position, if any.
     */
    public ProtectedRegion getRegionAt(BlockPos pos) {
        long chunkKey = getChunkKey(pos.getX() >> 4, pos.getZ() >> 4);
        Set<UUID> candidateIds = chunkIndex.get(chunkKey);

        if (candidateIds == null) {
            return null;
        }

        for (UUID id : candidateIds) {
            ProtectedRegion region = regions.get(id);
            if (region != null && region.contains(pos)) {
                return region;
            }
        }

        return null;
    }

    /**
     * Get all protected regions.
     */
    public Collection<ProtectedRegion> getAllRegions() {
        return Collections.unmodifiableCollection(regions.values());
    }

    /**
     * Get region count.
     */
    public int getRegionCount() {
        return regions.size();
    }

    // === Chunk Indexing ===

    private void indexRegion(ProtectedRegion region) {
        BlockBox bounds = region.bounds();
        int minCX = bounds.getMinX() >> 4;
        int maxCX = bounds.getMaxX() >> 4;
        int minCZ = bounds.getMinZ() >> 4;
        int maxCZ = bounds.getMaxZ() >> 4;

        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                long key = getChunkKey(cx, cz);
                chunkIndex.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                         .add(region.id());
            }
        }
    }

    private void unindexRegion(ProtectedRegion region) {
        BlockBox bounds = region.bounds();
        int minCX = bounds.getMinX() >> 4;
        int maxCX = bounds.getMaxX() >> 4;
        int minCZ = bounds.getMinZ() >> 4;
        int maxCZ = bounds.getMaxZ() >> 4;

        for (int cx = minCX; cx <= maxCX; cx++) {
            for (int cz = minCZ; cz <= maxCZ; cz++) {
                long key = getChunkKey(cx, cz);
                Set<UUID> set = chunkIndex.get(key);
                if (set != null) {
                    set.remove(region.id());
                    if (set.isEmpty()) {
                        chunkIndex.remove(key);
                    }
                }
            }
        }
    }

    private static long getChunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }
}
