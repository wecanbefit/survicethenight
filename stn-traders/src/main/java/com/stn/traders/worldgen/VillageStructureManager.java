package com.stn.traders.worldgen;

import com.stn.core.STNCore;
import com.stn.core.api.IGamestageProvider;
import com.stn.traders.STNTraders;
import com.stn.traders.structure.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Manages placement of trader structures near villages.
 * Hooks into chunk load events to detect villages and place structures.
 */
public class VillageStructureManager {

    private static final int VILLAGE_SEARCH_RADIUS = 96;
    private static final int STRUCTURE_DISTANCE_FROM_VILLAGE = 50;
    private static final int SPAWN_STRUCTURE_DISTANCE = 80; // Distance from world spawn

    // Track chunks we've already processed to avoid redundant checks
    private static final Set<Long> processedChunks = new HashSet<>();

    // Configuration
    private static boolean enabled = true;

    /**
     * Initialize the village structure manager.
     */
    public static void init() {
        // Reset flags on world load
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.getRegistryKey() == World.OVERWORLD) {
                spawnStructurePlaced = false;
                processedChunks.clear();
            }
        });

        // Listen for chunk loads - only process village structures
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (!enabled) return;
            if (world.getRegistryKey() != World.OVERWORLD) return;

            ChunkPos chunkPos = chunk.getPos();
            long chunkKey = chunkPos.toLong();

            // Skip if already processed
            if (processedChunks.contains(chunkKey)) return;
            processedChunks.add(chunkKey);

            // Schedule processing on next tick - only village structures
            world.getServer().execute(() -> {
                processChunkForStructures(world, chunkPos);
            });
        });

        // Place spawn structure when first player joins (spawn chunks guaranteed loaded)
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerWorld overworld = server.getOverworld();
            if (overworld != null && !spawnStructurePlaced) {
                server.execute(() -> placeSpawnStructure(overworld));
            }
        });

        STNTraders.LOGGER.info("Village structure manager initialized");
    }

    // Track if spawn structure has been placed this session
    private static boolean spawnStructurePlaced = false;

    /**
     * Place a trader structure near world spawn point.
     * Called once when first player joins.
     * Uses a simple fixed-direction approach to avoid chunk generation issues.
     */
    private static void placeSpawnStructure(ServerWorld world) {
        if (spawnStructurePlaced) return;
        spawnStructurePlaced = true;

        BlockPos spawnPos = world.getSpawnPos();

        // Check if we already placed a spawn structure in this world
        PlacedStructureTracker tracker = PlacedStructureTracker.get(world);
        if (tracker.hasStructureNear(spawnPos)) {
            STNTraders.LOGGER.debug("Spawn structure already exists");
            return; // Already placed
        }

        // Select a structure (use gamestage 0 for spawn structure)
        TraderStructure structure = TraderStructureRegistry.selectForGamestage(world.getRandom(), 0);
        if (structure == null) {
            STNTraders.LOGGER.warn("No trader structures registered for spawn placement!");
            return;
        }

        // Simple approach: place in a fixed direction from spawn (positive X)
        // This avoids the 16-attempt search loop that was causing lag
        BlockPos targetPos = spawnPos.add(SPAWN_STRUCTURE_DISTANCE, 0, 0);
        ChunkPos targetChunk = new ChunkPos(targetPos);

        // Only proceed if the target chunk is already loaded
        if (!world.getChunkManager().isChunkLoaded(targetChunk.x, targetChunk.z)) {
            STNTraders.LOGGER.debug("Spawn structure target chunk not loaded, skipping");
            return;
        }

        // Get the surface Y at the target position
        int y = world.getTopY(net.minecraft.world.Heightmap.Type.WORLD_SURFACE, targetPos.getX(), targetPos.getZ());
        BlockPos structurePos = targetPos.withY(y);

        // Place the structure
        boolean success = TraderStructurePlacer.place(world, structurePos, structure);

        if (success) {
            tracker.markPlaced(spawnPos, structure.id());
            STNTraders.LOGGER.info("Placed spawn trader structure '{}' at {} (spawn at {})",
                structure.id(), structurePos.toShortString(), spawnPos.toShortString());
        } else {
            STNTraders.LOGGER.debug("Failed to place spawn trader structure at {}", structurePos.toShortString());
        }
    }

    /**
     * Process a chunk to check for nearby villages and place structures.
     */
    private static void processChunkForStructures(ServerWorld world, ChunkPos chunkPos) {
        BlockPos chunkCenter = chunkPos.getCenterAtY(world.getSeaLevel());

        // Check for village meeting point (bell) nearby
        PointOfInterestStorage poiStorage = world.getPointOfInterestStorage();

        // Get the POI type for village meeting points (bells)
        RegistryKey<PointOfInterestType> meetingKey = PointOfInterestTypes.MEETING;

        Optional<BlockPos> villageCenter = poiStorage.getNearestPosition(
            type -> type.matchesKey(meetingKey),
            chunkCenter,
            VILLAGE_SEARCH_RADIUS,
            PointOfInterestStorage.OccupationStatus.ANY
        );

        if (villageCenter.isEmpty()) {
            return; // No village nearby
        }

        BlockPos village = villageCenter.get();

        // Check if we've already placed a structure for this village
        PlacedStructureTracker tracker = PlacedStructureTracker.get(world);
        if (tracker.hasStructureNear(village)) {
            return; // Already placed
        }

        // Get current gamestage for structure selection
        IGamestageProvider provider = STNCore.getGamestageProvider();
        int gamestage = provider != null ? provider.getWorldGamestage() : 0;

        // Select a structure
        TraderStructure structure = TraderStructureRegistry.selectForGamestage(world.getRandom(), gamestage);
        if (structure == null) {
            STNTraders.LOGGER.warn("No trader structures registered!");
            return;
        }

        // Calculate placement position
        BlockPos structurePos = StructurePositionCalculator.calculate(
            world, village, STRUCTURE_DISTANCE_FROM_VILLAGE, structure.dimensions()
        );

        if (structurePos == null) {
            STNTraders.LOGGER.debug("Could not find valid position for trader structure near {}", village.toShortString());
            return;
        }

        // Place the structure
        boolean success = TraderStructurePlacer.place(world, structurePos, structure);

        if (success) {
            // Mark this village as having a structure
            tracker.markPlaced(village, structure.id());
            STNTraders.LOGGER.info("Placed {} near village at {} (structure at {})",
                structure.id(), village.toShortString(), structurePos.toShortString());
        }
    }

    /**
     * Enable or disable structure generation.
     */
    public static void setEnabled(boolean enabled) {
        VillageStructureManager.enabled = enabled;
    }

    /**
     * Check if structure generation is enabled.
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Clear the processed chunks cache (for world reload).
     */
    public static void clearCache() {
        processedChunks.clear();
    }

    /**
     * Force placement of a structure at a position (for commands/testing).
     */
    public static boolean forcePlace(ServerWorld world, BlockPos pos, TraderStructure structure) {
        return TraderStructurePlacer.place(world, pos, structure);
    }
}
