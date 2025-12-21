package com.stn.zombies.entity;

import com.stn.mobai.entity.BlockBreakAnimatable;
import com.stn.zombies.config.STNZombiesConfig;
import com.stn.zombies.entity.ai.SpitAttackGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Spitter Zombie - Ranged harassment role
 * Shoots acid projectiles that apply poison and damage blocks.
 */
public class SpitterZombieEntity extends ZombieEntity implements BlockBreakAnimatable {

    private boolean isBreakingBlock = false;
    private int spitCooldown = 0;

    public SpitterZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(2, new SpitAttackGoal(this, 1.0, STNZombiesConfig.SPITTER_ATTACK_COOLDOWN, 10.0f));
    }

    public static DefaultAttributeContainer.Builder createSpitterAttributes() {
        return ZombieEntity.createZombieAttributes()
            .add(EntityAttributes.MAX_HEALTH, STNZombiesConfig.SPITTER_HEALTH)
            .add(EntityAttributes.MOVEMENT_SPEED, STNZombiesConfig.SPITTER_SPEED)
            .add(EntityAttributes.ATTACK_DAMAGE, STNZombiesConfig.SPITTER_DAMAGE)
            .add(EntityAttributes.FOLLOW_RANGE, 24.0)
            .add(EntityAttributes.ARMOR, 0.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (spitCooldown > 0) {
            spitCooldown--;
        }

        // Dripping acid particles
        if (this.getWorld() instanceof ServerWorld sw && this.random.nextInt(20) == 0) {
            sw.spawnParticles(
                ParticleTypes.FALLING_DRIPSTONE_WATER,
                this.getX(),
                this.getY() + 1.5,
                this.getZ(),
                1, 0.2, 0, 0.2, 0
            );
        }
    }

    public void spit(LivingEntity target) {
        if (spitCooldown > 0) return;

        spitCooldown = STNZombiesConfig.SPITTER_ATTACK_COOLDOWN;

        // Calculate trajectory
        double dx = target.getX() - this.getX();
        double dy = target.getBodyY(0.5) - this.getBodyY(0.5);
        double dz = target.getZ() - this.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        // Create acid projectile (using small fireball as base, will customize later)
        Vec3d velocity = new Vec3d(dx, dy + distance * 0.2, dz).normalize().multiply(0.8);

        SmallFireballEntity projectile = new SmallFireballEntity(
            this.getWorld(),
            this,
            velocity
        );
        projectile.setPosition(this.getX(), this.getBodyY(0.5) + 0.5, this.getZ());

        this.getWorld().spawnEntity(projectile);

        // Sound and particles
        this.playSound(SoundEvents.ENTITY_LLAMA_SPIT, 1.0f, 0.8f);

        if (this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(
                ParticleTypes.SNEEZE,
                this.getX(),
                this.getY() + 1.5,
                this.getZ(),
                5, 0.1, 0.1, 0.1, 0.1
            );
        }
    }

    public boolean canSpit() {
        return spitCooldown <= 0;
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
