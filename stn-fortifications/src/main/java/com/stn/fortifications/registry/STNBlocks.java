package com.stn.fortifications.registry;

import com.stn.fortifications.STNFortifications;
import com.stn.fortifications.block.BarbedWireBlock;
import com.stn.fortifications.block.ElectricFenceBlock;
import com.stn.fortifications.block.MotionSensorBlock;
import com.stn.fortifications.block.ReinforcedBlock;
import com.stn.fortifications.block.SpikeBlock;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Function;

/**
 * Registry for all STN Fortifications blocks.
 * Includes defensive blocks for base protection.
 */
public class STNBlocks {

    // Spike Blocks
    public static final Block BAMBOO_SPIKES = registerBlock("bamboo_spikes",
        settings -> new SpikeBlock(settings, 1.0f, 25),
        AbstractBlock.Settings.create()
            .mapColor(MapColor.PALE_GREEN)
            .strength(0.5f, 1.0f)
            .sounds(BlockSoundGroup.BAMBOO)
            .nonOpaque());

    public static final Block WOODEN_SPIKES = registerBlock("wooden_spikes",
        settings -> new SpikeBlock(settings, 2.0f, 50),
        AbstractBlock.Settings.create()
            .mapColor(MapColor.OAK_TAN)
            .strength(1.0f, 2.0f)
            .sounds(BlockSoundGroup.WOOD)
            .nonOpaque());

    public static final Block IRON_SPIKES = registerBlock("iron_spikes",
        settings -> new SpikeBlock(settings, 4.0f, 200),
        AbstractBlock.Settings.create()
            .mapColor(MapColor.IRON_GRAY)
            .strength(3.0f, 6.0f)
            .sounds(BlockSoundGroup.METAL)
            .nonOpaque()
            .requiresTool());

    public static final Block REINFORCED_SPIKES = registerBlock("reinforced_spikes",
        settings -> new SpikeBlock(settings, 6.0f, 500),
        AbstractBlock.Settings.create()
            .mapColor(MapColor.GRAY)
            .strength(5.0f, 12.0f)
            .sounds(BlockSoundGroup.METAL)
            .nonOpaque()
            .requiresTool());

    // Reinforced Walls
    public static final Block REINFORCED_WOOD = registerBlock("reinforced_wood",
        settings -> new ReinforcedBlock(settings, false),
        AbstractBlock.Settings.create()
            .mapColor(MapColor.OAK_TAN)
            .strength(4.0f, 12.0f)
            .sounds(BlockSoundGroup.WOOD)
            .requiresTool());

    public static final Block REINFORCED_COBBLESTONE = registerBlock("reinforced_cobblestone",
        settings -> new ReinforcedBlock(settings, false),
        AbstractBlock.Settings.create()
            .mapColor(MapColor.STONE_GRAY)
            .strength(6.0f, 18.0f)
            .sounds(BlockSoundGroup.STONE)
            .requiresTool());

    public static final Block REINFORCED_IRON = registerBlock("reinforced_iron",
        settings -> new ReinforcedBlock(settings, true),
        AbstractBlock.Settings.create()
            .mapColor(MapColor.IRON_GRAY)
            .strength(8.0f, 30.0f)
            .sounds(BlockSoundGroup.METAL)
            .requiresTool());

    public static final Block STEEL_BLOCK = registerBlock("steel_block",
        settings -> new ReinforcedBlock(settings, true),
        AbstractBlock.Settings.create()
            .mapColor(MapColor.GRAY)
            .strength(10.0f, 50.0f)
            .sounds(BlockSoundGroup.METAL)
            .requiresTool());

    // Barbed Wire
    public static final Block BARBED_WIRE = registerBlock("barbed_wire",
        BarbedWireBlock::new,
        AbstractBlock.Settings.create()
            .mapColor(MapColor.IRON_GRAY)
            .strength(0.5f, 1.0f)
            .sounds(BlockSoundGroup.CHAIN)
            .nonOpaque()
            .noCollision());

    // Electric Fence
    public static final Block ELECTRIC_FENCE = registerBlock("electric_fence",
        ElectricFenceBlock::new,
        AbstractBlock.Settings.create()
            .mapColor(MapColor.YELLOW)
            .strength(2.0f, 4.0f)
            .sounds(BlockSoundGroup.METAL)
            .nonOpaque()
            .luminance(state -> state.get(ElectricFenceBlock.POWERED) ? 7 : 0));

    // Motion Sensor
    public static final Block MOTION_SENSOR = registerBlock("motion_sensor",
        MotionSensorBlock::new,
        AbstractBlock.Settings.create()
            .mapColor(MapColor.IRON_GRAY)
            .strength(2.0f, 4.0f)
            .sounds(BlockSoundGroup.METAL)
            .nonOpaque()
            .luminance(state -> state.get(MotionSensorBlock.POWERED) ? 10 : 0));

    /**
     * Register a block with its item.
     * In MC 1.21+, registry key must be set on settings before block construction.
     */
    private static <T extends Block> T registerBlock(String name, Function<AbstractBlock.Settings, T> factory, AbstractBlock.Settings settings) {
        Identifier id = Identifier.of(STNFortifications.MOD_ID, name);

        // Set registry key on block settings BEFORE creating block
        RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
        settings = settings.registryKey(blockKey);

        // Create and register block
        T block = factory.apply(settings);
        Registry.register(Registries.BLOCK, id, block);

        // Register block item
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
        BlockItem blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
        Registry.register(Registries.ITEM, id, blockItem);

        return block;
    }

    // Creative Tab (defined after blocks to avoid forward reference)
    public static final RegistryKey<ItemGroup> FORTIFICATIONS_GROUP_KEY = RegistryKey.of(
        RegistryKeys.ITEM_GROUP,
        Identifier.of(STNFortifications.MOD_ID, "fortifications")
    );

    public static final ItemGroup FORTIFICATIONS_GROUP = FabricItemGroup.builder()
        .icon(() -> new ItemStack(BARBED_WIRE))
        .displayName(Text.translatable("itemGroup.stn_fortifications.fortifications"))
        .build();

    public static void register() {
        STNFortifications.LOGGER.info("Registering STN Fortifications blocks...");

        // Register the creative tab
        Registry.register(Registries.ITEM_GROUP, FORTIFICATIONS_GROUP_KEY, FORTIFICATIONS_GROUP);

        // Add blocks to creative tab
        ItemGroupEvents.modifyEntriesEvent(FORTIFICATIONS_GROUP_KEY).register(content -> {
            // Spikes
            content.add(BAMBOO_SPIKES);
            content.add(WOODEN_SPIKES);
            content.add(IRON_SPIKES);
            content.add(REINFORCED_SPIKES);

            // Reinforced walls
            content.add(REINFORCED_WOOD);
            content.add(REINFORCED_COBBLESTONE);
            content.add(REINFORCED_IRON);
            content.add(STEEL_BLOCK);

            // Advanced defenses
            content.add(BARBED_WIRE);
            content.add(ELECTRIC_FENCE);
            content.add(MOTION_SENSOR);
        });

        STNFortifications.LOGGER.info("STN Fortifications blocks registered!");
    }
}
