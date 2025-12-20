package com.stn.skeletons.entity;

import com.stn.skeletons.config.STNSkeletonsConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Suppressor Skeleton - Area control role
 * Fast fire rate, arrows apply slowness.
 * Forces players out of cover and into bad positions.
 */
public class SuppressorSkeletonEntity extends SkeletonEntity {

    private int attackCooldown = 0;

    public SuppressorSkeletonEntity(EntityType<? extends SkeletonEntity> entityType, World world) {
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

    public static DefaultAttributeContainer.Builder createSuppressorAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, STNSkeletonsConfig.SUPPRESSOR_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, STNSkeletonsConfig.SUPPRESSOR_SPEED)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // Rapid fire attack logic
        if (!this.getWorld().isClient() && this.getTarget() != null) {
            LivingEntity target = this.getTarget();
            double distance = this.squaredDistanceTo(target);

            if (distance < 32 * 32 && this.canSee(target) && attackCooldown <= 0) {
                performRangedAttack(target);
                attackCooldown = STNSkeletonsConfig.SUPPRESSOR_FIRE_COOLDOWN;
            }
        }
    }

    private void performRangedAttack(LivingEntity target) {
        // Create arrow with slowness effect
        ArrowEntity arrow = new ArrowEntity(this.getWorld(), this, new ItemStack(Items.ARROW), null);

        // Add slowness effect to the arrow
        arrow.addEffect(new StatusEffectInstance(
            StatusEffects.SLOWNESS,
            STNSkeletonsConfig.SUPPRESSOR_SLOWNESS_DURATION,
            STNSkeletonsConfig.SUPPRESSOR_SLOWNESS_LEVEL
        ));

        // Calculate aim with slight spread for suppression effect
        double dx = target.getX() - this.getX();
        double dy = target.getBodyY(0.3333333333333333) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        arrow.setVelocity(dx, dy + distance * 0.2, dz, 1.4f, 4.0f); // Some spread
        arrow.setDamage(STNSkeletonsConfig.SUPPRESSOR_ARROW_DAMAGE);

        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0f, 1.2f); // Higher pitch for rapid fire
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

}
