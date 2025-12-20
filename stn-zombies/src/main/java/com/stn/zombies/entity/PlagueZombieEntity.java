package com.stn.zombies.entity;

import com.stn.mobai.entity.BlockBreakAnimatable;
import com.stn.zombies.config.STNZombiesConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Plague Zombie - Attrition role
 * Applies stacking sickness effect on hit that weakens movement and healing.
 */
public class PlagueZombieEntity extends ZombieEntity implements BlockBreakAnimatable {

    private boolean isBreakingBlock = false;

    // Track plague stacks per player
    private static final Map<UUID, Integer> plagueStacks = new HashMap<>();

    public PlagueZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createPlagueAttributes() {
        return ZombieEntity.createZombieAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, STNZombiesConfig.PLAGUE_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, STNZombiesConfig.PLAGUE_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, STNZombiesConfig.PLAGUE_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
            .add(EntityAttributes.GENERIC_ARMOR, 0.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        // Drip poison particles
        if (!this.getWorld().isClient() && this.random.nextInt(10) == 0) {
            this.getWorld().addParticle(
                ParticleTypes.FALLING_SPORE_BLOSSOM,
                this.getX() + this.random.nextGaussian() * 0.3,
                this.getY() + 1.0,
                this.getZ() + this.random.nextGaussian() * 0.3,
                0, -0.05, 0
            );
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);

        if (hit && target instanceof PlayerEntity player) {
            applyPlague(player);
        }

        return hit;
    }

    private void applyPlague(PlayerEntity player) {
        UUID playerId = player.getUuid();
        int currentStacks = plagueStacks.getOrDefault(playerId, 0);

        if (currentStacks < STNZombiesConfig.PLAGUE_MAX_STACKS) {
            currentStacks++;
            plagueStacks.put(playerId, currentStacks);
        }

        // Apply effects based on stack count
        int duration = STNZombiesConfig.PLAGUE_SICKNESS_DURATION;

        // Slowness (stacks increase amplifier)
        player.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SLOWNESS,
            duration,
            Math.min(currentStacks - 1, 2), // Max slowness 3
            false,
            true
        ));

        // Hunger at 2+ stacks
        if (currentStacks >= 2) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.HUNGER,
                duration,
                currentStacks - 2,
                false,
                true
            ));
        }

        // Weakness at 3+ stacks
        if (currentStacks >= 3) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.WEAKNESS,
                duration,
                0,
                false,
                true
            ));
        }

        // Nausea at 4+ stacks
        if (currentStacks >= 4) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NAUSEA,
                duration / 2,
                0,
                false,
                true
            ));
        }

        // Wither at max stacks
        if (currentStacks >= STNZombiesConfig.PLAGUE_MAX_STACKS) {
            player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.WITHER,
                duration / 4,
                0,
                false,
                true
            ));
        }

        // Visual feedback
        for (int i = 0; i < currentStacks * 2; i++) {
            player.getWorld().addParticle(
                ParticleTypes.SPORE_BLOSSOM_AIR,
                player.getX() + this.random.nextGaussian() * 0.5,
                player.getY() + 1.0,
                player.getZ() + this.random.nextGaussian() * 0.5,
                0, 0.1, 0
            );
        }
    }

    /**
     * Clear plague stacks for a player (call on death or cure).
     */
    public static void clearPlagueStacks(UUID playerId) {
        plagueStacks.remove(playerId);
    }

    public static int getPlagueStacks(UUID playerId) {
        return plagueStacks.getOrDefault(playerId, 0);
    }

    // BlockBreakAnimatable implementation
    @Override
    public void triggerBlockBreakSwing() {
        this.swingHand(this.getActiveHand());
    }

    @Override
    public boolean isBreakingBlock() {
        return this.isBreakingBlock;
    }

    @Override
    public void setBreakingBlock(boolean breaking) {
        this.isBreakingBlock = breaking;
    }

    @Override
    protected boolean burnsInDaylight() {
        return true;
    }
}
