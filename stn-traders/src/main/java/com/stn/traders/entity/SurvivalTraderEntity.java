package com.stn.traders.entity;

import com.stn.core.STNCore;
import com.stn.core.api.IGamestageProvider;
import com.stn.traders.STNTraders;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Immortal trader NPC that spawns in trader structures.
 * Combines all villager trades into a single merchant.
 * Trade quality and availability scale with gamestage.
 */
public class SurvivalTraderEntity extends WanderingTraderEntity {

    // Cached trade offers, rebuilt when gamestage changes significantly
    private TradeOfferList cachedOffers;
    private int lastOfferGamestage = -1;

    public SurvivalTraderEntity(EntityType<? extends WanderingTraderEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        // Minimal AI - just looks at players and occasionally looks around
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(2, new LookAroundGoal(this));
    }

    /**
     * Create attributes for the survival trader.
     * High health as backup, but mainly relies on invulnerability.
     */
    public static DefaultAttributeContainer.Builder createTraderAttributes() {
        return WanderingTraderEntity.createMobAttributes()
            .add(EntityAttributes.MAX_HEALTH, 100.0)
            .add(EntityAttributes.MOVEMENT_SPEED, 0.0); // Stationary
    }

    // === INVULNERABILITY ===

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        // Completely invulnerable to all damage
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    // canBeLeashedBy - no override, just return false by default behavior

    @Override
    public boolean isPushable() {
        return false;
    }

    // === RECIPES / TRADES ===

    @Override
    protected void fillRecipes() {
        // Trades are built dynamically in getOffers() based on gamestage
        // This is called during entity initialization
    }

    // === TRADING ===

    @Override
    public TradeOfferList getOffers() {
        int currentGamestage = getCurrentGamestage();

        // Rebuild offers if gamestage changed significantly (every 10 levels)
        if (cachedOffers == null || (currentGamestage / 10) != (lastOfferGamestage / 10)) {
            cachedOffers = buildTradeOffers(currentGamestage);
            lastOfferGamestage = currentGamestage;
        }

        return cachedOffers;
    }

    /**
     * Force refresh of trade offers (e.g., daily restock).
     */
    public void refreshOffers() {
        cachedOffers = null;
        lastOfferGamestage = -1;
    }

    private int getCurrentGamestage() {
        IGamestageProvider provider = STNCore.getGamestageProvider();
        return provider != null ? provider.getWorldGamestage() : 0;
    }

    /**
     * Build trade offers based on current gamestage.
     * TODO: Load from config file for customization.
     */
    private TradeOfferList buildTradeOffers(int gamestage) {
        TradeOfferList offers = new TradeOfferList();
        int tier = Math.min(9, gamestage / 10);

        // Tier 0+ : Basic supplies (always available)
        offers.add(createTrade(Items.EMERALD.getDefaultStack(), 1, Items.BREAD.getDefaultStack(), 6, 16));
        offers.add(createTrade(Items.EMERALD.getDefaultStack(), 1, Items.COOKED_BEEF.getDefaultStack(), 4, 16));
        offers.add(createTrade(Items.EMERALD.getDefaultStack(), 2, Items.TORCH.getDefaultStack(), 16, 16));
        offers.add(createTrade(Items.EMERALD.getDefaultStack(), 3, Items.ARROW.getDefaultStack(), 16, 16));

        // Tier 1+ (Gamestage 10+): Iron tools
        if (tier >= 1) {
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 5, Items.IRON_SWORD.getDefaultStack(), 1, 8));
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 4, Items.IRON_PICKAXE.getDefaultStack(), 1, 8));
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 3, Items.IRON_AXE.getDefaultStack(), 1, 8));
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 6, Items.IRON_HELMET.getDefaultStack(), 1, 8));
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 8, Items.IRON_CHESTPLATE.getDefaultStack(), 1, 8));
        }

        // Tier 2+ (Gamestage 20+): Chainmail, shields
        if (tier >= 2) {
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 4, Items.CHAINMAIL_HELMET.getDefaultStack(), 1, 8));
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 6, Items.CHAINMAIL_CHESTPLATE.getDefaultStack(), 1, 8));
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 5, Items.SHIELD.getDefaultStack(), 1, 8));
        }

        // Tier 3+ (Gamestage 30+): Potions, enchanting supplies
        if (tier >= 3) {
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 8, Items.EXPERIENCE_BOTTLE.getDefaultStack(), 4, 12));
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 6, Items.ENDER_PEARL.getDefaultStack(), 2, 8));
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 12, Items.ENCHANTED_BOOK.getDefaultStack(), 1, 4));
        }

        // Tier 4+ (Gamestage 40+): Diamond tools (unenchanted)
        if (tier >= 4) {
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 16, Items.DIAMOND_SWORD.getDefaultStack(), 1, 4));
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 14, Items.DIAMOND_PICKAXE.getDefaultStack(), 1, 4));
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 18, Items.DIAMOND_CHESTPLATE.getDefaultStack(), 1, 4));
        }

        // Tier 5+ (Gamestage 50+): Better gear
        if (tier >= 5) {
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 20, Items.DIAMOND_HELMET.getDefaultStack(), 1, 4));
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 18, Items.DIAMOND_LEGGINGS.getDefaultStack(), 1, 4));
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 16, Items.DIAMOND_BOOTS.getDefaultStack(), 1, 4));
        }

        // Tier 6+ (Gamestage 60+): Rare items
        if (tier >= 6) {
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 32, Items.GOLDEN_APPLE.getDefaultStack(), 2, 4));
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 24, Items.NAME_TAG.getDefaultStack(), 1, 8));
        }

        // Tier 7+ (Gamestage 70+): Netherite materials
        if (tier >= 7) {
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 48, Items.NETHERITE_INGOT.getDefaultStack(), 1, 2));
        }

        // Tier 8+ (Gamestage 80+): Very rare
        if (tier >= 8) {
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 64, Items.NETHERITE_SWORD.getDefaultStack(), 1, 1));
        }

        // Tier 9+ (Gamestage 90+): Legendary
        if (tier >= 9) {
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 64, Items.TOTEM_OF_UNDYING.getDefaultStack(), 1, 1));
            offers.add(createTrade(Items.EMERALD.getDefaultStack(), 64, Items.ELYTRA.getDefaultStack(), 1, 1));
        }

        return offers;
    }

    private TradeOffer createTrade(ItemStack buy, int buyCount, ItemStack sell, int sellCount, int maxUses) {
        ItemStack buyStack = buy.copy();
        buyStack.setCount(buyCount);
        ItemStack sellStack = sell.copy();
        sellStack.setCount(sellCount);

        return new TradeOffer(
            new TradedItem(buyStack.getItem(), buyCount),
            sellStack,
            maxUses,
            1,    // merchant experience
            0.05f // price multiplier
        );
    }

    @Override
    public void trade(TradeOffer offer) {
        offer.use();
        this.ambientSoundChance = -this.getMinAmbientSoundDelay();
    }

    @Override
    public void afterUsing(TradeOffer offer) {
        PlayerEntity customer = this.getCustomer();
        if (customer != null) {
            customer.incrementStat(Stats.TRADED_WITH_VILLAGER);
        }
    }

    @Override
    public boolean isLeveledMerchant() {
        return false; // No leveling system
    }

    // === SOUNDS ===

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_VILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_VILLAGER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_VILLAGER_DEATH;
    }

    @Override
    public SoundEvent getYesSound() {
        return SoundEvents.ENTITY_VILLAGER_YES;
    }

    // === NOT BREEDABLE ===

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null; // Cannot breed
    }
}
