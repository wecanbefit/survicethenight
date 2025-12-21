package com.stn.zombies.entity;

import com.stn.mobai.entity.BlockBreakAnimatable;
import com.stn.zombies.config.STNZombiesConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

/**
 * Electric Zombie - Area denial role
 * Occasionally summons lightning that strikes itself and damages nearby entities.
 */
public class ElectricZombieEntity extends ZombieEntity implements BlockBreakAnimatable {

    private boolean isBreakingBlock = false;
    private int lightningCooldown = 0;
    private int chargeBuildup = 0;
    private boolean isCharged = false;

    public ElectricZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createElectricAttributes() {
        return ZombieEntity.createZombieAttributes()
            .add(EntityAttributes.MAX_HEALTH, STNZombiesConfig.ELECTRIC_HEALTH)
            .add(EntityAttributes.MOVEMENT_SPEED, STNZombiesConfig.ELECTRIC_SPEED)
            .add(EntityAttributes.ATTACK_DAMAGE, STNZombiesConfig.ELECTRIC_DAMAGE)
            .add(EntityAttributes.FOLLOW_RANGE, 32.0)
            .add(EntityAttributes.ARMOR, 0.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (lightningCooldown > 0) {
            lightningCooldown--;
        }

        if (!this.getWorld().isClient()) {
            // Build up charge over time
            if (this.getTarget() != null && lightningCooldown <= 0) {
                chargeBuildup++;

                // Spark particles as charge builds
                if (chargeBuildup % 10 == 0) {
                    spawnSparkParticles();
                }

                // Trigger lightning at full charge
                if (chargeBuildup >= 60) { // 3 seconds to charge
                    triggerLightning();
                    chargeBuildup = 0;
                    lightningCooldown = STNZombiesConfig.ELECTRIC_LIGHTNING_COOLDOWN;
                }
            }
        }

        // Constant electric particles
        if (this.random.nextInt(5) == 0 && this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(
                ParticleTypes.ELECTRIC_SPARK,
                this.getX() + this.random.nextGaussian() * 0.3,
                this.getY() + 1.0 + this.random.nextDouble(),
                this.getZ() + this.random.nextGaussian() * 0.3,
                1, 0, 0, 0, 0
            );
        }
    }

    private void spawnSparkParticles() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;
        for (int i = 0; i < 3; i++) {
            serverWorld.spawnParticles(
                ParticleTypes.ELECTRIC_SPARK,
                this.getX() + this.random.nextGaussian() * 0.5,
                this.getY() + 1.5,
                this.getZ() + this.random.nextGaussian() * 0.5,
                1, 0.1, 0.1, 0.1, 0.1
            );
        }
    }

    private void triggerLightning() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        // Sound effect
        this.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.2f);
        this.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 1.0f);

        // Big flash of particles
        serverWorld.spawnParticles(
            ParticleTypes.ELECTRIC_SPARK,
            this.getX(),
            this.getY() + 1.0,
            this.getZ(),
            30, 1.0, 1.0, 1.0, 0.2
        );

        // Damage nearby entities in AOE
        double radius = STNZombiesConfig.ELECTRIC_AOE_RADIUS;
        Box aoeBox = new Box(this.getBlockPos()).expand(radius);

        List<LivingEntity> nearbyEntities = serverWorld.getEntitiesByClass(
            LivingEntity.class,
            aoeBox,
            entity -> entity != this && !(entity instanceof ZombieEntity)
        );

        DamageSource lightningDamage = serverWorld.getDamageSources().create(DamageTypes.LIGHTNING_BOLT);

        for (LivingEntity entity : nearbyEntities) {
            double distance = this.distanceTo(entity);
            if (distance <= radius) {
                // Damage falls off with distance
                float damage = (float) (STNZombiesConfig.ELECTRIC_LIGHTNING_DAMAGE * (1.0 - distance / radius));
                entity.damage(serverWorld, lightningDamage, damage);

                // Spark particles on hit entity
                serverWorld.spawnParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    entity.getX(),
                    entity.getY() + 1.0,
                    entity.getZ(),
                    5, 0.3, 0.3, 0.3, 0.1
                );
            }
        }
    }

    public boolean isCharged() {
        return chargeBuildup > 30;
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
