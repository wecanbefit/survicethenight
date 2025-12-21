package com.stn.traders.structure;

import com.stn.traders.STNTraders;
import com.stn.traders.entity.SurvivalTraderEntity;
import com.stn.traders.protection.ProtectedRegion;
import com.stn.traders.protection.TraderProtectionState;
import com.stn.traders.registry.STNTraderEntities;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.Optional;
import java.util.UUID;

/**
 * Handles the actual placement of trader structures in the world.
 * Uses Minecraft's StructureTemplate system to load and place NBT structures.
 */
public class TraderStructurePlacer {

    /**
     * Place a trader structure at the specified position.
     *
     * @param world the server world
     * @param pos position to place at (ground level)
     * @param structure the structure definition
     * @return true if placement was successful
     */
    public static boolean place(ServerWorld world, BlockPos pos, TraderStructure structure) {
        StructureTemplateManager templateManager = world.getStructureTemplateManager();

        // Try to load the structure template
        Optional<StructureTemplate> templateOpt = templateManager.getTemplate(structure.templateId());

        BlockPos actualPos;
        Vec3i actualSize;

        if (templateOpt.isPresent()) {
            // Use the NBT template
            StructureTemplate template = templateOpt.get();
            actualSize = template.getSize();

            // Configure placement with random rotation
            StructurePlacementData placementData = new StructurePlacementData()
                .setMirror(BlockMirror.NONE)
                .setRotation(getRandomRotation(world))
                .setIgnoreEntities(false);

            // Adjust position to center structure
            actualPos = pos.add(
                -actualSize.getX() / 2,
                0,
                -actualSize.getZ() / 2
            );

            // Place the structure
            boolean success = template.place(
                world,
                actualPos,
                actualPos,
                placementData,
                world.getRandom(),
                Block.NOTIFY_ALL
            );

            if (!success) {
                STNTraders.LOGGER.warn("Failed to place structure template: {}", structure.templateId());
                return false;
            }
        } else {
            // No template found - create a simple placeholder structure
            STNTraders.LOGGER.info("No template found for {}, creating placeholder", structure.templateId());
            actualSize = structure.dimensions();
            actualPos = pos.add(-actualSize.getX() / 2, 0, -actualSize.getZ() / 2);
            createPlaceholderStructure(world, actualPos, structure);
        }

        // Calculate structure bounds
        BlockPos minCorner = actualPos;
        BlockPos maxCorner = actualPos.add(actualSize.getX() - 1, actualSize.getY() - 1, actualSize.getZ() - 1);

        // Register protection for the structure
        UUID protectionId = UUID.randomUUID();
        BlockBox bounds = BlockBox.create(minCorner, maxCorner);
        ProtectedRegion region = new ProtectedRegion(
            protectionId,
            structure.type().name().toLowerCase(),
            bounds,
            world.getTime()
        );
        TraderProtectionState.get(world).addRegion(region);

        // Spawn the trader if configured
        if (structure.spawnsMerchant()) {
            spawnTrader(world, pos.up(), structure);
        }

        STNTraders.LOGGER.info("Placed trader structure '{}' at {} (protected: {})",
            structure.id(), pos.toShortString(), protectionId);

        return true;
    }

    /**
     * Create a simple placeholder structure when no NBT template exists.
     */
    private static void createPlaceholderStructure(ServerWorld world, BlockPos pos, TraderStructure structure) {
        Vec3i size = structure.dimensions();
        int width = size.getX();
        int height = size.getY();
        int depth = size.getZ();

        // Simple box structure
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                // Floor
                world.setBlockState(pos.add(x, 0, z), Blocks.OAK_PLANKS.getDefaultState(), Block.NOTIFY_ALL);

                // Walls (only on edges)
                if (x == 0 || x == width - 1 || z == 0 || z == depth - 1) {
                    for (int y = 1; y < height - 1; y++) {
                        // Leave gap for door
                        if (x == width / 2 && z == 0 && y < 3) {
                            continue;
                        }
                        world.setBlockState(pos.add(x, y, z), Blocks.OAK_LOG.getDefaultState(), Block.NOTIFY_ALL);
                    }
                }

                // Roof
                world.setBlockState(pos.add(x, height - 1, z), Blocks.OAK_PLANKS.getDefaultState(), Block.NOTIFY_ALL);
            }
        }

        // Add a light source
        BlockPos center = pos.add(width / 2, 2, depth / 2);
        world.setBlockState(center, Blocks.LANTERN.getDefaultState(), Block.NOTIFY_ALL);
    }

    /**
     * Spawn a trader entity inside the structure.
     */
    private static void spawnTrader(ServerWorld world, BlockPos pos, TraderStructure structure) {
        SurvivalTraderEntity trader = STNTraderEntities.SURVIVAL_TRADER.create(world, SpawnReason.STRUCTURE);
        if (trader != null) {
            // Position in center of structure, slightly above ground
            trader.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            trader.setYaw(world.getRandom().nextFloat() * 360f);
            world.spawnEntity(trader);
            STNTraders.LOGGER.debug("Spawned trader at {}", pos.toShortString());
        }
    }

    /**
     * Get a random rotation for structure variety.
     */
    private static BlockRotation getRandomRotation(ServerWorld world) {
        return BlockRotation.values()[world.getRandom().nextInt(4)];
    }
}
