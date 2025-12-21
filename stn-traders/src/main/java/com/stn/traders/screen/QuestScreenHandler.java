package com.stn.traders.screen;

import com.stn.traders.quest.*;
import com.stn.traders.registry.STNTraderScreens;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Screen handler for the quest UI.
 */
public class QuestScreenHandler extends ScreenHandler {
    private final PlayerEntity player;
    private final boolean isClient;
    private List<Quest> offeredQuests = new ArrayList<>();
    private List<QuestInstance> activeQuests = new ArrayList<>();

    // Client data supplier - set by client mod
    private static Supplier<List<Quest>> clientOfferedQuestsSupplier;
    private static Supplier<List<QuestInstance>> clientActiveQuestsSupplier;

    public static void setClientDataSuppliers(
            Supplier<List<Quest>> offeredSupplier,
            Supplier<List<QuestInstance>> activeSupplier) {
        clientOfferedQuestsSupplier = offeredSupplier;
        clientActiveQuestsSupplier = activeSupplier;
    }

    public QuestScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(STNTraderScreens.QUEST_SCREEN_HANDLER, syncId);
        this.player = playerInventory.player;
        this.isClient = !(player instanceof ServerPlayerEntity);

        // On server, load quests
        if (player instanceof ServerPlayerEntity serverPlayer) {
            refreshQuests(serverPlayer);
        }
        // On client, data comes from ClientQuestData via the suppliers
    }

    /**
     * Refresh the list of offered and active quests.
     */
    public void refreshQuests(ServerPlayerEntity player) {
        this.offeredQuests = QuestManager.getOfferedQuests(player, 3);
        this.activeQuests = QuestManager.getActiveQuests(player);
    }

    /**
     * Get offered quests (for display).
     */
    public List<Quest> getOfferedQuests() {
        if (isClient && clientOfferedQuestsSupplier != null) {
            return clientOfferedQuestsSupplier.get();
        }
        return offeredQuests;
    }

    /**
     * Get active quests (for display).
     */
    public List<QuestInstance> getActiveQuests() {
        if (isClient && clientActiveQuestsSupplier != null) {
            return clientActiveQuestsSupplier.get();
        }
        return activeQuests;
    }

    /**
     * Accept a quest by index in the offered list.
     */
    public boolean acceptQuest(int index) {
        if (index < 0 || index >= offeredQuests.size()) return false;
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return false;

        Quest quest = offeredQuests.get(index);
        boolean success = QuestManager.acceptQuest(serverPlayer, quest.id());

        if (success) {
            refreshQuests(serverPlayer);
        }

        return success;
    }

    /**
     * Turn in a completed quest.
     *
     * @param questId the quest ID
     * @param rewardType 0=emeralds, 1=experience, 2=item
     */
    public boolean turnInQuest(String questId, int rewardType) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return false;

        boolean success = QuestManager.turnInQuest(serverPlayer, questId, rewardType);

        if (success) {
            refreshQuests(serverPlayer);
        }

        return success;
    }

    /**
     * Check if a quest is ready to turn in.
     */
    public boolean isQuestComplete(String questId) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return false;

        Quest quest = QuestConfigManager.getQuest(questId);
        if (quest == null) return false;

        return switch (quest.type()) {
            case KILL -> QuestManager.isKillQuestComplete(serverPlayer, questId);
            case GATHER -> QuestManager.canTurnInGatherQuest(serverPlayer, questId);
            case FETCH -> QuestManager.canTurnInFetchQuest(serverPlayer, questId);
        };
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
