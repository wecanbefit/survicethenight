package com.stn.traders.block;

import com.stn.traders.network.QuestNetworking;
import com.stn.traders.screen.QuestScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Jobs Board block where players accept and turn in quests.
 */
public class JobsBoardBlock extends Block {
    public static final Property<Direction> FACING = Properties.HORIZONTAL_FACING;

    // Sign-like shape (thin in one direction)
    private static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(0, 0, 14, 16, 16, 16);
    private static final VoxelShape SHAPE_SOUTH = Block.createCuboidShape(0, 0, 0, 16, 16, 2);
    private static final VoxelShape SHAPE_EAST = Block.createCuboidShape(0, 0, 0, 2, 16, 16);
    private static final VoxelShape SHAPE_WEST = Block.createCuboidShape(14, 0, 0, 16, 16, 16);

    public JobsBoardBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                  PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        if (player instanceof ServerPlayerEntity serverPlayer) {
            // Send quest data to client before opening screen
            QuestNetworking.sendQuestSync(serverPlayer);

            // Open the quest screen
            serverPlayer.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, playerEntity) -> new QuestScreenHandler(syncId, playerInventory),
                Text.translatable("screen.stn_traders.jobs_board")
            ));
        }

        return ActionResult.CONSUME;
    }
}
