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
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Marksman Skeleton - Precision ranged role
 * High damage, slow fire rate, perfect accuracy.
 * Punishes peeking and stationary players.
 */
public class MarksmanSkeletonEntity extends SkeletonEntity {

    private int attackCooldown = 0;

    public MarksmanSkeletonEntity(EntityType<? extends SkeletonEntity> entityType, World world) {
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

    public static DefaultAttributeContainer.Builder createMarksmanAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.MAX_HEALTH, STNSkeletonsConfig.MARKSMAN_HEALTH)
            .add(EntityAttributes.MOVEMENT_SPEED, STNSkeletonsConfig.MARKSMAN_SPEED)
            .add(EntityAttributes.FOLLOW_RANGE, 40.0); // Long range
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // Custom ranged attack logic
        if (!this.getWorld().isClient() && this.getTarget() != null) {
            LivingEntity target = this.getTarget();
            double distance = this.squaredDistanceTo(target);

            // Attack if in range and cooldown is ready
            if (distance < 40 * 40 && this.canSee(target) && attackCooldown <= 0) {
                performRangedAttack(target);
                attackCooldown = STNSkeletonsConfig.MARKSMAN_FIRE_COOLDOWN;
            }
        }
    }

    private void performRangedAttack(LivingEntity target) {
        ItemStack bow = new ItemStack(Items.BOW);
        PersistentProjectileEntity arrow = ProjectileUtil.createArrowProjectile(this, bow, 1.0f, null);

        // Calculate perfect aim (no spread)
        double dx = target.getX() - this.getX();
        double dy = target.getBodyY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        // Set velocity with no spread for perfect accuracy
        arrow.setVelocity(dx, dy + distance * 0.2, dz, 1.6f, STNSkeletonsConfig.MARKSMAN_ACCURACY);

        // High damage arrow
        arrow.setDamage(STNSkeletonsConfig.MARKSMAN_ARROW_DAMAGE);

        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0f, 0.8f); // Lower pitch for power shot
        this.getWorld().spawnEntity(arrow);
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        // Override default skeleton shooting - we handle it in tickMovement
    }

    @Override
    protected void initEquipment(net.minecraft.util.math.random.Random random, net.minecraft.world.LocalDifficulty localDifficulty) {
        // Always spawn with a bow
        this.equipStack(net.minecraft.entity.EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

}
