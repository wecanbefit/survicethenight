package com.stn.fortifications.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Spike block that damages entities walking on it.
 * Inspired by 7 Days to Die's spike traps.
 */
public class SpikeBlock extends Block {

    public static final IntProperty DAMAGE_COUNT = IntProperty.of("damage_count", 0, 15);

    private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.5, 1.0);

    private final float spikeDamage;
    private final int maxDurability;

    public SpikeBlock(Settings settings, float damage, int durability) {
        super(settings);
        this.spikeDamage = damage;
        this.maxDurability = durability;
        this.setDefaultState(this.stateManager.getDefaultState().with(DAMAGE_COUNT, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DAMAGE_COUNT);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    // Called when entity collides with block
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient() || !(world instanceof ServerWorld serverWorld)) {
            return;
        }

        if (entity instanceof LivingEntity livingEntity) {
            // Don't damage players in creative mode
            if (livingEntity instanceof PlayerEntity player && player.isCreative()) {
                return;
            }

            // Damage the entity
            DamageSource damageSource = serverWorld.getDamageSources().cactus();

            // Hostile entities take more damage from spikes
            float damage = spikeDamage;
            if (livingEntity instanceof HostileEntity) {
                damage *= 1.5f;
            }

            livingEntity.damage(serverWorld, damageSource, damage);

            // Slow the entity
            livingEntity.setVelocity(
                livingEntity.getVelocity().multiply(0.5, 1.0, 0.5)
            );

            // Visual effects
            serverWorld.spawnParticles(
                ParticleTypes.DAMAGE_INDICATOR,
                entity.getX(),
                entity.getY() + 0.5,
                entity.getZ(),
                3,
                0.2, 0.2, 0.2,
                0.1
            );

            world.playSound(
                null,
                pos,
                SoundEvents.ENTITY_PLAYER_HURT,
                SoundCategory.BLOCKS,
                0.5f,
                1.0f
            );

            // Degrade the spike block
            degradeSpike(world, pos, state);
        }
    }

    private void degradeSpike(World world, BlockPos pos, BlockState state) {
        int currentDamage = state.get(DAMAGE_COUNT);
        int damagePerHit = Math.max(1, 16 / (maxDurability / 50));

        int newDamage = currentDamage + damagePerHit;

        if (newDamage >= 15) {
            // Spike is destroyed
            world.breakBlock(pos, false);
            world.playSound(
                null,
                pos,
                SoundEvents.BLOCK_WOOD_BREAK,
                SoundCategory.BLOCKS,
                1.0f,
                1.0f
            );
        } else if (newDamage > currentDamage) {
            // Update damage state
            world.setBlockState(pos, state.with(DAMAGE_COUNT, Math.min(newDamage, 15)));
        }
    }

}
