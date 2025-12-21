package com.stn.zombies.entity;

import com.stn.mobai.entity.BlockBreakAnimatable;
import com.stn.mobai.entity.IBlockBreaker;
import com.stn.zombies.config.STNZombiesConfig;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Lumberjack Zombie - Block destruction role
 * Destroys wooden structures faster. Axe attacks cause bleeding.
 */
public class LumberjackZombieEntity extends ZombieEntity implements BlockBreakAnimatable, IBlockBreaker {

    private boolean isBreakingBlock = false;

    public LumberjackZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initEquipment(net.minecraft.util.math.random.Random random, net.minecraft.world.LocalDifficulty localDifficulty) {
        super.initEquipment(random, localDifficulty);
        // Always spawn with iron axe
        this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
    }

    public static DefaultAttributeContainer.Builder createLumberjackAttributes() {
        return ZombieEntity.createZombieAttributes()
            .add(EntityAttributes.MAX_HEALTH, STNZombiesConfig.LUMBERJACK_HEALTH)
            .add(EntityAttributes.MOVEMENT_SPEED, STNZombiesConfig.LUMBERJACK_SPEED)
            .add(EntityAttributes.ATTACK_DAMAGE, STNZombiesConfig.LUMBERJACK_DAMAGE)
            .add(EntityAttributes.FOLLOW_RANGE, 32.0)
            .add(EntityAttributes.ARMOR, 2.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        // Wood chip particles when near wood
        if (this.getWorld() instanceof ServerWorld sw && this.random.nextInt(40) == 0) {
            BlockPos below = this.getBlockPos().down();
            BlockState state = this.getWorld().getBlockState(below);

            if (state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.PLANKS)) {
                sw.spawnParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    this.getX(),
                    this.getY() + 0.1,
                    this.getZ(),
                    1, 0, 0, 0, 0.02
                );
            }
        }

        // Re-equip axe if lost
        if (this.getMainHandStack().isEmpty() && !this.getWorld().isClient()) {
            this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        }
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean hit = super.tryAttack(world, target);

        if (hit && target instanceof LivingEntity living && !this.getWorld().isClient()) {
            // Apply bleeding effect (using instant damage for now, could be custom effect)
            // Bleeding: deals damage over time

            // Visual bleeding effect
            if (target.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(
                    ParticleTypes.DAMAGE_INDICATOR,
                    target.getX(),
                    target.getY() + 1.0,
                    target.getZ(),
                    8, 0.3, 0.3, 0.3, 0.1
                );
            }

            // Apply wither as "bleeding" (damage over time)
            // Low level wither for bleed simulation
            if (target instanceof PlayerEntity player) {
                player.addStatusEffect(new StatusEffectInstance(
                    net.minecraft.entity.effect.StatusEffects.WITHER,
                    STNZombiesConfig.LUMBERJACK_BLEED_DURATION,
                    0, // Level 1 wither
                    false,
                    true
                ));
            }

            this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 0.8f);
        }

        return hit;
    }

    // IBlockBreaker implementation - extra wood damage
    @Override
    public double getWoodBreakMultiplier() {
        return STNZombiesConfig.LUMBERJACK_WOOD_BREAK_MULTIPLIER;
    }

    @Override
    public double getStoneBreakMultiplier() {
        return 0.5; // Worse at stone
    }

    @Override
    public boolean canBreakBlock(BlockState state, BlockPos pos) {
        // Prioritize wood, but can break anything
        return true;
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
        return false; // Adapted to outdoor work
    }
}
