package com.stn.mobai.event;

import com.stn.core.api.ISoundEmitter;
import com.stn.mobai.entity.ai.sense.SenseManager;
import com.stn.mobai.entity.ai.sense.SoundVolumes;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Handles game events and emits corresponding sounds for mob detection.
 */
public class SoundEventHandler {

    public static void register() {
        // Block break events
        PlayerBlockBreakEvents.AFTER.register(SoundEventHandler::onBlockBreak);

        // Block use events (doors, chests, levers, etc.)
        UseBlockCallback.EVENT.register(SoundEventHandler::onBlockUse);

        // Item use events (goat horn, etc.)
        UseItemCallback.EVENT.register(SoundEventHandler::onItemUse);

        // Attack events (weapon sounds)
        AttackEntityCallback.EVENT.register(SoundEventHandler::onAttackEntity);
    }

    private static void onBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (world instanceof ServerWorld serverWorld) {
            float volume = SoundVolumes.getBlockBreakVolume(state);
            SenseManager.getInstance().registerSound(
                serverWorld,
                pos,
                volume,
                player,
                ISoundEmitter.SoundType.BLOCK_BREAK
            );
        }
    }

    private static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        if (world.isClient() || hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }

        BlockPos pos = hitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        ServerWorld serverWorld = (ServerWorld) world;

        // Doors
        if (block instanceof DoorBlock) {
            float volume = (block == Blocks.IRON_DOOR) ?
                SoundVolumes.DOOR_OPEN * 1.2f : SoundVolumes.DOOR_OPEN;
            SenseManager.getInstance().registerSound(
                serverWorld,
                pos,
                volume,
                player,
                ISoundEmitter.SoundType.INTERACTION
            );
        }
        // Trapdoors
        else if (block instanceof TrapdoorBlock) {
            SenseManager.getInstance().registerSound(
                serverWorld,
                pos,
                SoundVolumes.TRAPDOOR_USE,
                player,
                ISoundEmitter.SoundType.INTERACTION
            );
        }
        // Fence gates
        else if (block instanceof FenceGateBlock) {
            SenseManager.getInstance().registerSound(
                serverWorld,
                pos,
                SoundVolumes.FENCE_GATE_USE,
                player,
                ISoundEmitter.SoundType.INTERACTION
            );
        }
        // Chests and barrels
        else if (block instanceof ChestBlock || block instanceof BarrelBlock ||
                 block instanceof EnderChestBlock || block instanceof ShulkerBoxBlock) {
            SenseManager.getInstance().registerSound(
                serverWorld,
                pos,
                SoundVolumes.CHEST_OPEN,
                player,
                ISoundEmitter.SoundType.INTERACTION
            );
        }
        // Levers
        else if (block instanceof LeverBlock) {
            SenseManager.getInstance().registerSound(
                serverWorld,
                pos,
                SoundVolumes.LEVER_USE,
                player,
                ISoundEmitter.SoundType.INTERACTION
            );
        }
        // Buttons
        else if (block instanceof ButtonBlock) {
            SenseManager.getInstance().registerSound(
                serverWorld,
                pos,
                SoundVolumes.BUTTON_USE,
                player,
                ISoundEmitter.SoundType.INTERACTION
            );
        }

        return ActionResult.PASS;
    }

    private static TypedActionResult<ItemStack> onItemUse(PlayerEntity player, World world, Hand hand) {
        if (world.isClient() || hand != Hand.MAIN_HAND) {
            return TypedActionResult.pass(player.getStackInHand(hand));
        }

        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();

        // Goat Horn - very loud, attracts mobs from far away
        if (item instanceof GoatHornItem) {
            ServerWorld serverWorld = (ServerWorld) world;
            SenseManager.getInstance().registerSound(
                serverWorld,
                player.getBlockPos(),
                SoundVolumes.GOAT_HORN,
                player,
                ISoundEmitter.SoundType.GOAT_HORN
            );
        }

        return TypedActionResult.pass(stack);
    }

    private static ActionResult onAttackEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        if (world.isClient() || hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }

        ServerWorld serverWorld = (ServerWorld) world;
        ItemStack heldItem = player.getStackInHand(hand);
        Item item = heldItem.getItem();

        float volume = getWeaponVolume(item);

        SenseManager.getInstance().registerSound(
            serverWorld,
            player.getBlockPos(),
            volume,
            player,
            ISoundEmitter.SoundType.COMBAT
        );

        return ActionResult.PASS;
    }

    private static float getWeaponVolume(Item item) {
        // Mace - very loud
        if (item instanceof MaceItem) {
            return SoundVolumes.WEAPON_MACE;
        }
        // Trident - loud
        if (item instanceof TridentItem) {
            return SoundVolumes.WEAPON_TRIDENT;
        }
        // Axes - medium-loud
        if (item instanceof AxeItem) {
            return SoundVolumes.WEAPON_AXE;
        }
        // Swords - medium
        if (item instanceof SwordItem) {
            return SoundVolumes.WEAPON_SWORD;
        }
        // Other tools (pickaxe, shovel, hoe)
        if (item instanceof ToolItem) {
            return SoundVolumes.WEAPON_DEFAULT;
        }
        // Bare fists or non-weapons
        return SoundVolumes.WEAPON_FIST;
    }
}
