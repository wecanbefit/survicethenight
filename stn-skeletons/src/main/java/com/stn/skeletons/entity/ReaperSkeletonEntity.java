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
import net.minecraft.server.world.ServerWorld;
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
        // Target players (will prefer low health in tryAttack)
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }

    public static DefaultAttributeContainer.Builder createReaperAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.MAX_HEALTH, STNSkeletonsConfig.REAPER_HEALTH)
            .add(EntityAttributes.MOVEMENT_SPEED, STNSkeletonsConfig.REAPER_SPEED)
            .add(EntityAttributes.ATTACK_DAMAGE, STNSkeletonsConfig.REAPER_DAMAGE)
            .add(EntityAttributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        // Dark particles around the reaper
        if (this.random.nextInt(8) == 0 && this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(
                ParticleTypes.SMOKE,
                this.getX(),
                this.getY() + 1.5,
                this.getZ(),
                1, 0.3, 0.5, 0.3, 0.02
            );
        }

        // Target low health indicator
        if (this.getWorld() instanceof ServerWorld sw && this.getTarget() != null) {
            LivingEntity target = this.getTarget();
            float healthPercent = target.getHealth() / target.getMaxHealth();

            if (healthPercent <= STNSkeletonsConfig.REAPER_LOW_HEALTH_THRESHOLD) {
                // Execute particles - show the reaper is targeting them
                if (this.random.nextInt(5) == 0) {
                    sw.spawnParticles(
                        ParticleTypes.ANGRY_VILLAGER,
                        this.getX(),
                        this.getY() + 2.2,
                        this.getZ(),
                        1, 0, 0, 0, 0
                    );
                }
            }
        }
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        if (target instanceof LivingEntity living) {
            float healthPercent = living.getHealth() / living.getMaxHealth();
            boolean isLowHealth = healthPercent <= STNSkeletonsConfig.REAPER_LOW_HEALTH_THRESHOLD;

            // Store original damage
            double originalDamage = this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);

            boolean hit = super.tryAttack(world, target);

            if (hit) {
                // Apply execute bonus damage
                if (isLowHealth) {
                    float bonusDamage = (float) (originalDamage * (STNSkeletonsConfig.REAPER_EXECUTE_DAMAGE_MULTIPLIER - 1.0));
                    living.damage(world, this.getDamageSources().mobAttack(this), bonusDamage);

                    // Execute visual
                    this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.8f);

                    world.spawnParticles(
                        ParticleTypes.CRIT,
                        target.getX(),
                        target.getY() + 1.0,
                        target.getZ(),
                        10, 0.5, 0.5, 0.5, 0.2
                    );
                }

                // Check if we killed the target
                if (living.isDead() || living.getHealth() <= 0) {
                    onKill(world, living);
                }
            }

            return hit;
        }

        return super.tryAttack(world, target);
    }

    private void onKill(ServerWorld world, LivingEntity victim) {
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
        world.spawnParticles(
            ParticleTypes.SOUL,
            victim.getX(),
            victim.getY() + 1.0,
            victim.getZ(),
            15, 0.5, 0.5, 0.5, 0.2
        );
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
