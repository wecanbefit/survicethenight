package com.stn.skeletons.entity;

import com.stn.skeletons.config.STNSkeletonsConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Flame Archer Skeleton - Damage over time role
 * Shoots flaming arrows that ignite targets.
 * Strong night threat and pressure against armor.
 */
public class FlameArcherSkeletonEntity extends SkeletonEntity {

    private int attackCooldown = 0;

    public FlameArcherSkeletonEntity(EntityType<? extends SkeletonEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(2, new AvoidSunlightGoal(this));
        this.goalSelector.add(3, new EscapeSunlightGoal(this, 1.0));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));

        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }

    public static DefaultAttributeContainer.Builder createFlameArcherAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, STNSkeletonsConfig.FLAME_ARCHER_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, STNSkeletonsConfig.FLAME_ARCHER_SPEED)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // Flame particles around the skeleton
        if (this.random.nextInt(10) == 0) {
            this.getWorld().addParticle(
                ParticleTypes.FLAME,
                this.getX() + this.random.nextGaussian() * 0.2,
                this.getY() + 1.5,
                this.getZ() + this.random.nextGaussian() * 0.2,
                0, 0.02, 0
            );
        }

        // Fire attack logic
        if (!this.getWorld().isClient() && this.getTarget() != null) {
            LivingEntity target = this.getTarget();
            double distance = this.squaredDistanceTo(target);

            if (distance < 32 * 32 && this.canSee(target) && attackCooldown <= 0) {
                performRangedAttack(target);
                attackCooldown = STNSkeletonsConfig.FLAME_ARCHER_FIRE_COOLDOWN;
            }
        }
    }

    private void performRangedAttack(LivingEntity target) {
        ArrowEntity arrow = new ArrowEntity(this.getWorld(), this, new ItemStack(Items.ARROW), null);

        // Set arrow on fire
        arrow.setOnFireFor(STNSkeletonsConfig.FLAME_ARCHER_BURN_DURATION / 20); // Convert ticks to seconds

        // Calculate aim
        double dx = target.getX() - this.getX();
        double dy = target.getBodyY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        arrow.setVelocity(dx, dy + distance * 0.2, dz, 1.5f, 2.0f);
        arrow.setDamage(STNSkeletonsConfig.FLAME_ARCHER_ARROW_DAMAGE);

        // Fire sound
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0f, 1.0f);
        this.playSound(SoundEvents.ITEM_FIRECHARGE_USE, 0.5f, 1.0f);

        this.getWorld().spawnEntity(arrow);
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        // Override default skeleton shooting
    }

    @Override
    protected void initEquipment(net.minecraft.util.math.random.Random random, net.minecraft.world.LocalDifficulty localDifficulty) {
        this.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    // Flame archer is immune to fire
    @Override
    public boolean isFireImmune() {
        return true;
    }

}
