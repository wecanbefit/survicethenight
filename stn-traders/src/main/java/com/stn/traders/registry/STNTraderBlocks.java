package com.stn.traders.registry;

import com.stn.traders.STNTraders;
import com.stn.traders.block.JobsBoardBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.function.Function;

/**
 * Registry for STN trader blocks.
 */
public class STNTraderBlocks {

    public static final JobsBoardBlock JOBS_BOARD = registerBlock("jobs_board",
        settings -> new JobsBoardBlock(settings),
        AbstractBlock.Settings.copy(Blocks.OAK_SIGN)
            .strength(2.0f, 3.0f)
            .nonOpaque());

    /**
     * Register a block with its item.
     * In MC 1.21+, registry key must be set on settings before block construction.
     */
    private static <T extends Block> T registerBlock(String name, Function<AbstractBlock.Settings, T> factory, AbstractBlock.Settings settings) {
        Identifier id = Identifier.of(STNTraders.MOD_ID, name);

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

    /**
     * Initialize all blocks (triggers static initialization).
     */
    public static void register() {
        STNTraders.LOGGER.info("Registering STN trader blocks...");

        // Add to creative tab
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(JOBS_BOARD);
        });

        STNTraders.LOGGER.info("Registered trader blocks");
    }
}
