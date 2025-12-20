package com.stn.zombies.entity;

import com.stn.zombies.config.STNZombiesConfig;
import com.stn.zombies.entity.ai.LeapAttackGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Zombabie - Swarm/harassment role
 * Small zombie that fits through 1-block gaps and has a leap attack.
 * Often spawns in pairs or small packs.
 */
public class ZombabieEntity extends ZombieEntity {

    private int leapCooldown = 0;

    public ZombabieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(2, new LeapAttackGoal(this, STNZombiesConfig.ZOMBABIE_LEAP_VELOCITY));
    }

    public static DefaultAttributeContainer.Builder createZombabieAttributes() {
        return ZombieEntity.createZombieAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, STNZombiesConfig.ZOMBABIE_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, STNZombiesConfig.ZOMBABIE_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, STNZombiesConfig.ZOMBABIE_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
            .add(EntityAttributes.GENERIC_ARMOR, 0.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (leapCooldown > 0) {
            leapCooldown--;
        }
    }

    public void leap() {
        if (leapCooldown > 0 || this.getTarget() == null) return;

        leapCooldown = STNZombiesConfig.ZOMBABIE_LEAP_COOLDOWN;

        double dx = this.getTarget().getX() - this.getX();
        double dz = this.getTarget().getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance > 0) {
            double velocityX = (dx / distance) * STNZombiesConfig.ZOMBABIE_LEAP_VELOCITY;
            double velocityZ = (dz / distance) * STNZombiesConfig.ZOMBABIE_LEAP_VELOCITY;
            double velocityY = 0.4;

            this.setVelocity(velocityX, velocityY, velocityZ);
            this.playSound(SoundEvents.ENTITY_SLIME_JUMP, 0.5f, 1.5f);
        }
    }

    public boolean canLeap() {
        return leapCooldown <= 0 && this.isOnGround();
    }

    @Override
    public boolean isBaby() {
        return true; // Always render as baby zombie size
    }

    @Override
    protected boolean burnsInDaylight() {
        return true;
    }

    // Small zombie - uses baby zombie rendering
}
