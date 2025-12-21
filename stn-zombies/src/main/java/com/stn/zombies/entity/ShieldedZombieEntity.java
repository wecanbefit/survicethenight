package com.stn.zombies.entity;

import com.stn.mobai.entity.BlockBreakAnimatable;
import com.stn.zombies.config.STNZombiesConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Shielded Zombie - Formation breaker role
 * Uses a shield to block frontal attacks. Vulnerable from sides and back.
 */
public class ShieldedZombieEntity extends ZombieEntity implements BlockBreakAnimatable {

    private boolean isBreakingBlock = false;
    private boolean isBlocking = true;
    private int blockBrokenCooldown = 0;

    public ShieldedZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initEquipment(net.minecraft.util.math.random.Random random, net.minecraft.world.LocalDifficulty localDifficulty) {
        super.initEquipment(random, localDifficulty);
        // Always spawn with shield
        this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
    }

    public static DefaultAttributeContainer.Builder createShieldedAttributes() {
        return ZombieEntity.createZombieAttributes()
            .add(EntityAttributes.MAX_HEALTH, STNZombiesConfig.SHIELDED_HEALTH)
            .add(EntityAttributes.MOVEMENT_SPEED, STNZombiesConfig.SHIELDED_SPEED)
            .add(EntityAttributes.ATTACK_DAMAGE, STNZombiesConfig.SHIELDED_DAMAGE)
            .add(EntityAttributes.FOLLOW_RANGE, 32.0)
            .add(EntityAttributes.ARMOR, 2.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (blockBrokenCooldown > 0) {
            blockBrokenCooldown--;
            if (blockBrokenCooldown <= 0) {
                isBlocking = true;
            }
        }

        // Re-equip shield if lost
        if (this.getOffHandStack().isEmpty() && !this.getWorld().isClient()) {
            this.equipStack(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (isBlocking && source.getAttacker() != null && !this.getWorld().isClient()) {
            // Check if attack is from the front
            Vec3d attackerPos = source.getAttacker().getPos();
            Vec3d myPos = this.getPos();
            Vec3d toAttacker = attackerPos.subtract(myPos).normalize();

            // Get facing direction
            Vec3d facing = Vec3d.fromPolar(0, this.getYaw()).normalize();

            // Dot product: 1 = directly in front, -1 = directly behind
            double dot = facing.dotProduct(toAttacker);

            // Convert block angle to dot product threshold
            // 90 degrees = cos(45) = ~0.7 on each side
            double threshold = Math.cos(Math.toRadians(STNZombiesConfig.SHIELDED_BLOCK_ANGLE / 2));

            if (dot > threshold) {
                // Attack is from the front - block it!
                this.playSound(SoundEvents.ITEM_SHIELD_BLOCK.value(), 1.0f, 1.0f);

                // Spawn block particles
                world.spawnParticles(
                    ParticleTypes.CRIT,
                    this.getX() + facing.x * 0.5,
                    this.getY() + 1.2,
                    this.getZ() + facing.z * 0.5,
                    5, 0.1, 0.1, 0.1, 0.1
                );

                // Reduce damage significantly
                amount *= 0.2f;

                // Shield takes damage, can break temporarily
                if (this.random.nextFloat() < 0.15f) {
                    breakShieldTemporarily();
                }
            }
        }

        return super.damage(world, source, amount);
    }

    private void breakShieldTemporarily() {
        isBlocking = false;
        blockBrokenCooldown = 60; // 3 seconds

        this.playSound(SoundEvents.ITEM_SHIELD_BREAK.value(), 1.0f, 1.0f);

        if (this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(
                ParticleTypes.CRIT,
                this.getX(),
                this.getY() + 1.0,
                this.getZ(),
                10, 0.5, 0.5, 0.5, 0.1
            );
        }
    }

    public boolean isBlocking() {
        return isBlocking;
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
        return false; // Shield protects from sunlight
    }
}
