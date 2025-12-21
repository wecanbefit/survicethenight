package com.stn.zombies.entity;

import com.stn.mobai.entity.BlockBreakAnimatable;
import com.stn.zombies.config.STNZombiesConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Leech Zombie - Sustain threat role
 * Heals itself based on damage dealt. Forces quick target prioritization.
 */
public class LeechZombieEntity extends ZombieEntity implements BlockBreakAnimatable {

    private boolean isBreakingBlock = false;

    public LeechZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createLeechAttributes() {
        return ZombieEntity.createZombieAttributes()
            .add(EntityAttributes.MAX_HEALTH, STNZombiesConfig.LEECH_HEALTH)
            .add(EntityAttributes.MOVEMENT_SPEED, STNZombiesConfig.LEECH_SPEED)
            .add(EntityAttributes.ATTACK_DAMAGE, STNZombiesConfig.LEECH_DAMAGE)
            .add(EntityAttributes.FOLLOW_RANGE, 32.0)
            .add(EntityAttributes.ARMOR, 0.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        // Dripping blood particles
        if (this.getWorld() instanceof ServerWorld sw && this.random.nextInt(20) == 0) {
            sw.spawnParticles(
                ParticleTypes.DRIPPING_OBSIDIAN_TEAR,
                this.getX(),
                this.getY() + 1.0,
                this.getZ(),
                1, 0.2, 0, 0.2, 0
            );
        }
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean hit = super.tryAttack(world, target);

        if (hit && !this.getWorld().isClient()) {
            // Calculate heal amount
            float damage = (float) this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
            float healAmount = damage * STNZombiesConfig.LEECH_HEAL_PERCENT;

            // Heal self
            float currentHealth = this.getHealth();
            float maxHealth = this.getMaxHealth();

            if (currentHealth < maxHealth) {
                this.setHealth(Math.min(currentHealth + healAmount, maxHealth));

                // Healing visual/audio feedback
                this.playSound(SoundEvents.ENTITY_GENERIC_DRINK.value(), 0.5f, 1.5f);

                // Blood drain particles from target to self
                if (this.getWorld() instanceof ServerWorld sw) {
                    // Trail of particles between target and self
                    double midX = (target.getX() + this.getX()) / 2;
                    double midY = (target.getY() + this.getY()) / 2 + 1.0;
                    double midZ = (target.getZ() + this.getZ()) / 2;
                    sw.spawnParticles(
                        ParticleTypes.DAMAGE_INDICATOR,
                        midX, midY, midZ,
                        8, 0.5, 0.3, 0.5, 0
                    );

                    // Healing particles on self
                    sw.spawnParticles(
                        ParticleTypes.HEART,
                        this.getX(),
                        this.getY() + 1.5,
                        this.getZ(),
                        5, 0.3, 0.3, 0.3, 0.1
                    );
                }
            }
        }

        return hit;
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
