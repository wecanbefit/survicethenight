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
import net.minecraft.server.world.ServerWorld;
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
            .add(EntityAttributes.MAX_HEALTH, STNSpidersConfig.VENOM_HEALTH)
            .add(EntityAttributes.MOVEMENT_SPEED, STNSpidersConfig.VENOM_SPEED)
            .add(EntityAttributes.ATTACK_DAMAGE, STNSpidersConfig.VENOM_DAMAGE)
            .add(EntityAttributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        // Dripping venom particles
        if (this.random.nextInt(10) == 0 && this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(
                ParticleTypes.ITEM_SLIME,
                this.getX(),
                this.getY() + 0.3,
                this.getZ(),
                1, 0.3, 0, 0.3, 0
            );
        }
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean hit = super.tryAttack(world, target);

        if (hit && target instanceof LivingEntity living) {
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
            world.spawnParticles(
                ParticleTypes.ITEM_SLIME,
                target.getX(),
                target.getY() + 0.8,
                target.getZ(),
                8, 0.5, 0.3, 0.5, 0.1
            );
        }

        return hit;
    }
}
