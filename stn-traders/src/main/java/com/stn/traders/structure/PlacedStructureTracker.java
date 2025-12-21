package com.stn.traders.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stn.traders.STNTraders;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Tracks which villages have had trader structures placed near them.
 * Persists to world save to avoid duplicate placement.
 */
public class PlacedStructureTracker extends PersistentState {

    private static final String DATA_NAME = "stn_trader_structures";
    private static final int VILLAGE_CLUSTER_RADIUS = 100; // Treat nearby villages as one

    // Village position (long) -> structure ID placed
    private final Map<Long, String> placedVillages = new ConcurrentHashMap<>();

    // Serialization record
    private record PlacedEntry(long pos, String structureId) {
        private static final Codec<PlacedEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Codec.LONG.fieldOf("pos").forGetter(PlacedEntry::pos),
                Codec.STRING.fieldOf("structure").forGetter(PlacedEntry::structureId)
            ).apply(instance, PlacedEntry::new)
        );
    }

    private static final Codec<PlacedStructureTracker> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.list(PlacedEntry.CODEC).fieldOf("placed").forGetter(tracker ->
                tracker.placedVillages.entrySet().stream()
                    .map(e -> new PlacedEntry(e.getKey(), e.getValue()))
                    .collect(Collectors.toList())
            )
        ).apply(instance, entries -> {
            PlacedStructureTracker tracker = new PlacedStructureTracker();
            for (PlacedEntry entry : entries) {
                tracker.placedVillages.put(entry.pos(), entry.structureId());
            }
            STNTraders.LOGGER.info("Loaded {} placed trader structures", tracker.placedVillages.size());
            return tracker;
        })
    );

    private static final PersistentStateType<PlacedStructureTracker> TYPE = new PersistentStateType<>(
        DATA_NAME,
        PlacedStructureTracker::new,
        CODEC,
        null
    );

    public PlacedStructureTracker() {
        super();
    }

    /**
     * Get the tracker for a world.
     */
    public static PlacedStructureTracker get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    /**
     * Check if a structure has already been placed near a village position.
     */
    public boolean hasStructureNear(BlockPos villagePos) {
        // Check this position and nearby positions within cluster radius
        for (Long existing : placedVillages.keySet()) {
            BlockPos existingPos = BlockPos.fromLong(existing);
            if (villagePos.isWithinDistance(existingPos, VILLAGE_CLUSTER_RADIUS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mark a village as having a structure placed.
     */
    public void markPlaced(BlockPos villagePos, Identifier structureId) {
        placedVillages.put(villagePos.asLong(), structureId.toString());
        markDirty();
    }

    /**
     * Get the structure ID placed near a village.
     */
    public String getStructureNear(BlockPos villagePos) {
        for (Map.Entry<Long, String> entry : placedVillages.entrySet()) {
            BlockPos existingPos = BlockPos.fromLong(entry.getKey());
            if (villagePos.isWithinDistance(existingPos, VILLAGE_CLUSTER_RADIUS)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Get total placed structure count.
     */
    public int getPlacedCount() {
        return placedVillages.size();
    }

    /**
     * Remove a placed structure record (for admin commands).
     */
    public void removePlaced(BlockPos villagePos) {
        Long toRemove = null;
        for (Long existing : placedVillages.keySet()) {
            BlockPos existingPos = BlockPos.fromLong(existing);
            if (villagePos.isWithinDistance(existingPos, VILLAGE_CLUSTER_RADIUS)) {
                toRemove = existing;
                break;
            }
        }
        if (toRemove != null) {
            placedVillages.remove(toRemove);
            markDirty();
        }
    }
}
