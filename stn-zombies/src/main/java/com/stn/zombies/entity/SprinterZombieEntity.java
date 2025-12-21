package com.stn.zombies.entity;

import com.stn.mobai.entity.BlockBreakAnimatable;
import com.stn.zombies.config.STNZombiesConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Sprinter Zombie - Chaser role
 * Burst sprints when within range of target. Lower health to balance speed.
 */
public class SprinterZombieEntity extends ZombieEntity implements BlockBreakAnimatable {

    private boolean isBreakingBlock = false;
    private boolean isSprinting = false;
    private int sprintCooldown = 0;

    public SprinterZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createSprinterAttributes() {
        return ZombieEntity.createZombieAttributes()
            .add(EntityAttributes.MAX_HEALTH, STNZombiesConfig.SPRINTER_HEALTH)
            .add(EntityAttributes.MOVEMENT_SPEED, STNZombiesConfig.SPRINTER_SPEED)
            .add(EntityAttributes.ATTACK_DAMAGE, STNZombiesConfig.SPRINTER_DAMAGE)
            .add(EntityAttributes.FOLLOW_RANGE, 48.0)
            .add(EntityAttributes.ARMOR, 0.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (!this.getWorld().isClient()) {
            if (sprintCooldown > 0) {
                sprintCooldown--;
            }

            if (this.getTarget() != null) {
                double distance = this.squaredDistanceTo(this.getTarget());
                double burstRangeSq = STNZombiesConfig.SPRINTER_BURST_RANGE * STNZombiesConfig.SPRINTER_BURST_RANGE;

                if (distance <= burstRangeSq && distance > 4.0 && sprintCooldown <= 0) {
                    // Activate burst sprint
                    if (!isSprinting) {
                        startSprint();
                    }
                    updateSpeed(STNZombiesConfig.SPRINTER_BURST_SPEED);
                } else {
                    if (isSprinting) {
                        stopSprint();
                    }
                    updateSpeed(STNZombiesConfig.SPRINTER_SPEED);
                }
            } else {
                if (isSprinting) {
                    stopSprint();
                }
                updateSpeed(STNZombiesConfig.SPRINTER_SPEED);
            }

            // Sprint particles
            if (isSprinting && this.random.nextInt(2) == 0 && this.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(
                    ParticleTypes.CLOUD,
                    this.getX(),
                    this.getY() + 0.1,
                    this.getZ(),
                    1, 0, 0, 0, 0
                );
            }
        }
    }

    private void startSprint() {
        isSprinting = true;
        this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.5f, 1.5f);
    }

    private void stopSprint() {
        isSprinting = false;
        sprintCooldown = 40; // 2 second cooldown before next sprint
    }

    private void updateSpeed(double speed) {
        var attr = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (attr != null && Math.abs(attr.getBaseValue() - speed) > 0.01) {
            attr.setBaseValue(speed);
        }
    }

    public boolean isSprinting() {
        return isSprinting;
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
