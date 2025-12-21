package com.stn.wastelands.loot;

import com.mojang.serialization.MapCodec;
import com.stn.core.STNCore;
import com.stn.core.api.IGamestageProvider;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.random.Random;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom loot function that applies enchantments based on the current gamestage.
 * Uses LootConfig for tier-based enchantment levels.
 */
public class GamestageEnchantLootFunction implements LootFunction {
    public static final MapCodec<GamestageEnchantLootFunction> CODEC =
            MapCodec.unit(GamestageEnchantLootFunction::new);

    public static final LootFunctionType<GamestageEnchantLootFunction> TYPE =
            new LootFunctionType<>(CODEC);

    public GamestageEnchantLootFunction() {
    }

    @Override
    public LootFunctionType<GamestageEnchantLootFunction> getType() {
        return TYPE;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        Random random = context.getRandom();
        LootConfig config = LootConfigManager.getConfig();
        LootConfig.EnchantConfig enchantConfig = config.enchantments;

        // Get current gamestage and tier
        IGamestageProvider provider = STNCore.getGamestageProvider();
        int gamestage = provider != null ? provider.getWorldGamestage() : 0;
        int tier = LootConfig.getTierFromGamestage(gamestage);

        // Check if we should enchant at all
        if (random.nextDouble() > enchantConfig.enchantChance[tier]) {
            return stack;
        }

        // Get all available enchantments
        var enchantmentRegistry = context.getWorld().getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT);

        // Filter enchantments to only those applicable to this item
        boolean isBook = stack.isOf(Items.ENCHANTED_BOOK);
        List<RegistryEntry<Enchantment>> validEnchantments = enchantmentRegistry.streamEntries()
                .filter(entry -> isBook || entry.value().isAcceptableItem(stack))
                .collect(Collectors.toList());

        if (validEnchantments.isEmpty()) {
            return stack;
        }

        // Apply first enchantment
        applyEnchantment(stack, validEnchantments, tier, random, isBook, enchantConfig);

        // Chance for second enchantment
        if (random.nextDouble() < enchantConfig.multiEnchantChance[tier]) {
            applyEnchantment(stack, validEnchantments, tier, random, isBook, enchantConfig);
        }

        return stack;
    }

    private void applyEnchantment(ItemStack stack, List<RegistryEntry<Enchantment>> validEnchantments,
                                  int tier, Random random, boolean isBook,
                                  LootConfig.EnchantConfig enchantConfig) {
        // Select a random enchantment from valid options
        RegistryEntry<Enchantment> selectedEnchantment = validEnchantments.get(random.nextInt(validEnchantments.size()));
        Enchantment enchantment = selectedEnchantment.value();

        // Calculate enchantment level based on tier config
        int maxLevel = enchantment.getMaxLevel();
        int level = calculateLevel(tier, maxLevel, random, enchantConfig);

        // Apply the enchantment (different component for books vs items)
        if (isBook) {
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(
                    stack.getOrDefault(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT)
            );
            builder.add(selectedEnchantment, level);
            stack.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());
        } else {
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(
                    stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT)
            );
            builder.add(selectedEnchantment, level);
            stack.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        }
    }

    /**
     * Calculate enchantment level based on tier and config.
     */
    private int calculateLevel(int tier, int maxLevel, Random random, LootConfig.EnchantConfig config) {
        if (maxLevel == 1) {
            return 1;
        }

        // Calculate level range from config multipliers
        int minLevel = Math.max(1, (int)(maxLevel * config.minLevelMultiplier[tier]));
        int maxPossible = Math.max(minLevel, (int)(maxLevel * config.maxLevelMultiplier[tier]));

        // Roll for level within range
        int levelRange = maxPossible - minLevel + 1;
        return minLevel + random.nextInt(levelRange);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements LootFunction.Builder {
        @Override
        public LootFunction build() {
            return new GamestageEnchantLootFunction();
        }
    }
}
