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
            .add(EntityAttributes.GENERIC_MAX_HEALTH, STNZombiesConfig.ELECTRIC_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, STNZombiesConfig.ELECTRIC_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, STNZombiesConfig.ELECTRIC_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
            .add(EntityAttributes.GENERIC_ARMOR, 0.0);
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
        if (this.random.nextInt(5) == 0) {
            this.getWorld().addParticle(
                ParticleTypes.ELECTRIC_SPARK,
                this.getX() + this.random.nextGaussian() * 0.3,
                this.getY() + 1.0 + this.random.nextDouble(),
                this.getZ() + this.random.nextGaussian() * 0.3,
                0, 0, 0
            );
        }
    }

    private void spawnSparkParticles() {
        for (int i = 0; i < 3; i++) {
            this.getWorld().addParticle(
                ParticleTypes.ELECTRIC_SPARK,
                this.getX() + this.random.nextGaussian() * 0.5,
                this.getY() + 1.5,
                this.getZ() + this.random.nextGaussian() * 0.5,
                this.random.nextGaussian() * 0.1,
                0.1,
                this.random.nextGaussian() * 0.1
            );
        }
    }

    private void triggerLightning() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        // Sound effect
        this.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 2.0f, 1.2f);
        this.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 1.0f);

        // Big flash of particles
        for (int i = 0; i < 30; i++) {
            this.getWorld().addParticle(
                ParticleTypes.ELECTRIC_SPARK,
                this.getX() + this.random.nextGaussian(),
                this.getY() + this.random.nextDouble() * 2,
                this.getZ() + this.random.nextGaussian(),
                this.random.nextGaussian() * 0.2,
                0.2,
                this.random.nextGaussian() * 0.2
            );
        }

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
                entity.damage(lightningDamage, damage);

                // Spark particles on hit entity
                for (int i = 0; i < 5; i++) {
                    entity.getWorld().addParticle(
                        ParticleTypes.ELECTRIC_SPARK,
                        entity.getX() + this.random.nextGaussian() * 0.3,
                        entity.getY() + 1.0,
                        entity.getZ() + this.random.nextGaussian() * 0.3,
                        0, 0.1, 0
                    );
                }
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
