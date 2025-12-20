package com.stn.spiders.entity;

import com.stn.spiders.config.STNSpidersConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Burden Spider - Debuffer/tank role
 * Heavily armored, slow movement.
 * Applies weakness or mining fatigue on hit.
 */
public class BurdenSpiderEntity extends SpiderEntity {

    public BurdenSpiderEntity(EntityType<? extends SpiderEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createBurdenAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, STNSpidersConfig.BURDEN_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, STNSpidersConfig.BURDEN_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, STNSpidersConfig.BURDEN_DAMAGE)
            .add(EntityAttributes.GENERIC_ARMOR, STNSpidersConfig.BURDEN_ARMOR)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, STNSpidersConfig.BURDEN_KNOCKBACK_RESISTANCE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        // Heavy footstep particles
        if (this.isOnGround() && this.getVelocity().horizontalLengthSquared() > 0.001) {
            if (this.random.nextInt(5) == 0) {
                this.getWorld().addParticle(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    0, 0.02, 0
                );
            }
        }

        // Armored appearance particles
        if (this.random.nextInt(30) == 0) {
            this.getWorld().addParticle(
                ParticleTypes.CRIT,
                this.getX() + this.random.nextGaussian() * 0.5,
                this.getY() + 0.3,
                this.getZ() + this.random.nextGaussian() * 0.5,
                0, 0, 0
            );
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);

        if (hit && target instanceof LivingEntity living && !this.getWorld().isClient()) {
            // Apply weakness
            living.addStatusEffect(new StatusEffectInstance(
                StatusEffects.WEAKNESS,
                STNSpidersConfig.BURDEN_WEAKNESS_DURATION,
                0, // Weakness I
                false,
                true
            ));

            // Apply mining fatigue
            living.addStatusEffect(new StatusEffectInstance(
                StatusEffects.MINING_FATIGUE,
                STNSpidersConfig.BURDEN_FATIGUE_DURATION,
                1, // Mining Fatigue II
                false,
                true
            ));

            // Heavy hit sound
            this.playSound(SoundEvents.ENTITY_IRON_GOLEM_ATTACK, 0.8f, 0.6f);

            // Debuff particles
            for (int i = 0; i < 6; i++) {
                target.getWorld().addParticle(
                    ParticleTypes.WITCH,
                    target.getX() + this.random.nextGaussian() * 0.5,
                    target.getY() + 1.0,
                    target.getZ() + this.random.nextGaussian() * 0.5,
                    0, 0.1, 0
                );
            }
        }

        return hit;
    }
}
