package com.stn.spiders.entity;

import com.stn.spiders.config.STNSpidersConfig;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Webspinner Spider - Control role
 * Places cobwebs on hit, slows movement and attack speed.
 */
public class WebspinnerSpiderEntity extends SpiderEntity {

    public WebspinnerSpiderEntity(EntityType<? extends SpiderEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createWebspinnerAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.MAX_HEALTH, STNSpidersConfig.WEBSPINNER_HEALTH)
            .add(EntityAttributes.MOVEMENT_SPEED, STNSpidersConfig.WEBSPINNER_SPEED)
            .add(EntityAttributes.ATTACK_DAMAGE, STNSpidersConfig.WEBSPINNER_DAMAGE)
            .add(EntityAttributes.FOLLOW_RANGE, 24.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        // Web trail particles
        if (this.random.nextInt(15) == 0 && this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(
                ParticleTypes.ITEM_SNOWBALL,
                this.getX(),
                this.getY() + 0.2,
                this.getZ(),
                1, 0.3, 0, 0.3, 0
            );
        }
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean hit = super.tryAttack(world, target);

        if (hit && target instanceof LivingEntity living) {
            // Apply slowness
            living.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS,
                STNSpidersConfig.WEBSPINNER_SLOWNESS_DURATION,
                STNSpidersConfig.WEBSPINNER_SLOWNESS_LEVEL,
                false,
                true
            ));

            // Try to place cobweb at target's feet
            BlockPos targetPos = target.getBlockPos();
            if (world.getBlockState(targetPos).isAir() && this.random.nextInt(3) == 0) {
                world.setBlockState(targetPos, Blocks.COBWEB.getDefaultState());
                this.playSound(SoundEvents.BLOCK_WOOL_PLACE, 0.5f, 1.5f);
            }

            // Web particles
            world.spawnParticles(
                ParticleTypes.ITEM_SNOWBALL,
                target.getX(),
                target.getY() + 0.5,
                target.getZ(),
                8, 0.5, 0.3, 0.5, 0.05
            );
        }

        return hit;
    }
}
