package com.stn.traders.protection;

import com.stn.traders.STNTraders;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Event handlers to prevent all player interaction with protected blocks.
 * Cancels block breaking, block use (doors, levers, etc.), and block attacks.
 */
public class BlockProtectionEvents {

    // Cooldown to prevent message spam
    private static final long MESSAGE_COOLDOWN_MS = 2000;
    private static long lastMessageTime = 0;

    /**
     * Register all protection event handlers.
     */
    public static void register() {
        // Cancel block breaking BEFORE it happens
        PlayerBlockBreakEvents.BEFORE.register(BlockProtectionEvents::onBlockBreak);

        // Cancel block USE (doors, chests, levers, buttons, etc.)
        UseBlockCallback.EVENT.register(BlockProtectionEvents::onBlockUse);

        // Cancel block ATTACK (left-click, mining start)
        AttackBlockCallback.EVENT.register(BlockProtectionEvents::onBlockAttack);

        STNTraders.LOGGER.info("Block protection event handlers registered");
    }

    /**
     * Cancel block breaking for protected blocks.
     * @return true to allow break, false to cancel
     */
    private static boolean onBlockBreak(World world, PlayerEntity player, BlockPos pos,
                                        BlockState state, BlockEntity blockEntity) {
        if (world instanceof ServerWorld serverWorld) {
            // Check region-based protection (legacy)
            if (TraderProtectionState.get(serverWorld).isProtected(pos)) {
                sendProtectionMessage(player);
                return false; // Cancel break
            }
            // Check structure-based protection (datapack worldgen)
            if (isInTraderStructure(serverWorld, pos)) {
                sendProtectionMessage(player);
                return false; // Cancel break
            }
        }
        return true; // Allow break
    }

    /**
     * Cancel block use (right-click) for protected blocks, except interactable blocks.
     */
    private static ActionResult onBlockUse(PlayerEntity player, World world,
                                           Hand hand, BlockHitResult hitResult) {
        if (world instanceof ServerWorld serverWorld) {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);

            // Allow interaction with doors, gates, and jobs board
            if (isInteractableBlock(state)) {
                return ActionResult.PASS;
            }

            if (TraderProtectionState.get(serverWorld).isProtected(pos)) {
                sendProtectionMessage(player);
                return ActionResult.FAIL; // Cancel interaction
            }
            if (isInTraderStructure(serverWorld, pos)) {
                sendProtectionMessage(player);
                return ActionResult.FAIL; // Cancel interaction
            }
        }
        return ActionResult.PASS; // Allow
    }

    /**
     * Check if a block is interactable (doors, gates, jobs board, signs, etc.)
     */
    private static boolean isInteractableBlock(BlockState state) {
        var block = state.getBlock();
        // Allow doors, fence gates, trapdoors, signs, and jobs board
        return block instanceof net.minecraft.block.DoorBlock
            || block instanceof net.minecraft.block.FenceGateBlock
            || block instanceof net.minecraft.block.TrapdoorBlock
            || block instanceof net.minecraft.block.AbstractSignBlock
            || block instanceof com.stn.traders.block.JobsBoardBlock;
    }

    /**
     * Cancel block attack (left-click/mining start) for protected blocks.
     */
    private static ActionResult onBlockAttack(PlayerEntity player, World world,
                                              Hand hand, BlockPos pos, Direction direction) {
        if (world instanceof ServerWorld serverWorld) {
            if (TraderProtectionState.get(serverWorld).isProtected(pos)) {
                sendProtectionMessage(player);
                return ActionResult.FAIL; // Cancel attack
            }
            if (isInTraderStructure(serverWorld, pos)) {
                sendProtectionMessage(player);
                return ActionResult.FAIL; // Cancel attack
            }
        }
        return ActionResult.PASS; // Allow
    }

    /**
     * Check if a position is inside a trader_outpost structure.
     */
    private static final Identifier TRADER_STRUCTURE_ID = Identifier.of(STNTraders.MOD_ID, "trader_outpost");

    private static boolean isInTraderStructure(ServerWorld world, BlockPos pos) {
        var structureAccessor = world.getStructureAccessor();

        // Use predicate to match our structure
        StructureStart start = structureAccessor.getStructureContaining(pos, entry -> {
            var key = entry.getKey();
            return key.isPresent() && key.get().getValue().equals(TRADER_STRUCTURE_ID);
        });

        return start.hasChildren();
    }

    /**
     * Send a rate-limited message to the player about protection.
     */
    private static void sendProtectionMessage(PlayerEntity player) {
        long now = System.currentTimeMillis();
        if (now - lastMessageTime > MESSAGE_COOLDOWN_MS) {
            lastMessageTime = now;
            player.sendMessage(
                Text.literal("This structure is protected.").withColor(0xFF5555),
                true // Action bar
            );
        }
    }
}
