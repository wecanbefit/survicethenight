package com.stn.traders.quest;

import com.stn.traders.STNTraders;
import com.stn.traders.network.QuestNetworking;
import com.stn.traders.screen.QuestScreenHandler;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Event handlers for quest progress tracking.
 */
public class QuestEventHandlers {

    /**
     * Register all quest-related event handlers.
     */
    public static void register() {
        // Track kills for KILL quests
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killedEntity) -> {
            if (entity instanceof ServerPlayerEntity player) {
                onEntityKilled(player, killedEntity);
            }
        });

        // Track player deaths for quest failure
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayerEntity player) {
                onPlayerDeath(player);
            }
        });

        // Detect "Jobs" signs and open quest screen
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient()) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            var state = world.getBlockState(pos);

            // Check if it's a sign
            if (state.getBlock() instanceof AbstractSignBlock) {
                var blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof SignBlockEntity sign) {
                    if (isJobsSign(sign)) {
                        // Open quest screen
                        if (player instanceof ServerPlayerEntity serverPlayer) {
                            // Send quest data sync packet
                            QuestNetworking.sendQuestSync(serverPlayer);

                            // Open the quest screen
                            serverPlayer.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                                (syncId, playerInventory, playerEntity) -> new QuestScreenHandler(syncId, playerInventory),
                                Text.translatable("screen.stn_traders.jobs_board")
                            ));
                            return ActionResult.SUCCESS;
                        }
                    }
                }
            }
            return ActionResult.PASS;
        });

        STNTraders.LOGGER.info("Registered quest event handlers");
    }

    /**
     * Check if a sign has "Jobs" text on it.
     */
    private static boolean isJobsSign(SignBlockEntity sign) {
        // Check both front and back text
        return hasJobsText(sign.getFrontText()) || hasJobsText(sign.getBackText());
    }

    private static boolean hasJobsText(SignText text) {
        for (int i = 0; i < 4; i++) {
            String line = text.getMessage(i, false).getString().toLowerCase();
            if (line.contains("job") || line.contains("quest")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Called when a player kills an entity.
     */
    private static void onEntityKilled(ServerPlayerEntity player, Entity killed) {
        if (!(killed instanceof LivingEntity)) return;

        // Get the entity type identifier
        Identifier entityType = Registries.ENTITY_TYPE.getId(killed.getType());

        // Add progress to matching kill quests
        QuestManager.addKillProgress(player, entityType);
    }

    /**
     * Called when a player dies.
     */
    private static void onPlayerDeath(ServerPlayerEntity player) {
        QuestManager.onPlayerDeath(player);
    }
}
