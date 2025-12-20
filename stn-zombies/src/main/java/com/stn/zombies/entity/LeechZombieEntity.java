package com.stn.zombies.entity;

import com.stn.mobai.entity.BlockBreakAnimatable;
import com.stn.zombies.config.STNZombiesConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.particle.ParticleTypes;
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
            .add(EntityAttributes.GENERIC_MAX_HEALTH, STNZombiesConfig.LEECH_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, STNZombiesConfig.LEECH_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, STNZombiesConfig.LEECH_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
            .add(EntityAttributes.GENERIC_ARMOR, 0.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        // Dripping blood particles
        if (!this.getWorld().isClient() && this.random.nextInt(20) == 0) {
            this.getWorld().addParticle(
                ParticleTypes.DRIPPING_OBSIDIAN_TEAR,
                this.getX() + this.random.nextGaussian() * 0.2,
                this.getY() + 1.0,
                this.getZ() + this.random.nextGaussian() * 0.2,
                0, -0.05, 0
            );
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);

        if (hit && !this.getWorld().isClient()) {
            // Calculate heal amount
            float damage = (float) this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            float healAmount = damage * STNZombiesConfig.LEECH_HEAL_PERCENT;

            // Heal self
            float currentHealth = this.getHealth();
            float maxHealth = this.getMaxHealth();

            if (currentHealth < maxHealth) {
                this.setHealth(Math.min(currentHealth + healAmount, maxHealth));

                // Healing visual/audio feedback
                this.playSound(SoundEvents.ENTITY_GENERIC_DRINK, 0.5f, 1.5f);

                // Blood drain particles from target to self
                for (int i = 0; i < 8; i++) {
                    double progress = i / 8.0;
                    double particleX = target.getX() + (this.getX() - target.getX()) * progress;
                    double particleY = target.getY() + 1.0 + (this.getY() + 1.0 - target.getY() - 1.0) * progress;
                    double particleZ = target.getZ() + (this.getZ() - target.getZ()) * progress;

                    this.getWorld().addParticle(
                        ParticleTypes.DAMAGE_INDICATOR,
                        particleX,
                        particleY,
                        particleZ,
                        0, 0, 0
                    );
                }

                // Healing particles on self
                for (int i = 0; i < 5; i++) {
                    this.getWorld().addParticle(
                        ParticleTypes.HEART,
                        this.getX() + this.random.nextGaussian() * 0.3,
                        this.getY() + 1.5,
                        this.getZ() + this.random.nextGaussian() * 0.3,
                        0, 0.1, 0
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
