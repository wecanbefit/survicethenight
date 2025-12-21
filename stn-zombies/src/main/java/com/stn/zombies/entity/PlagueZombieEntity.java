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
import net.minecraft.server.world.ServerWorld;
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
            .add(EntityAttributes.MAX_HEALTH, STNZombiesConfig.PLAGUE_HEALTH)
            .add(EntityAttributes.MOVEMENT_SPEED, STNZombiesConfig.PLAGUE_SPEED)
            .add(EntityAttributes.ATTACK_DAMAGE, STNZombiesConfig.PLAGUE_DAMAGE)
            .add(EntityAttributes.FOLLOW_RANGE, 32.0)
            .add(EntityAttributes.ARMOR, 0.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        // Drip poison particles
        if (this.getWorld() instanceof ServerWorld sw && this.random.nextInt(10) == 0) {
            sw.spawnParticles(
                ParticleTypes.FALLING_SPORE_BLOSSOM,
                this.getX(),
                this.getY() + 1.0,
                this.getZ(),
                1, 0.3, 0, 0.3, 0
            );
        }
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean hit = super.tryAttack(world, target);

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
        if (player.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(
                ParticleTypes.SPORE_BLOSSOM_AIR,
                player.getX(),
                player.getY() + 1.0,
                player.getZ(),
                currentStacks * 2, 0.5, 0.3, 0.5, 0.1
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
