package com.stn.spiders.entity;

import com.stn.spiders.config.STNSpidersConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Leaper Spider - Burst mobility role
 * Extended leap distance, prioritizes elevated targets.
 */
public class LeaperSpiderEntity extends SpiderEntity {

    private int leapCooldown = 0;

    public LeaperSpiderEntity(EntityType<? extends SpiderEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createLeaperAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, STNSpidersConfig.LEAPER_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, STNSpidersConfig.LEAPER_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, STNSpidersConfig.LEAPER_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (leapCooldown > 0) {
            leapCooldown--;
        }

        // Check for leap opportunity
        if (!this.getWorld().isClient() && this.getTarget() != null && leapCooldown <= 0 && this.isOnGround()) {
            LivingEntity target = this.getTarget();
            double distance = this.distanceTo(target);
            double heightDiff = target.getY() - this.getY();

            // Prioritize elevated targets or targets within leap range
            boolean shouldLeap = distance <= STNSpidersConfig.LEAPER_LEAP_RANGE &&
                (heightDiff > 1.0 || (distance > 3.0 && distance < 8.0));

            if (shouldLeap && this.canSee(target)) {
                performLeap(target);
                leapCooldown = STNSpidersConfig.LEAPER_LEAP_COOLDOWN;
            }
        }
    }

    private void performLeap(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dy = target.getY() - this.getY();
        double dz = target.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance > 0) {
            // Calculate leap velocity
            double velocityX = (dx / distance) * STNSpidersConfig.LEAPER_LEAP_VELOCITY;
            double velocityZ = (dz / distance) * STNSpidersConfig.LEAPER_LEAP_VELOCITY;

            // Add extra height for elevated targets
            double velocityY = STNSpidersConfig.LEAPER_LEAP_HEIGHT;
            if (dy > 0) {
                velocityY += Math.min(dy * 0.3, 0.5); // Extra height to reach elevated targets
            }

            this.setVelocity(velocityX, velocityY, velocityZ);
            this.playSound(SoundEvents.ENTITY_SPIDER_AMBIENT, 1.0f, 1.5f);

            // Leap particles
            for (int i = 0; i < 8; i++) {
                this.getWorld().addParticle(
                    ParticleTypes.POOF,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    this.random.nextGaussian() * 0.1,
                    0.1,
                    this.random.nextGaussian() * 0.1
                );
            }
        }
    }

    public boolean canLeap() {
        return leapCooldown <= 0 && this.isOnGround();
    }
}
