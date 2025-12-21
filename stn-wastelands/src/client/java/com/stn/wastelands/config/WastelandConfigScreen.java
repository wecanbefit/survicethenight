package com.stn.wastelands.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Cloth Config screen for Wasteland configuration.
 */
public class WastelandConfigScreen {

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("Wastelands Configuration"))
                .setSavingRunnable(WastelandConfigScreen::saveConfig);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // Village Zombification Category
        ConfigCategory villageCategory = builder.getOrCreateCategory(Text.literal("Village Zombification"));

        villageCategory.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Enable Village Zombification"), WastelandConfig.enableVillageZombification)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Replace all villagers with zombies when villages spawn"))
                .setSaveConsumer(value -> WastelandConfig.enableVillageZombification = value)
                .build());

        villageCategory.addEntry(entryBuilder
                .startFloatField(Text.literal("Zombie Villager Chance"), WastelandConfig.zombieVillagerChance)
                .setDefaultValue(0.6f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setTooltip(Text.literal("Chance for a villager to become a zombie villager instead of regular zombie (0.0 to 1.0)"))
                .setSaveConsumer(value -> WastelandConfig.zombieVillagerChance = value)
                .build());

        // Loot Enhancement Category
        ConfigCategory lootCategory = builder.getOrCreateCategory(Text.literal("Loot Enhancement"));

        lootCategory.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Enable Enhanced Loot"), WastelandConfig.enableEnhancedLoot)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Add bonus loot to village chests based on game stage and distance"))
                .setSaveConsumer(value -> WastelandConfig.enableEnhancedLoot = value)
                .build());

        lootCategory.addEntry(entryBuilder
                .startIntField(Text.literal("Minimum Gamestage for Bonus Loot"), WastelandConfig.minimumGamestageForBonusLoot)
                .setDefaultValue(10)
                .setMin(0)
                .setMax(100)
                .setTooltip(Text.literal("Minimum gamestage required before bonus loot starts appearing"))
                .setSaveConsumer(value -> WastelandConfig.minimumGamestageForBonusLoot = value)
                .build());

        lootCategory.addEntry(entryBuilder
                .startFloatField(Text.literal("Max Loot Quality Multiplier"), WastelandConfig.maxLootQualityMultiplier)
                .setDefaultValue(2.5f)
                .setMin(1.0f)
                .setMax(5.0f)
                .setTooltip(Text.literal("Maximum multiplier for loot quality (based on game stage + distance)"))
                .setSaveConsumer(value -> WastelandConfig.maxLootQualityMultiplier = value)
                .build());

        lootCategory.addEntry(entryBuilder
                .startDoubleField(Text.literal("Max Distance for Bonus (blocks)"), WastelandConfig.maxDistanceForBonus)
                .setDefaultValue(10000.0)
                .setMin(1000.0)
                .setMax(50000.0)
                .setTooltip(Text.literal("Distance from spawn where maximum distance bonus is reached"))
                .setSaveConsumer(value -> WastelandConfig.maxDistanceForBonus = value)
                .build());

        lootCategory.addEntry(entryBuilder
                .startBooleanToggle(Text.literal("Enhanced Enchantments"), WastelandConfig.enableEnhancedEnchantments)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Enable higher level enchantments on loot items"))
                .setSaveConsumer(value -> WastelandConfig.enableEnhancedEnchantments = value)
                .build());

        lootCategory.addEntry(entryBuilder
                .startFloatField(Text.literal("Rare Loot Chance"), WastelandConfig.rareLootChance)
                .setDefaultValue(0.15f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setTooltip(Text.literal("Chance for finding rare loot (enchanted golden apples, netherite, etc.)"))
                .setSaveConsumer(value -> WastelandConfig.rareLootChance = value)
                .build());

        // Extra Zombies Category
        ConfigCategory extrasCategory = builder.getOrCreateCategory(Text.literal("Extra Zombies"));

        extrasCategory.addEntry(entryBuilder
                .startFloatField(Text.literal("Extra Zombie Spawn Chance"), WastelandConfig.extraZombieSpawnChance)
                .setDefaultValue(0.3f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setTooltip(Text.literal("Chance to spawn 1-2 extra zombies when converting a villager"))
                .setSaveConsumer(value -> WastelandConfig.extraZombieSpawnChance = value)
                .build());

        extrasCategory.addEntry(entryBuilder
                .startIntField(Text.literal("Min Extra Zombies"), WastelandConfig.minExtraZombies)
                .setDefaultValue(1)
                .setMin(0)
                .setMax(5)
                .setTooltip(Text.literal("Minimum number of extra zombies to spawn"))
                .setSaveConsumer(value -> WastelandConfig.minExtraZombies = value)
                .build());

        extrasCategory.addEntry(entryBuilder
                .startIntField(Text.literal("Max Extra Zombies"), WastelandConfig.maxExtraZombies)
                .setDefaultValue(2)
                .setMin(1)
                .setMax(10)
                .setTooltip(Text.literal("Maximum number of extra zombies to spawn"))
                .setSaveConsumer(value -> WastelandConfig.maxExtraZombies = value)
                .build());

        return builder.build();
    }

    private static void saveConfig() {
        WastelandConfigManager.save();
    }
}
