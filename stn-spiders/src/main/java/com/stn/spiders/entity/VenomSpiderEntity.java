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
 * Venom Spider - Damage over time role
 * Applies stacking poison on hit.
 */
public class VenomSpiderEntity extends SpiderEntity {

    public VenomSpiderEntity(EntityType<? extends SpiderEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createVenomAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, STNSpidersConfig.VENOM_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, STNSpidersConfig.VENOM_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, STNSpidersConfig.VENOM_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        // Dripping venom particles
        if (this.random.nextInt(10) == 0) {
            this.getWorld().addParticle(
                ParticleTypes.ITEM_SLIME,
                this.getX() + this.random.nextGaussian() * 0.3,
                this.getY() + 0.3,
                this.getZ() + this.random.nextGaussian() * 0.3,
                0, -0.05, 0
            );
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);

        if (hit && target instanceof LivingEntity living && !this.getWorld().isClient()) {
            // Check for existing poison to stack duration
            StatusEffectInstance existingPoison = living.getStatusEffect(StatusEffects.POISON);
            int baseDuration = STNSpidersConfig.VENOM_POISON_DURATION;

            if (existingPoison != null) {
                // Stack: add bonus duration to existing
                int newDuration = existingPoison.getDuration() + STNSpidersConfig.VENOM_POISON_STACK_BONUS;
                living.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.POISON,
                    newDuration,
                    STNSpidersConfig.VENOM_POISON_LEVEL,
                    false,
                    true
                ));

                // Stacking visual feedback
                this.playSound(SoundEvents.ENTITY_SPIDER_HURT, 0.5f, 2.0f);
            } else {
                // Apply fresh poison
                living.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.POISON,
                    baseDuration,
                    STNSpidersConfig.VENOM_POISON_LEVEL,
                    false,
                    true
                ));
            }

            // Venom hit particles
            for (int i = 0; i < 8; i++) {
                target.getWorld().addParticle(
                    ParticleTypes.ITEM_SLIME,
                    target.getX() + this.random.nextGaussian() * 0.5,
                    target.getY() + 0.8,
                    target.getZ() + this.random.nextGaussian() * 0.5,
                    this.random.nextGaussian() * 0.05,
                    0.1,
                    this.random.nextGaussian() * 0.05
                );
            }
        }

        return hit;
    }
}
