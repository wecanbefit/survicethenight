package com.stn.skeletons.entity;

import com.stn.mobai.entity.BlockBreakAnimatable;
import com.stn.skeletons.config.STNSkeletonsConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Reaper Skeleton - Executioner role
 * Deals bonus damage to low-health players.
 * Gains brief speed boost after a kill.
 * Finisher that escalates fights quickly.
 */
public class ReaperSkeletonEntity extends WitherSkeletonEntity implements BlockBreakAnimatable {

    private boolean isBreakingBlock = false;

    public ReaperSkeletonEntity(EntityType<? extends WitherSkeletonEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(2, new AvoidSunlightGoal(this));
        this.goalSelector.add(3, new EscapeSunlightGoal(this, 1.0));
        this.goalSelector.add(4, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));

        this.targetSelector.add(1, new RevengeGoal(this));
        // Prioritize low health targets
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, 10, true, false,
            entity -> entity instanceof LivingEntity living &&
                living.getHealth() / living.getMaxHealth() <= STNSkeletonsConfig.REAPER_LOW_HEALTH_THRESHOLD));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(4, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.targetSelector.add(4, new ActiveTargetGoal<>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }

    public static DefaultAttributeContainer.Builder createReaperAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, STNSkeletonsConfig.REAPER_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, STNSkeletonsConfig.REAPER_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, STNSkeletonsConfig.REAPER_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        // Dark particles around the reaper
        if (this.random.nextInt(8) == 0) {
            this.getWorld().addParticle(
                ParticleTypes.SMOKE,
                this.getX() + this.random.nextGaussian() * 0.3,
                this.getY() + 1.0 + this.random.nextDouble(),
                this.getZ() + this.random.nextGaussian() * 0.3,
                0, 0.02, 0
            );
        }

        // Target low health indicator
        if (!this.getWorld().isClient() && this.getTarget() != null) {
            LivingEntity target = this.getTarget();
            float healthPercent = target.getHealth() / target.getMaxHealth();

            if (healthPercent <= STNSkeletonsConfig.REAPER_LOW_HEALTH_THRESHOLD) {
                // Execute particles - show the reaper is targeting them
                if (this.random.nextInt(5) == 0) {
                    this.getWorld().addParticle(
                        ParticleTypes.ANGRY_VILLAGER,
                        this.getX(),
                        this.getY() + 2.2,
                        this.getZ(),
                        0, 0, 0
                    );
                }
            }
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        if (target instanceof LivingEntity living) {
            float healthPercent = living.getHealth() / living.getMaxHealth();
            boolean isLowHealth = healthPercent <= STNSkeletonsConfig.REAPER_LOW_HEALTH_THRESHOLD;

            // Store original damage
            double originalDamage = this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

            // Temporarily boost damage for low health targets
            if (isLowHealth) {
                // We'll apply bonus damage manually since we can't easily modify attribute mid-attack
            }

            boolean hit = super.tryAttack(target);

            if (hit && !this.getWorld().isClient()) {
                // Apply execute bonus damage
                if (isLowHealth) {
                    float bonusDamage = (float) (originalDamage * (STNSkeletonsConfig.REAPER_EXECUTE_DAMAGE_MULTIPLIER - 1.0));
                    living.damage(this.getDamageSources().mobAttack(this), bonusDamage);

                    // Execute visual
                    this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.8f);

                    for (int i = 0; i < 10; i++) {
                        target.getWorld().addParticle(
                            ParticleTypes.CRIT,
                            target.getX() + this.random.nextGaussian() * 0.5,
                            target.getY() + 1.0,
                            target.getZ() + this.random.nextGaussian() * 0.5,
                            0, 0.2, 0
                        );
                    }
                }

                // Check if we killed the target
                if (living.isDead() || living.getHealth() <= 0) {
                    onKill(living);
                }
            }

            return hit;
        }

        return super.tryAttack(target);
    }

    private void onKill(LivingEntity victim) {
        // Speed boost after kill
        this.addStatusEffect(new StatusEffectInstance(
            StatusEffects.SPEED,
            STNSkeletonsConfig.REAPER_KILL_SPEED_DURATION,
            STNSkeletonsConfig.REAPER_KILL_SPEED_LEVEL,
            false,
            true
        ));

        // Kill celebration
        this.playSound(SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT, 1.0f, 0.8f);

        // Death particles burst
        for (int i = 0; i < 15; i++) {
            this.getWorld().addParticle(
                ParticleTypes.SOUL,
                victim.getX() + this.random.nextGaussian() * 0.5,
                victim.getY() + 1.0,
                victim.getZ() + this.random.nextGaussian() * 0.5,
                this.random.nextGaussian() * 0.1,
                0.2,
                this.random.nextGaussian() * 0.1
            );
        }
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        // Melee only
    }

    @Override
    protected void initEquipment(net.minecraft.util.math.random.Random random, net.minecraft.world.LocalDifficulty localDifficulty) {
        // Netherite hoe as "scythe"
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_HOE));
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
