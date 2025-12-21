package com.stn.traders.protection;

import com.stn.core.STNCore;
import com.stn.core.api.IBlockProtectionProvider;
import com.stn.traders.STNTraders;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Manages trader structure protection.
 * Implements IBlockProtectionProvider for cross-mod integration.
 */
public class TraderProtectionManager implements IBlockProtectionProvider {

    private static TraderProtectionManager INSTANCE;

    private TraderProtectionManager() {}

    public static TraderProtectionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TraderProtectionManager();
        }
        return INSTANCE;
    }

    /**
     * Initialize the protection system and register with STNCore.
     */
    public static void init() {
        STNCore.registerBlockProtectionProvider(getInstance());
        STNTraders.LOGGER.info("Trader protection system initialized");
    }

    // === IBlockProtectionProvider Implementation ===

    private static final Identifier TRADER_STRUCTURE_ID = Identifier.of(STNTraders.MOD_ID, "trader_outpost");

    @Override
    public boolean isProtected(World world, BlockPos pos) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return false; // Client-side: assume not protected
        }

        // Check legacy region-based protection
        if (TraderProtectionState.get(serverWorld).isProtected(pos)) {
            return true;
        }

        // Check structure-based protection (datapack worldgen)
        return isInTraderStructure(serverWorld, pos);
    }

    /**
     * Check if a position is inside a trader_outpost structure.
     */
    private boolean isInTraderStructure(ServerWorld world, BlockPos pos) {
        var structureAccessor = world.getStructureAccessor();

        StructureStart start = structureAccessor.getStructureContaining(pos, entry -> {
            var key = entry.getKey();
            return key.isPresent() && key.get().getValue().equals(TRADER_STRUCTURE_ID);
        });

        return start.hasChildren();
    }

    @Override
    @Nullable
    public String getProtectionType(World world, BlockPos pos) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return null;
        }
        ProtectedRegion region = TraderProtectionState.get(serverWorld).getRegionAt(pos);
        return region != null ? "Trader: " + region.structureType() : null;
    }

    // === Protection Management ===

    /**
     * Protect a new trader structure region.
     * @param world the server world
     * @param corner1 first corner of the region
     * @param corner2 second corner of the region
     * @param structureType type of structure (e.g., "booth", "shop", "bunker")
     * @return UUID of the created protection region
     */
    public UUID protectStructure(ServerWorld world, BlockPos corner1, BlockPos corner2, String structureType) {
        UUID id = UUID.randomUUID();
        BlockBox bounds = BlockBox.create(corner1, corner2);
        ProtectedRegion region = new ProtectedRegion(id, structureType, bounds, world.getTime());

        TraderProtectionState.get(world).addRegion(region);
        STNTraders.LOGGER.info("Protected trader structure '{}' at {} - {} (ID: {})",
            structureType, corner1.toShortString(), corner2.toShortString(), id);

        return id;
    }

    /**
     * Remove protection from a structure.
     * @param world the server world
     * @param structureId UUID of the protection to remove
     */
    public void unprotectStructure(ServerWorld world, UUID structureId) {
        TraderProtectionState.get(world).removeRegion(structureId);
        STNTraders.LOGGER.info("Removed protection from structure {}", structureId);
    }

    /**
     * Check if a position is protected (convenience method).
     */
    public boolean isProtectedAt(ServerWorld world, BlockPos pos) {
        return TraderProtectionState.get(world).isProtected(pos);
    }

    /**
     * Get the region at a position.
     */
    @Nullable
    public ProtectedRegion getRegionAt(ServerWorld world, BlockPos pos) {
        return TraderProtectionState.get(world).getRegionAt(pos);
    }

    /**
     * Get total protected region count.
     */
    public int getRegionCount(ServerWorld world) {
        return TraderProtectionState.get(world).getRegionCount();
    }
}
