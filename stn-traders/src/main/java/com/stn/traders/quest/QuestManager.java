package com.stn.traders.quest;

import com.stn.core.STNCore;
import com.stn.traders.STNTraders;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

/**
 * Central manager for quest operations.
 */
public class QuestManager {

    /**
     * Initialize the quest system.
     */
    public static void init() {
        QuestConfigManager.load();
        STNTraders.LOGGER.info("Quest system initialized with {} quests", QuestConfigManager.getQuests().size());
    }

    /**
     * Get available quests for a player to choose from.
     */
    public static List<Quest> getAvailableQuests(ServerPlayerEntity player) {
        int gamestage = getGamestage(player);
        List<String> activeIds = PlayerQuestState.get((ServerWorld) player.getWorld())
            .getActiveQuestIds(player.getUuid());
        return QuestSelector.getAvailableQuests(gamestage).stream()
            .filter(q -> !activeIds.contains(q.id()))
            .toList();
    }

    /**
     * Get quests to offer at the Jobs board.
     */
    public static List<Quest> getOfferedQuests(ServerPlayerEntity player, int count) {
        int gamestage = getGamestage(player);
        List<String> activeIds = PlayerQuestState.get((ServerWorld) player.getWorld())
            .getActiveQuestIds(player.getUuid());
        return QuestSelector.selectMultiple(count, gamestage, activeIds);
    }

    /**
     * Accept a quest for a player.
     *
     * @return true if quest was accepted
     */
    public static boolean acceptQuest(ServerPlayerEntity player, String questId) {
        Quest quest = QuestConfigManager.getQuest(questId);
        if (quest == null) {
            player.sendMessage(Text.literal("Quest not found!").formatted(Formatting.RED), true);
            return false;
        }

        PlayerQuestState state = PlayerQuestState.get((ServerWorld) player.getWorld());

        if (!state.canAcceptQuest(player.getUuid())) {
            player.sendMessage(Text.literal("You already have 3 active quests!").formatted(Formatting.RED), true);
            return false;
        }

        int gamestage = getGamestage(player);
        if (!quest.isAvailableAt(gamestage)) {
            player.sendMessage(Text.literal("This quest is not available at your level.").formatted(Formatting.RED), true);
            return false;
        }

        QuestInstance instance = state.acceptQuest(
            player.getUuid(),
            quest,
            gamestage,
            ((ServerWorld) player.getWorld()).getTime()
        );

        if (instance != null) {
            player.sendMessage(Text.literal("Quest accepted: " + quest.getFormattedDescription(gamestage))
                .formatted(Formatting.GREEN), true);
            return true;
        }

        return false;
    }

    /**
     * Add progress to a kill quest.
     */
    public static void addKillProgress(ServerPlayerEntity player, Identifier entityType) {
        PlayerQuestState state = PlayerQuestState.get((ServerWorld) player.getWorld());

        for (QuestInstance instance : state.getActiveQuests(player.getUuid())) {
            Quest quest = QuestConfigManager.getQuest(instance.getQuestId());
            if (quest != null && quest.type() == QuestType.KILL && quest.target().equals(entityType)) {
                int oldProgress = instance.getProgress();
                state.addProgress(player.getUuid(), instance.getQuestId(), 1);

                if (instance.isComplete() && oldProgress < instance.getTargetCount()) {
                    player.sendMessage(Text.literal("Quest complete! Return to the Jobs board to claim your reward.")
                        .formatted(Formatting.GOLD), true);
                } else {
                    player.sendMessage(Text.literal(quest.getFormattedDescription(instance.getGamestageAtAccept()) +
                        " (" + instance.getProgress() + "/" + instance.getTargetCount() + ")")
                        .formatted(Formatting.YELLOW), true);
                }
            }
        }
    }

    /**
     * Check if a GATHER quest can be turned in (consumes items).
     */
    public static boolean canTurnInGatherQuest(ServerPlayerEntity player, String questId) {
        QuestInstance instance = PlayerQuestState.get((ServerWorld) player.getWorld())
            .getActiveQuest(player.getUuid(), questId);
        if (instance == null) return false;

        Quest quest = QuestConfigManager.getQuest(questId);
        if (quest == null || quest.type() != QuestType.GATHER) return false;

        // Count items in inventory
        Item targetItem = Registries.ITEM.get(quest.target());
        int count = countItems(player, targetItem);

        return count >= instance.getTargetCount();
    }

    /**
     * Check if a FETCH quest can be turned in (doesn't consume items).
     */
    public static boolean canTurnInFetchQuest(ServerPlayerEntity player, String questId) {
        QuestInstance instance = PlayerQuestState.get((ServerWorld) player.getWorld())
            .getActiveQuest(player.getUuid(), questId);
        if (instance == null) return false;

        Quest quest = QuestConfigManager.getQuest(questId);
        if (quest == null || quest.type() != QuestType.FETCH) return false;

        // Check if player has the item(s)
        Item targetItem = Registries.ITEM.get(quest.target());
        int count = countItems(player, targetItem);

        return count >= instance.getTargetCount();
    }

    /**
     * Check if a KILL quest is complete.
     */
    public static boolean isKillQuestComplete(ServerPlayerEntity player, String questId) {
        QuestInstance instance = PlayerQuestState.get((ServerWorld) player.getWorld())
            .getActiveQuest(player.getUuid(), questId);
        return instance != null && instance.isComplete();
    }

    /**
     * Check if any quest can be turned in based on its type.
     */
    public static boolean canTurnIn(ServerPlayerEntity player, String questId) {
        Quest quest = QuestConfigManager.getQuest(questId);
        if (quest == null) return false;

        return switch (quest.type()) {
            case KILL -> isKillQuestComplete(player, questId);
            case GATHER -> canTurnInGatherQuest(player, questId);
            case FETCH -> canTurnInFetchQuest(player, questId);
        };
    }

    /**
     * Turn in a completed quest and give reward.
     *
     * @param rewardType 0=emeralds, 1=experience, 2=item
     * @return true if turn-in was successful
     */
    public static boolean turnInQuest(ServerPlayerEntity player, String questId, int rewardType) {
        PlayerQuestState state = PlayerQuestState.get((ServerWorld) player.getWorld());
        QuestInstance instance = state.getActiveQuest(player.getUuid(), questId);
        if (instance == null) return false;

        Quest quest = QuestConfigManager.getQuest(questId);
        if (quest == null) return false;

        // Verify completion based on type
        boolean canComplete = switch (quest.type()) {
            case KILL -> instance.isComplete();
            case GATHER -> canTurnInGatherQuest(player, questId);
            case FETCH -> canTurnInFetchQuest(player, questId);
        };

        if (!canComplete) {
            player.sendMessage(Text.literal("Quest not yet complete!").formatted(Formatting.RED), true);
            return false;
        }

        // Consume items for GATHER quests
        if (quest.type() == QuestType.GATHER) {
            Item targetItem = Registries.ITEM.get(quest.target());
            removeItems(player, targetItem, instance.getTargetCount());
        }

        // Give reward
        int gamestage = instance.getGamestageAtAccept();
        QuestReward reward = quest.reward();

        switch (rewardType) {
            case 0 -> { // Emeralds
                int amount = reward.getScaledEmeralds(gamestage);
                giveItem(player, Registries.ITEM.get(Identifier.of("minecraft", "emerald")), amount);
                player.sendMessage(Text.literal("Received " + amount + " emeralds!").formatted(Formatting.GREEN), false);
            }
            case 1 -> { // Experience
                int xp = reward.getScaledExperience(gamestage);
                player.addExperience(xp);
                player.sendMessage(Text.literal("Received " + xp + " experience!").formatted(Formatting.GREEN), false);
            }
            case 2 -> { // Item
                if (reward.item() != null) {
                    Item rewardItem = Registries.ITEM.get(reward.item());
                    ItemStack stack = new ItemStack(rewardItem, reward.itemCount());
                    // TODO: Add enchantments if itemEnchanted based on gamestage
                    giveItemStack(player, stack);
                    player.sendMessage(Text.literal("Received " + reward.itemCount() + "x " +
                        rewardItem.getName().getString() + "!").formatted(Formatting.GREEN), false);
                }
            }
        }

        // Complete the quest
        state.completeQuest(player.getUuid(), questId);
        return true;
    }

    /**
     * Fail all quests on player death.
     */
    public static void onPlayerDeath(ServerPlayerEntity player) {
        PlayerQuestState state = PlayerQuestState.get((ServerWorld) player.getWorld());
        List<QuestInstance> quests = state.getActiveQuests(player.getUuid());

        if (!quests.isEmpty()) {
            state.failAllQuests(player.getUuid());
            player.sendMessage(Text.literal("All active quests failed due to death!")
                .formatted(Formatting.RED), false);
        }
    }

    /**
     * Get player's active quest instances.
     */
    public static List<QuestInstance> getActiveQuests(ServerPlayerEntity player) {
        return PlayerQuestState.get((ServerWorld) player.getWorld()).getActiveQuests(player.getUuid());
    }

    // === Helper methods ===

    private static int getGamestage(ServerPlayerEntity player) {
        if (STNCore.getGamestageProvider() != null) {
            return STNCore.getGamestageProvider().getWorldGamestage();
        }
        return 0;
    }

    private static int countItems(PlayerEntity player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void removeItems(PlayerEntity player, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().size() && remaining > 0; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == item) {
                int toRemove = Math.min(remaining, stack.getCount());
                stack.decrement(toRemove);
                remaining -= toRemove;
            }
        }
    }

    private static void giveItem(PlayerEntity player, Item item, int amount) {
        while (amount > 0) {
            int stackSize = Math.min(amount, item.getMaxCount());
            giveItemStack(player, new ItemStack(item, stackSize));
            amount -= stackSize;
        }
    }

    private static void giveItemStack(PlayerEntity player, ItemStack stack) {
        if (!player.getInventory().insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }
}
