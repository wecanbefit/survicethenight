package com.stn.wastelands.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stn.core.STNCore;
import com.stn.core.api.IGamestageProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.math.random.Random;

/**
 * Custom loot function that sets item count based on gamestage tier.
 * Reads min/max quantities from LootConfig for the specific item.
 */
public class GamestageCountLootFunction implements LootFunction {
    private final String itemKey;

    public static final MapCodec<GamestageCountLootFunction> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.STRING.fieldOf("item_key").forGetter(f -> f.itemKey)
            ).apply(instance, GamestageCountLootFunction::new)
    );

    public static final LootFunctionType<GamestageCountLootFunction> TYPE =
            new LootFunctionType<>(CODEC);

    public GamestageCountLootFunction(String itemKey) {
        this.itemKey = itemKey;
    }

    @Override
    public LootFunctionType<GamestageCountLootFunction> getType() {
        return TYPE;
    }

    @Override
    public ItemStack apply(ItemStack stack, LootContext context) {
        Random random = context.getRandom();
        LootConfig config = LootConfigManager.getConfig();

        // Get current gamestage and tier
        IGamestageProvider provider = STNCore.getGamestageProvider();
        int gamestage = provider != null ? provider.getWorldGamestage() : 0;
        int tier = LootConfig.getTierFromGamestage(gamestage);

        // Get item config
        LootConfig.ItemConfig itemConfig = config.items.get(itemKey);
        if (itemConfig == null) {
            return stack;
        }

        // Check if this item should appear at this tier
        if (tier < itemConfig.minTier || tier > itemConfig.maxTier) {
            stack.setCount(0);
            return stack;
        }

        // Get quantity range for this tier
        int minQty = itemConfig.minQuantity[tier];
        int maxQty = itemConfig.maxQuantity[tier];

        // If quantity is 0, item doesn't drop at this tier
        if (maxQty <= 0) {
            stack.setCount(0);
            return stack;
        }

        // Calculate random count
        int count = minQty;
        if (maxQty > minQty) {
            count = minQty + random.nextInt(maxQty - minQty + 1);
        }

        stack.setCount(Math.max(1, count));
        return stack;
    }

    public static Builder builder(String itemKey) {
        return new Builder(itemKey);
    }

    public static class Builder implements LootFunction.Builder {
        private final String itemKey;

        public Builder(String itemKey) {
            this.itemKey = itemKey;
        }

        @Override
        public LootFunction build() {
            return new GamestageCountLootFunction(itemKey);
        }
    }
}
