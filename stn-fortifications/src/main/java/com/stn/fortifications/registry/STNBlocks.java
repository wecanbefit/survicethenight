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

/**
 * Registry for all STN Fortifications blocks.
 * Includes defensive blocks for base protection.
 */
public class STNBlocks {

    // Spike Blocks
    public static final Block WOODEN_SPIKES = registerBlock("wooden_spikes",
        new SpikeBlock(
            AbstractBlock.Settings.create()
                .mapColor(MapColor.OAK_TAN)
                .strength(1.0f, 2.0f)
                .sounds(BlockSoundGroup.WOOD)
                .nonOpaque(),
            2.0f,  // damage
            50     // durability hits
        ));

    public static final Block IRON_SPIKES = registerBlock("iron_spikes",
        new SpikeBlock(
            AbstractBlock.Settings.create()
                .mapColor(MapColor.IRON_GRAY)
                .strength(3.0f, 6.0f)
                .sounds(BlockSoundGroup.METAL)
                .nonOpaque()
                .requiresTool(),
            4.0f,  // damage
            200    // durability hits
        ));

    public static final Block REINFORCED_SPIKES = registerBlock("reinforced_spikes",
        new SpikeBlock(
            AbstractBlock.Settings.create()
                .mapColor(MapColor.GRAY)
                .strength(5.0f, 12.0f)
                .sounds(BlockSoundGroup.METAL)
                .nonOpaque()
                .requiresTool(),
            6.0f,  // damage
            500    // durability hits
        ));

    // Reinforced Walls
    public static final Block REINFORCED_WOOD = registerBlock("reinforced_wood",
        new ReinforcedBlock(
            AbstractBlock.Settings.create()
                .mapColor(MapColor.OAK_TAN)
                .strength(4.0f, 12.0f)  // 2x blast resistance
                .sounds(BlockSoundGroup.WOOD)
                .requiresTool(),
            false  // can be broken by zombies
        ));

    public static final Block REINFORCED_COBBLESTONE = registerBlock("reinforced_cobblestone",
        new ReinforcedBlock(
            AbstractBlock.Settings.create()
                .mapColor(MapColor.STONE_GRAY)
                .strength(6.0f, 18.0f)  // 3x blast resistance
                .sounds(BlockSoundGroup.STONE)
                .requiresTool(),
            false  // can be broken by zombies (slowly)
        ));

    public static final Block REINFORCED_IRON = registerBlock("reinforced_iron",
        new ReinforcedBlock(
            AbstractBlock.Settings.create()
                .mapColor(MapColor.IRON_GRAY)
                .strength(8.0f, 30.0f)
                .sounds(BlockSoundGroup.METAL)
                .requiresTool(),
            true   // cannot be broken by zombies
        ));

    public static final Block STEEL_BLOCK = registerBlock("steel_block",
        new ReinforcedBlock(
            AbstractBlock.Settings.create()
                .mapColor(MapColor.GRAY)
                .strength(10.0f, 50.0f)  // highest durability
                .sounds(BlockSoundGroup.METAL)
                .requiresTool(),
            true   // cannot be broken by zombies
        ));

    // Barbed Wire
    public static final Block BARBED_WIRE = registerBlock("barbed_wire",
        new BarbedWireBlock(
            AbstractBlock.Settings.create()
                .mapColor(MapColor.IRON_GRAY)
                .strength(0.5f, 1.0f)
                .sounds(BlockSoundGroup.CHAIN)
                .nonOpaque()
                .noCollision()
        ));

    // Electric Fence
    public static final Block ELECTRIC_FENCE = registerBlock("electric_fence",
        new ElectricFenceBlock(
            AbstractBlock.Settings.create()
                .mapColor(MapColor.YELLOW)
                .strength(2.0f, 4.0f)
                .sounds(BlockSoundGroup.METAL)
                .nonOpaque()
                .luminance(state -> state.get(ElectricFenceBlock.POWERED) ? 7 : 0)
        ));

    // Motion Sensor
    public static final Block MOTION_SENSOR = registerBlock("motion_sensor",
        new MotionSensorBlock(
            AbstractBlock.Settings.create()
                .mapColor(MapColor.IRON_GRAY)
                .strength(2.0f, 4.0f)
                .sounds(BlockSoundGroup.METAL)
                .nonOpaque()
                .luminance(state -> state.get(MotionSensorBlock.POWERED) ? 10 : 0)
        ));

    private static Block registerBlock(String name, Block block) {
        // Register the block
        Block registeredBlock = Registry.register(
            Registries.BLOCK,
            Identifier.of(STNFortifications.MOD_ID, name),
            block
        );

        // Register the block item
        Registry.register(
            Registries.ITEM,
            Identifier.of(STNFortifications.MOD_ID, name),
            new BlockItem(registeredBlock, new Item.Settings())
        );

        return registeredBlock;
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
