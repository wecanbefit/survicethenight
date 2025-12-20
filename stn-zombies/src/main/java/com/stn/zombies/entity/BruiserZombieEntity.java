package com.stn.zombies.entity;

import com.stn.mobai.entity.BlockBreakAnimatable;
import com.stn.mobai.entity.IBlockBreaker;
import com.stn.zombies.config.STNZombiesConfig;
import com.stn.zombies.entity.ai.WindupAttackGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Bruiser Zombie - Tank role
 * High health and knockback resistance with a heavy windup attack.
 */
public class BruiserZombieEntity extends ZombieEntity implements BlockBreakAnimatable, IBlockBreaker {

    private boolean isBreakingBlock = false;
    private boolean isWindingUp = false;

    public BruiserZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        // Replace standard attack with windup attack
        this.goalSelector.add(2, new WindupAttackGoal(this, 1.0, false, STNZombiesConfig.BRUISER_WINDUP_TICKS));
    }

    public static DefaultAttributeContainer.Builder createBruiserAttributes() {
        return ZombieEntity.createZombieAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, STNZombiesConfig.BRUISER_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, STNZombiesConfig.BRUISER_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, STNZombiesConfig.BRUISER_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 40.0)
            .add(EntityAttributes.GENERIC_ARMOR, 4.0)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.8)
            .add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1.5);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        // Heavy footstep particles
        if (!this.getWorld().isClient() && this.isOnGround() && this.random.nextInt(10) == 0) {
            this.getWorld().addParticle(
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                this.getX(),
                this.getY() + 0.1,
                this.getZ(),
                0, 0.02, 0
            );
        }
    }

    public void setWindingUp(boolean windingUp) {
        this.isWindingUp = windingUp;
        if (windingUp && !this.getWorld().isClient()) {
            this.playSound(SoundEvents.ENTITY_RAVAGER_ROAR, 0.5f, 1.2f);
        }
    }

    public boolean isWindingUp() {
        return isWindingUp;
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

    // IBlockBreaker implementation
    @Override
    public double getWoodBreakMultiplier() {
        return 1.5;
    }

    @Override
    public double getStoneBreakMultiplier() {
        return 1.2;
    }

    @Override
    protected boolean burnsInDaylight() {
        return false; // Bruiser is tough enough to endure sunlight
    }
}
