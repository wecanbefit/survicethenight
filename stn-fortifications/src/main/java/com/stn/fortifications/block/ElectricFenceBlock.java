package com.stn.fortifications.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Electric fence that requires redstone power.
 * Stuns and damages zombies when powered.
 * Connects horizontally like a fence.
 * Power chains through connected fences like powered rails.
 */
public class ElectricFenceBlock extends Block implements Waterloggable {

    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty NORTH = Properties.NORTH;
    public static final BooleanProperty SOUTH = Properties.SOUTH;
    public static final BooleanProperty EAST = Properties.EAST;
    public static final BooleanProperty WEST = Properties.WEST;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final IntProperty DAMAGE_COUNT = IntProperty.of("damage_count", 0, 15);

    private static final VoxelShape POST_SHAPE = Block.createCuboidShape(6, 0, 6, 10, 16, 10);
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(6, 0, 0, 10, 16, 6);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(6, 0, 10, 10, 16, 16);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(10, 0, 6, 16, 16, 10);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(0, 0, 6, 6, 16, 10);

    private static final float SHOCK_DAMAGE = 4.0f;
    private static final int STUN_DURATION = 40; // 2 seconds
    private static final int MAX_CHAIN_LENGTH = 64; // Maximum fences that can be powered from one source
    private static final int MAX_DURABILITY = 300; // More durable than barbed wire

    public ElectricFenceBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
            .with(POWERED, false)
            .with(NORTH, false)
            .with(SOUTH, false)
            .with(EAST, false)
            .with(WEST, false)
            .with(WATERLOGGED, false)
            .with(DAMAGE_COUNT, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED, NORTH, SOUTH, EAST, WEST, WATERLOGGED, DAMAGE_COUNT);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        FluidState fluidState = world.getFluidState(pos);

        BlockState northState = world.getBlockState(pos.north());
        BlockState southState = world.getBlockState(pos.south());
        BlockState eastState = world.getBlockState(pos.east());
        BlockState westState = world.getBlockState(pos.west());

        // Check if we receive power directly or through chain
        boolean powered = world.isReceivingRedstonePower(pos) || isConnectedToPowerSource(world, pos);

        return getDefaultState()
            .with(POWERED, powered)
            .with(NORTH, canConnect(northState, world, pos.north()))
            .with(SOUTH, canConnect(southState, world, pos.south()))
            .with(EAST, canConnect(eastState, world, pos.east()))
            .with(WEST, canConnect(westState, world, pos.west()))
            .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    private boolean canConnect(BlockState state, BlockView world, BlockPos pos) {
        return state.getBlock() instanceof ElectricFenceBlock || state.isSolidBlock(world, pos);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
            WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (state.get(WATERLOGGED)) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        if (direction.getAxis().isHorizontal()) {
            boolean shouldConnect = neighborState.getBlock() instanceof ElectricFenceBlock ||
                neighborState.isSolidBlock((BlockView) world, neighborPos);

            state = switch (direction) {
                case NORTH -> state.with(NORTH, shouldConnect);
                case SOUTH -> state.with(SOUTH, shouldConnect);
                case EAST -> state.with(EAST, shouldConnect);
                case WEST -> state.with(WEST, shouldConnect);
                default -> state;
            };

            // Schedule a tick to recalculate power when a neighbor changes
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.scheduleBlockTick(pos, this, 1);
            }
        }
        return state;
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        // Recalculate power state
        boolean shouldBePowered = isConnectedToPowerSource(world, pos);
        if (state.get(POWERED) != shouldBePowered) {
            world.setBlockState(pos, state.with(POWERED, shouldBePowered), Block.NOTIFY_ALL);

            if (shouldBePowered) {
                world.playSound(null, pos, SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.BLOCKS, 0.3f, 2.0f);
            }
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape shape = POST_SHAPE;
        if (state.get(NORTH)) shape = VoxelShapes.union(shape, NORTH_SHAPE);
        if (state.get(SOUTH)) shape = VoxelShapes.union(shape, SOUTH_SHAPE);
        if (state.get(EAST)) shape = VoxelShapes.union(shape, EAST_SHAPE);
        if (state.get(WEST)) shape = VoxelShapes.union(shape, WEST_SHAPE);
        return shape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // No collision when powered (entities pass through and get shocked)
        return state.get(POWERED) ? VoxelShapes.empty() : getOutlineShape(state, world, pos, context);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isClient()) {
            // Schedule tick to recalculate power
            world.scheduleBlockTick(pos, this, 1);
        }
    }

    /**
     * When a fence is broken, notify all connected fences to recalculate power
     */
    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            // This fence was removed, schedule updates for neighbors
            if (!world.isClient()) {
                for (Direction dir : Direction.Type.HORIZONTAL) {
                    BlockPos neighborPos = pos.offset(dir);
                    BlockState neighborState = world.getBlockState(neighborPos);
                    if (neighborState.getBlock() instanceof ElectricFenceBlock) {
                        world.scheduleBlockTick(neighborPos, this, 1);
                    }
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    /**
     * Check if this fence is connected to a redstone power source through the chain
     */
    private boolean isConnectedToPowerSource(World world, BlockPos startPos) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(startPos);

        while (!queue.isEmpty() && visited.size() < MAX_CHAIN_LENGTH) {
            BlockPos pos = queue.poll();
            if (visited.contains(pos)) continue;
            visited.add(pos);

            // Check if this position receives redstone power directly
            if (world.isReceivingRedstonePower(pos)) {
                return true;
            }

            // Add connected electric fences to queue
            for (Direction dir : Direction.Type.HORIZONTAL) {
                BlockPos neighborPos = pos.offset(dir);
                BlockState neighborState = world.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof ElectricFenceBlock && !visited.contains(neighborPos)) {
                    queue.add(neighborPos);
                }
            }
        }

        return false;
    }

    /**
     * Recalculate power for all connected fences
     */
    private void recalculateChainPower(World world, BlockPos startPos) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(startPos);

        // First, find if any fence in the chain has direct power
        boolean chainHasPower = isConnectedToPowerSource(world, startPos);

        // Reset queue for updating
        visited.clear();
        queue.clear();
        queue.add(startPos);

        while (!queue.isEmpty() && visited.size() < MAX_CHAIN_LENGTH) {
            BlockPos pos = queue.poll();
            if (visited.contains(pos)) continue;
            visited.add(pos);

            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof ElectricFenceBlock) {
                // Update power state if different
                if (state.get(POWERED) != chainHasPower) {
                    world.setBlockState(pos, state.with(POWERED, chainHasPower), Block.NOTIFY_ALL);
                }

                // Add connected electric fences to queue
                for (Direction dir : Direction.Type.HORIZONTAL) {
                    BlockPos neighborPos = pos.offset(dir);
                    BlockState neighborState = world.getBlockState(neighborPos);
                    if (neighborState.getBlock() instanceof ElectricFenceBlock && !visited.contains(neighborPos)) {
                        queue.add(neighborPos);
                    }
                }
            }
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient() || !state.get(POWERED)) {
            return;
        }

        if (entity instanceof LivingEntity livingEntity) {
            // Don't shock players in creative
            if (livingEntity instanceof PlayerEntity player && player.isCreative()) {
                return;
            }

            // Apply shock damage
            float damage = SHOCK_DAMAGE;

            // Extra damage to hostile mobs
            if (livingEntity instanceof HostileEntity) {
                damage *= 2.0f;
            }

            DamageSource damageSource = new DamageSource(
                world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(DamageTypes.LIGHTNING_BOLT)
            );

            livingEntity.damage(damageSource, damage);

            // Apply stun effect (slowness + weakness)
            livingEntity.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS, STUN_DURATION, 4, false, true
            ));
            livingEntity.addStatusEffect(new StatusEffectInstance(
                StatusEffects.WEAKNESS, STUN_DURATION, 2, false, true
            ));

            // Knockback
            livingEntity.setVelocity(
                livingEntity.getVelocity().add(
                    (world.random.nextDouble() - 0.5) * 0.5,
                    0.3,
                    (world.random.nextDouble() - 0.5) * 0.5
                )
            );

            // Visual effects
            if (world instanceof ServerWorld serverWorld) {
                // Electric spark particles
                for (int i = 0; i < 10; i++) {
                    serverWorld.spawnParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        entity.getX() + (world.random.nextDouble() - 0.5) * 0.5,
                        entity.getY() + world.random.nextDouble() * entity.getHeight(),
                        entity.getZ() + (world.random.nextDouble() - 0.5) * 0.5,
                        1,
                        0.1, 0.1, 0.1,
                        0.1
                    );
                }
            }

            // Zap sound
            world.playSound(
                null,
                pos,
                SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT,
                SoundCategory.BLOCKS,
                0.5f,
                1.5f + world.random.nextFloat() * 0.5f
            );

            // Degrade the electric fence
            degradeFence(world, pos, state);
        }
    }

    private void degradeFence(World world, BlockPos pos, BlockState state) {
        int currentDamage = state.get(DAMAGE_COUNT);
        int damagePerHit = Math.max(1, 16 / (MAX_DURABILITY / 50));

        int newDamage = currentDamage + damagePerHit;

        if (newDamage >= 15) {
            // Fence is destroyed
            world.breakBlock(pos, false);
            world.playSound(
                null,
                pos,
                SoundEvents.BLOCK_ANVIL_BREAK,
                SoundCategory.BLOCKS,
                1.0f,
                1.2f
            );
        } else if (newDamage > currentDamage) {
            // Update damage state
            world.setBlockState(pos, state.with(DAMAGE_COUNT, Math.min(newDamage, 15)));
        }
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient() && !state.isOf(oldState.getBlock())) {
            // Schedule tick to calculate initial power state
            world.scheduleBlockTick(pos, this, 1);
        }
    }
}
