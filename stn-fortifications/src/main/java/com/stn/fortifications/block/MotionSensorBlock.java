package com.stn.fortifications.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

import java.util.List;

/**
 * Wall-mounted motion sensor that detects hostile mobs.
 * Outputs redstone signal when hostile entities are detected in front of it.
 * Detection range: 8 blocks in facing direction.
 */
public class MotionSensorBlock extends Block implements Waterloggable {

    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final Property<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    private static final int DETECTION_RANGE = 8;
    private static final int DETECTION_WIDTH = 4;
    private static final int DETECTION_HEIGHT = 3;
    private static final int SCAN_INTERVAL = 10; // ticks (0.5 seconds)

    // Wall-mounted shapes (2 pixels thick)
    private static final VoxelShape NORTH_SHAPE = Block.createCuboidShape(4, 4, 14, 12, 12, 16);
    private static final VoxelShape SOUTH_SHAPE = Block.createCuboidShape(4, 4, 0, 12, 12, 2);
    private static final VoxelShape EAST_SHAPE = Block.createCuboidShape(0, 4, 4, 2, 12, 12);
    private static final VoxelShape WEST_SHAPE = Block.createCuboidShape(14, 4, 4, 16, 12, 12);

    public MotionSensorBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
            .with(POWERED, false)
            .with(FACING, Direction.NORTH)
            .with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED, FACING, WATERLOGGED);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction side = ctx.getSide();
        Direction facing;

        // Place on the wall the player clicked
        if (side.getAxis().isHorizontal()) {
            facing = side;
        } else {
            // If clicking top/bottom, face away from player
            facing = ctx.getHorizontalPlayerFacing().getOpposite();
        }

        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());

        return getDefaultState()
            .with(FACING, facing)
            .with(POWERED, false)
            .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction facing = state.get(FACING);
        BlockPos attachPos = pos.offset(facing.getOpposite());
        return world.getBlockState(attachPos).isSolidBlock(world, attachPos);
    }

    // Update block state when neighbor changes
    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView,
            BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }

        // Check if the block we're attached to was removed
        Direction facing = state.get(FACING);
        if (direction == facing.getOpposite()) {
            BlockPos attachPos = pos.offset(facing.getOpposite());
            if (!world.getBlockState(attachPos).isSolidBlock(world, attachPos)) {
                return net.minecraft.block.Blocks.AIR.getDefaultState();
            }
        }

        return state;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient() && !state.isOf(oldState.getBlock())) {
            // Start scanning for mobs
            world.scheduleBlockTick(pos, this, SCAN_INTERVAL);
        }
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        // Scan for hostile mobs
        boolean detected = detectHostileMobs(world, pos, state.get(FACING));
        boolean currentlyPowered = state.get(POWERED);

        if (detected != currentlyPowered) {
            world.setBlockState(pos, state.with(POWERED, detected), Block.NOTIFY_ALL);

            // Play sound on detection
            if (detected) {
                world.playSound(null, pos, SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(),
                    SoundCategory.BLOCKS, 0.5f, 2.0f);

                // Spawn particles
                spawnDetectionParticles(world, pos, state.get(FACING));
            }

            // Notify neighbors of redstone change
            world.updateNeighbors(pos, this);
            world.updateNeighbors(pos.offset(state.get(FACING).getOpposite()), this);
        }

        // Schedule next scan
        world.scheduleBlockTick(pos, this, SCAN_INTERVAL);
    }

    private boolean detectHostileMobs(ServerWorld world, BlockPos pos, Direction facing) {
        Vec3d sensorPos = Vec3d.ofCenter(pos);
        Vec3d facingVec = Vec3d.of(facing.getVector());

        // Create detection box in front of sensor
        // Box extends DETECTION_RANGE blocks forward, DETECTION_WIDTH blocks to each side
        double minX, minY, minZ, maxX, maxY, maxZ;

        minY = sensorPos.y - DETECTION_HEIGHT;
        maxY = sensorPos.y + DETECTION_HEIGHT;

        switch (facing) {
            case NORTH -> {
                minX = sensorPos.x - DETECTION_WIDTH;
                maxX = sensorPos.x + DETECTION_WIDTH;
                minZ = sensorPos.z - DETECTION_RANGE;
                maxZ = sensorPos.z;
            }
            case SOUTH -> {
                minX = sensorPos.x - DETECTION_WIDTH;
                maxX = sensorPos.x + DETECTION_WIDTH;
                minZ = sensorPos.z;
                maxZ = sensorPos.z + DETECTION_RANGE;
            }
            case EAST -> {
                minX = sensorPos.x;
                maxX = sensorPos.x + DETECTION_RANGE;
                minZ = sensorPos.z - DETECTION_WIDTH;
                maxZ = sensorPos.z + DETECTION_WIDTH;
            }
            case WEST -> {
                minX = sensorPos.x - DETECTION_RANGE;
                maxX = sensorPos.x;
                minZ = sensorPos.z - DETECTION_WIDTH;
                maxZ = sensorPos.z + DETECTION_WIDTH;
            }
            default -> {
                return false;
            }
        }

        Box detectionArea = new Box(minX, minY, minZ, maxX, maxY, maxZ);

        List<HostileEntity> mobs = world.getEntitiesByClass(
            HostileEntity.class,
            detectionArea,
            entity -> !entity.isSpectator() && entity.isAlive()
        );

        return !mobs.isEmpty();
    }

    private void spawnDetectionParticles(ServerWorld world, BlockPos pos, Direction facing) {
        Vec3d particlePos = Vec3d.ofCenter(pos).add(Vec3d.of(facing.getVector()).multiply(0.5));

        for (int i = 0; i < 5; i++) {
            world.spawnParticles(
                ParticleTypes.ELECTRIC_SPARK,
                particlePos.x + (world.random.nextDouble() - 0.5) * 0.3,
                particlePos.y + (world.random.nextDouble() - 0.5) * 0.3,
                particlePos.z + (world.random.nextDouble() - 0.5) * 0.3,
                1,
                0.05, 0.05, 0.05,
                0.01
            );
        }
    }

    // Redstone output methods

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWERED) ? 15 : 0;
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        // Strong power only to the block it's attached to
        if (state.get(POWERED) && direction == state.get(FACING)) {
            return 15;
        }
        return 0;
    }
}
