package com.stn.skeletons.entity;

import com.stn.mobai.entity.BlockBreakAnimatable;
import com.stn.skeletons.config.STNSkeletonsConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Duelist Skeleton - Aggressive melee role
 * Fast movement and attack speed with a short dash ability.
 * Punishes backpedaling and poor spacing.
 */
public class DuelistSkeletonEntity extends WitherSkeletonEntity implements BlockBreakAnimatable {

    private boolean isBreakingBlock = false;
    private int dashCooldown = 0;

    public DuelistSkeletonEntity(EntityType<? extends WitherSkeletonEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(2, new AvoidSunlightGoal(this));
        this.goalSelector.add(3, new EscapeSunlightGoal(this, 1.0));
        this.goalSelector.add(4, new MeleeAttackGoal(this, 1.2, false)); // Faster attack
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));

        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }

    public static DefaultAttributeContainer.Builder createDuelistAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.MAX_HEALTH, STNSkeletonsConfig.DUELIST_HEALTH)
            .add(EntityAttributes.MOVEMENT_SPEED, STNSkeletonsConfig.DUELIST_SPEED)
            .add(EntityAttributes.ATTACK_DAMAGE, STNSkeletonsConfig.DUELIST_DAMAGE)
            .add(EntityAttributes.ATTACK_SPEED, 2.0) // Faster attacks
            .add(EntityAttributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (dashCooldown > 0) {
            dashCooldown--;
        }

        // Dash toward target when in range
        if (!this.getWorld().isClient() && this.getTarget() != null && dashCooldown <= 0) {
            LivingEntity target = this.getTarget();
            double distance = this.distanceTo(target);

            // Dash when within range but not too close
            if (distance <= STNSkeletonsConfig.DUELIST_DASH_RANGE && distance > 2.0 && this.isOnGround()) {
                performDash(target);
                dashCooldown = STNSkeletonsConfig.DUELIST_DASH_COOLDOWN;
            }
        }

        // Speed trail particles
        if (this.getVelocity().horizontalLengthSquared() > 0.04) {
            if (this.random.nextInt(3) == 0 && this.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(
                    ParticleTypes.SOUL,
                    this.getX(),
                    this.getY() + 0.5,
                    this.getZ(),
                    1, 0, 0, 0, 0
                );
            }
        }
    }

    private void performDash(LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance > 0) {
            double velocityX = (dx / distance) * STNSkeletonsConfig.DUELIST_DASH_VELOCITY;
            double velocityZ = (dz / distance) * STNSkeletonsConfig.DUELIST_DASH_VELOCITY;

            this.setVelocity(velocityX, 0.25, velocityZ);
            this.playSound(SoundEvents.ENTITY_PHANTOM_FLAP, 1.0f, 1.5f);

            // Dash particles
            if (this.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(),
                    this.getY() + 1.0,
                    this.getZ(),
                    10, 0.3, 0.3, 0.3, 0.1
                );
            }
        }
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        // Melee only
    }

    @Override
    protected void initEquipment(net.minecraft.util.math.random.Random random, net.minecraft.world.LocalDifficulty localDifficulty) {
        // Stone sword for faster animation feel
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
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

}
