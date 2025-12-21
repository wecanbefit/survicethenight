package com.stn.traders.structure.processor;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Property;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Structure processor that replaces oak wood with biome-appropriate wood types.
 * Build structures with oak - they'll automatically adapt to their biome.
 */
public class BiomeWoodProcessor extends StructureProcessor {

    public static final MapCodec<BiomeWoodProcessor> CODEC = MapCodec.unit(BiomeWoodProcessor::new);

    // Wood type mappings: oak -> biome variant
    private static final Map<Block, WoodSet> WOOD_MAPPINGS = new HashMap<>();

    static {
        // Map oak blocks to their WoodSet for replacement
        WOOD_MAPPINGS.put(Blocks.OAK_LOG, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.OAK_WOOD, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.STRIPPED_OAK_LOG, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.STRIPPED_OAK_WOOD, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.OAK_PLANKS, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.OAK_STAIRS, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.OAK_SLAB, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.OAK_FENCE, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.OAK_FENCE_GATE, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.OAK_DOOR, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.OAK_TRAPDOOR, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.OAK_PRESSURE_PLATE, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.OAK_BUTTON, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.OAK_SIGN, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.OAK_WALL_SIGN, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.OAK_HANGING_SIGN, WoodSet.OAK);
        WOOD_MAPPINGS.put(Blocks.OAK_WALL_HANGING_SIGN, WoodSet.OAK);
    }

    public BiomeWoodProcessor() {
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return STNProcessors.BIOME_WOOD;
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(
            WorldView world,
            BlockPos pos,
            BlockPos pivot,
            StructureTemplate.StructureBlockInfo originalBlockInfo,
            StructureTemplate.StructureBlockInfo currentBlockInfo,
            StructurePlacementData data) {

        BlockState state = currentBlockInfo.state();
        Block block = state.getBlock();

        // Check if this is an oak block we should replace
        WoodSet sourceSet = WOOD_MAPPINGS.get(block);
        if (sourceSet == null) {
            return currentBlockInfo; // Not a replaceable block
        }

        // Get the biome at the target position
        BlockPos targetPos = currentBlockInfo.pos();
        Biome biome = world.getBiome(targetPos).value();

        // Determine target wood type based on biome
        WoodSet targetSet = getWoodSetForBiome(world, targetPos);
        if (targetSet == sourceSet) {
            return currentBlockInfo; // Same wood type, no change needed
        }

        // Replace the block with the biome-appropriate variant
        BlockState newState = replaceWood(state, block, targetSet);
        if (newState == null) {
            return currentBlockInfo;
        }

        return new StructureTemplate.StructureBlockInfo(currentBlockInfo.pos(), newState, currentBlockInfo.nbt());
    }

    private WoodSet getWoodSetForBiome(WorldView world, BlockPos pos) {
        var biomeEntry = world.getBiome(pos);

        // Check biome tags to determine wood type
        if (biomeEntry.isIn(BiomeTags.IS_TAIGA) || biomeEntry.isIn(BiomeTags.IS_MOUNTAIN)) {
            return WoodSet.SPRUCE;
        }
        if (biomeEntry.isIn(BiomeTags.IS_JUNGLE)) {
            return WoodSet.JUNGLE;
        }
        if (biomeEntry.isIn(BiomeTags.IS_SAVANNA)) {
            return WoodSet.ACACIA;
        }
        if (biomeEntry.isIn(BiomeTags.IS_BADLANDS)) {
            return WoodSet.ACACIA; // Badlands use acacia-like desert theme
        }

        // Check specific biomes by registry key
        var biomeKey = biomeEntry.getKey();
        if (biomeKey.isPresent()) {
            String path = biomeKey.get().getValue().getPath();

            if (path.contains("birch")) {
                return WoodSet.BIRCH;
            }
            if (path.contains("dark_forest")) {
                return WoodSet.DARK_OAK;
            }
            if (path.contains("cherry")) {
                return WoodSet.CHERRY;
            }
            if (path.contains("mangrove")) {
                return WoodSet.MANGROVE;
            }
            if (path.contains("snowy") || path.contains("frozen") || path.contains("ice")) {
                return WoodSet.SPRUCE;
            }
            if (path.contains("desert")) {
                return WoodSet.ACACIA;
            }
        }

        // Default to oak
        return WoodSet.OAK;
    }

    @Nullable
    private BlockState replaceWood(BlockState original, Block block, WoodSet target) {
        // Get the equivalent block from the target wood set
        Block replacement = target.getEquivalent(block);
        if (replacement == null || replacement == block) {
            return null;
        }

        // Start with default state of replacement block
        BlockState newState = replacement.getDefaultState();

        // Copy all properties that exist in both blocks (facing, half, shape, etc.)
        for (Property<?> property : original.getProperties()) {
            if (newState.contains(property)) {
                newState = copyProperty(original, newState, property);
            }
        }

        return newState;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, Property<T> property) {
        return to.with(property, from.get(property));
    }

    /**
     * Represents a complete set of wood blocks for a tree type.
     */
    private enum WoodSet {
        OAK(Blocks.OAK_LOG, Blocks.OAK_WOOD, Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_OAK_WOOD,
            Blocks.OAK_PLANKS, Blocks.OAK_STAIRS, Blocks.OAK_SLAB, Blocks.OAK_FENCE, Blocks.OAK_FENCE_GATE,
            Blocks.OAK_DOOR, Blocks.OAK_TRAPDOOR, Blocks.OAK_PRESSURE_PLATE, Blocks.OAK_BUTTON,
            Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN),

        SPRUCE(Blocks.SPRUCE_LOG, Blocks.SPRUCE_WOOD, Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_WOOD,
            Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_STAIRS, Blocks.SPRUCE_SLAB, Blocks.SPRUCE_FENCE, Blocks.SPRUCE_FENCE_GATE,
            Blocks.SPRUCE_DOOR, Blocks.SPRUCE_TRAPDOOR, Blocks.SPRUCE_PRESSURE_PLATE, Blocks.SPRUCE_BUTTON,
            Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN),

        BIRCH(Blocks.BIRCH_LOG, Blocks.BIRCH_WOOD, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_BIRCH_WOOD,
            Blocks.BIRCH_PLANKS, Blocks.BIRCH_STAIRS, Blocks.BIRCH_SLAB, Blocks.BIRCH_FENCE, Blocks.BIRCH_FENCE_GATE,
            Blocks.BIRCH_DOOR, Blocks.BIRCH_TRAPDOOR, Blocks.BIRCH_PRESSURE_PLATE, Blocks.BIRCH_BUTTON,
            Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN),

        JUNGLE(Blocks.JUNGLE_LOG, Blocks.JUNGLE_WOOD, Blocks.STRIPPED_JUNGLE_LOG, Blocks.STRIPPED_JUNGLE_WOOD,
            Blocks.JUNGLE_PLANKS, Blocks.JUNGLE_STAIRS, Blocks.JUNGLE_SLAB, Blocks.JUNGLE_FENCE, Blocks.JUNGLE_FENCE_GATE,
            Blocks.JUNGLE_DOOR, Blocks.JUNGLE_TRAPDOOR, Blocks.JUNGLE_PRESSURE_PLATE, Blocks.JUNGLE_BUTTON,
            Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN),

        ACACIA(Blocks.ACACIA_LOG, Blocks.ACACIA_WOOD, Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_ACACIA_WOOD,
            Blocks.ACACIA_PLANKS, Blocks.ACACIA_STAIRS, Blocks.ACACIA_SLAB, Blocks.ACACIA_FENCE, Blocks.ACACIA_FENCE_GATE,
            Blocks.ACACIA_DOOR, Blocks.ACACIA_TRAPDOOR, Blocks.ACACIA_PRESSURE_PLATE, Blocks.ACACIA_BUTTON,
            Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.ACACIA_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN),

        DARK_OAK(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_WOOD, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.STRIPPED_DARK_OAK_WOOD,
            Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_STAIRS, Blocks.DARK_OAK_SLAB, Blocks.DARK_OAK_FENCE, Blocks.DARK_OAK_FENCE_GATE,
            Blocks.DARK_OAK_DOOR, Blocks.DARK_OAK_TRAPDOOR, Blocks.DARK_OAK_PRESSURE_PLATE, Blocks.DARK_OAK_BUTTON,
            Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.DARK_OAK_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN),

        MANGROVE(Blocks.MANGROVE_LOG, Blocks.MANGROVE_WOOD, Blocks.STRIPPED_MANGROVE_LOG, Blocks.STRIPPED_MANGROVE_WOOD,
            Blocks.MANGROVE_PLANKS, Blocks.MANGROVE_STAIRS, Blocks.MANGROVE_SLAB, Blocks.MANGROVE_FENCE, Blocks.MANGROVE_FENCE_GATE,
            Blocks.MANGROVE_DOOR, Blocks.MANGROVE_TRAPDOOR, Blocks.MANGROVE_PRESSURE_PLATE, Blocks.MANGROVE_BUTTON,
            Blocks.MANGROVE_SIGN, Blocks.MANGROVE_WALL_SIGN, Blocks.MANGROVE_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN),

        CHERRY(Blocks.CHERRY_LOG, Blocks.CHERRY_WOOD, Blocks.STRIPPED_CHERRY_LOG, Blocks.STRIPPED_CHERRY_WOOD,
            Blocks.CHERRY_PLANKS, Blocks.CHERRY_STAIRS, Blocks.CHERRY_SLAB, Blocks.CHERRY_FENCE, Blocks.CHERRY_FENCE_GATE,
            Blocks.CHERRY_DOOR, Blocks.CHERRY_TRAPDOOR, Blocks.CHERRY_PRESSURE_PLATE, Blocks.CHERRY_BUTTON,
            Blocks.CHERRY_SIGN, Blocks.CHERRY_WALL_SIGN, Blocks.CHERRY_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN);

        private final Map<Block, Block> equivalents = new HashMap<>();

        WoodSet(Block log, Block wood, Block strippedLog, Block strippedWood,
                Block planks, Block stairs, Block slab, Block fence, Block fenceGate,
                Block door, Block trapdoor, Block pressurePlate, Block button,
                Block sign, Block wallSign, Block hangingSign, Block wallHangingSign) {

            // Map by index position in the oak set
            equivalents.put(Blocks.OAK_LOG, log);
            equivalents.put(Blocks.OAK_WOOD, wood);
            equivalents.put(Blocks.STRIPPED_OAK_LOG, strippedLog);
            equivalents.put(Blocks.STRIPPED_OAK_WOOD, strippedWood);
            equivalents.put(Blocks.OAK_PLANKS, planks);
            equivalents.put(Blocks.OAK_STAIRS, stairs);
            equivalents.put(Blocks.OAK_SLAB, slab);
            equivalents.put(Blocks.OAK_FENCE, fence);
            equivalents.put(Blocks.OAK_FENCE_GATE, fenceGate);
            equivalents.put(Blocks.OAK_DOOR, door);
            equivalents.put(Blocks.OAK_TRAPDOOR, trapdoor);
            equivalents.put(Blocks.OAK_PRESSURE_PLATE, pressurePlate);
            equivalents.put(Blocks.OAK_BUTTON, button);
            equivalents.put(Blocks.OAK_SIGN, sign);
            equivalents.put(Blocks.OAK_WALL_SIGN, wallSign);
            equivalents.put(Blocks.OAK_HANGING_SIGN, hangingSign);
            equivalents.put(Blocks.OAK_WALL_HANGING_SIGN, wallHangingSign);
        }

        public Block getEquivalent(Block oakBlock) {
            return equivalents.get(oakBlock);
        }
    }
}
