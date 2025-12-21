package com.stn.repair.registry;

import com.stn.repair.STNRepair;
import com.stn.repair.item.HammerItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Function;

/**
 * Registry for all STN Repair items.
 * Includes hammer tools for repairing fortified blocks.
 */
public class STNItems {

    // Hammers (tiered repair tools)
    public static final Item WOODEN_HAMMER = registerItem("wooden_hammer",
        settings -> new HammerItem(HammerItem.HammerTier.WOOD, settings));

    public static final Item STONE_HAMMER = registerItem("stone_hammer",
        settings -> new HammerItem(HammerItem.HammerTier.STONE, settings));

    public static final Item IRON_HAMMER = registerItem("iron_hammer",
        settings -> new HammerItem(HammerItem.HammerTier.IRON, settings));

    public static final Item DIAMOND_HAMMER = registerItem("diamond_hammer",
        settings -> new HammerItem(HammerItem.HammerTier.DIAMOND, settings));

    public static final Item NETHERITE_HAMMER = registerItem("netherite_hammer",
        settings -> new HammerItem(HammerItem.HammerTier.NETHERITE, settings.fireproof()));

    // Creative Tab
    public static final RegistryKey<ItemGroup> REPAIR_GROUP_KEY = RegistryKey.of(
        RegistryKeys.ITEM_GROUP,
        Identifier.of(STNRepair.MOD_ID, "repair")
    );

    public static final ItemGroup REPAIR_GROUP = FabricItemGroup.builder()
        .icon(() -> new ItemStack(IRON_HAMMER))
        .displayName(Text.translatable("itemGroup.stn_repair.repair"))
        .build();

    private static Item registerItem(String name, Function<Item.Settings, Item> itemFactory) {
        Identifier id = Identifier.of(STNRepair.MOD_ID, name);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);

        // Create settings with registry key set
        Item.Settings settings = new Item.Settings().registryKey(itemKey);

        // Create the item using the factory
        Item item = itemFactory.apply(settings);

        // Register the item
        return Registry.register(Registries.ITEM, itemKey, item);
    }

    public static void register() {
        STNRepair.LOGGER.info("Registering STN Repair items...");

        // Register the creative tab
        Registry.register(Registries.ITEM_GROUP, REPAIR_GROUP_KEY, REPAIR_GROUP);

        // Add items to creative tab
        ItemGroupEvents.modifyEntriesEvent(REPAIR_GROUP_KEY).register(content -> {
            // Hammers
            content.add(WOODEN_HAMMER);
            content.add(STONE_HAMMER);
            content.add(IRON_HAMMER);
            content.add(DIAMOND_HAMMER);
            content.add(NETHERITE_HAMMER);
        });

        STNRepair.LOGGER.info("STN Repair items registered!");
    }
}
