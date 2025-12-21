package com.stn.fortifications.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

/**
 * Barbed wire that slows and damages entities passing through.
 * Connects horizontally like a fence.
 */
public class BarbedWireBlock extends Block implements Waterloggable {

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

    private static final float DAMAGE = 1.5f;
    private static final double SLOW_FACTOR = 0.25;
    private static final int MAX_DURABILITY = 200; // How many hits before breaking

    public BarbedWireBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
            .with(NORTH, false)
            .with(SOUTH, false)
            .with(EAST, false)
            .with(WEST, false)
            .with(WATERLOGGED, false)
            .with(DAMAGE_COUNT, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH, EAST, WEST, WATERLOGGED, DAMAGE_COUNT);
    }

    private boolean canConnect(BlockState state, BlockView world, BlockPos pos) {
        return state.getBlock() instanceof BarbedWireBlock ||
               state.getBlock() instanceof ElectricFenceBlock ||
               state.getBlock() instanceof FenceGateBlock ||
               state.isSolidBlock(world, pos);
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

        return getDefaultState()
            .with(NORTH, canConnect(northState, world, pos.north()))
            .with(SOUTH, canConnect(southState, world, pos.south()))
            .with(EAST, canConnect(eastState, world, pos.east()))
            .with(WEST, canConnect(westState, world, pos.west()))
            .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    // Update block state when neighbor changes
    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView,
            BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        if (direction.getAxis().isHorizontal()) {
            boolean shouldConnect = canConnect(neighborState, world, neighborPos);

            return switch (direction) {
                case NORTH -> state.with(NORTH, shouldConnect);
                case SOUTH -> state.with(SOUTH, shouldConnect);
                case EAST -> state.with(EAST, shouldConnect);
                case WEST -> state.with(WEST, shouldConnect);
                default -> state;
            };
        }
        return state;
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
        // No collision - entities can walk through but get damaged
        return VoxelShapes.empty();
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    // Called when entity collides with block
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient() || !(world instanceof ServerWorld serverWorld)) {
            return;
        }

        if (entity instanceof LivingEntity livingEntity) {
            // Don't affect players in creative
            if (livingEntity instanceof PlayerEntity player && player.isCreative()) {
                return;
            }

            // Slow the entity significantly
            entity.setVelocity(entity.getVelocity().multiply(SLOW_FACTOR, 1.0, SLOW_FACTOR));
            entity.slowMovement(state, new net.minecraft.util.math.Vec3d(SLOW_FACTOR, 1.0, SLOW_FACTOR));

            // Damage on movement
            if (entity.getVelocity().horizontalLengthSquared() > 0.0001) {
                float damage = DAMAGE;

                // More damage to hostile mobs
                if (livingEntity instanceof HostileEntity) {
                    damage *= 2.0f;
                }

                DamageSource damageSource = serverWorld.getDamageSources().cactus();

                livingEntity.damage(serverWorld, damageSource, damage);

                // Visual effects
                serverWorld.spawnParticles(
                        ParticleTypes.DAMAGE_INDICATOR,
                        entity.getX(),
                        entity.getY() + 0.5,
                        entity.getZ(),
                        1,
                        0.1, 0.1, 0.1,
                        0.05
                );

                // Sound effect
                if (world.random.nextInt(5) == 0) {
                    world.playSound(
                        null,
                        pos,
                        SoundEvents.BLOCK_CHAIN_BREAK,
                        SoundCategory.BLOCKS,
                        0.3f,
                        1.5f
                    );
                }

                // Degrade the barbed wire
                degradeWire(world, pos, state);
            }
        }
    }

    private void degradeWire(World world, BlockPos pos, BlockState state) {
        int currentDamage = state.get(DAMAGE_COUNT);
        int damagePerHit = Math.max(1, 16 / (MAX_DURABILITY / 50));

        int newDamage = currentDamage + damagePerHit;

        if (newDamage >= 15) {
            // Wire is destroyed
            world.breakBlock(pos, false);
            world.playSound(
                null,
                pos,
                SoundEvents.BLOCK_CHAIN_BREAK,
                SoundCategory.BLOCKS,
                1.0f,
                0.8f
            );
        } else if (newDamage > currentDamage) {
            // Update damage state
            world.setBlockState(pos, state.with(DAMAGE_COUNT, Math.min(newDamage, 15)));
        }
    }

}
