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
 * Vanguard Skeleton - Frontline melee role
 * Tanky with heavy sword attacks that have slow swing speed.
 * Breaks shields and holds choke points.
 */
public class VanguardSkeletonEntity extends WitherSkeletonEntity implements BlockBreakAnimatable {

    private boolean isBreakingBlock = false;
    private int attackCooldown = 0;

    public VanguardSkeletonEntity(EntityType<? extends WitherSkeletonEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(2, new AvoidSunlightGoal(this));
        this.goalSelector.add(3, new EscapeSunlightGoal(this, 1.0));
        this.goalSelector.add(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));

        this.targetSelector.add(1, new RevengeGoal(this));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, IronGolemEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }

    public static DefaultAttributeContainer.Builder createVanguardAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.MAX_HEALTH, STNSkeletonsConfig.VANGUARD_HEALTH)
            .add(EntityAttributes.MOVEMENT_SPEED, STNSkeletonsConfig.VANGUARD_SPEED)
            .add(EntityAttributes.ATTACK_DAMAGE, STNSkeletonsConfig.VANGUARD_DAMAGE)
            .add(EntityAttributes.ARMOR, STNSkeletonsConfig.VANGUARD_ARMOR)
            .add(EntityAttributes.KNOCKBACK_RESISTANCE, STNSkeletonsConfig.VANGUARD_KNOCKBACK_RESISTANCE)
            .add(EntityAttributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        // Stomping particles
        if (this.isOnGround() && this.getVelocity().horizontalLengthSquared() > 0.01) {
            if (this.random.nextInt(8) == 0 && this.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    1, 0, 0, 0, 0.02
                );
            }
        }
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        if (attackCooldown > 0) {
            return false; // Slow attack rate
        }

        boolean hit = super.tryAttack(world, target);

        if (hit && target instanceof LivingEntity living) {
            attackCooldown = STNSkeletonsConfig.VANGUARD_ATTACK_COOLDOWN;

            // Heavy hit sound
            this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 0.8f);

            // Shield breaking - disable shield for longer
            if (living instanceof PlayerEntity player && player.isBlocking()) {
                // Disable shield by setting cooldown on shield item
                ItemStack activeItem = player.getActiveItem();
                if (activeItem.isOf(Items.SHIELD)) {
                    player.getItemCooldownManager().set(activeItem, 100); // 5 seconds cooldown
                    player.clearActiveItem();
                }

                // Extra knockback
                double dx = player.getX() - this.getX();
                double dz = player.getZ() - this.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0) {
                    player.addVelocity(dx / dist * 0.5, 0.2, dz / dist * 0.5);
                }

                this.playSound(SoundEvents.ITEM_SHIELD_BREAK.value(), 1.0f, 1.0f);
            }

            // Heavy hit particles
            world.spawnParticles(
                ParticleTypes.CRIT,
                target.getX(),
                target.getY() + 1.0,
                target.getZ(),
                5, 0.3, 0.3, 0.3, 0.1
            );
        }

        return hit;
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        // Melee only - no ranged attacks
    }

    @Override
    protected void initEquipment(net.minecraft.util.math.random.Random random, net.minecraft.world.LocalDifficulty localDifficulty) {
        // Heavy iron sword
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        // Chainmail armor for visual
        this.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.CHAINMAIL_CHESTPLATE));
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
