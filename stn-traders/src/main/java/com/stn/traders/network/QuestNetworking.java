package com.stn.traders.network;

import com.stn.traders.quest.Quest;
import com.stn.traders.quest.QuestConfigManager;
import com.stn.traders.quest.QuestInstance;
import com.stn.traders.quest.QuestManager;
import com.stn.traders.quest.QuestType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for quest-related networking.
 */
public class QuestNetworking {

    /**
     * Send quest sync data to a player.
     */
    public static void sendQuestSync(ServerPlayerEntity player) {
        // Get offered quests (IDs only)
        List<Quest> offered = QuestManager.getOfferedQuests(player, 3);
        List<String> offeredIds = new ArrayList<>();
        for (Quest quest : offered) {
            offeredIds.add(quest.id());
        }

        // Get active quests with progress
        List<QuestInstance> active = QuestManager.getActiveQuests(player);
        List<QuestSyncPayload.ActiveQuestEntry> activeEntries = new ArrayList<>();
        for (QuestInstance instance : active) {
            boolean canTurnIn = QuestManager.canTurnIn(player, instance.getQuestId());

            // For GATHER/FETCH quests, calculate progress from inventory
            int progress = instance.getProgress();
            Quest quest = QuestConfigManager.getQuest(instance.getQuestId());
            if (quest != null && (quest.type() == QuestType.GATHER || quest.type() == QuestType.FETCH)) {
                Item targetItem = Registries.ITEM.get(quest.target());
                progress = countItems(player, targetItem);
            }

            activeEntries.add(new QuestSyncPayload.ActiveQuestEntry(
                instance.getQuestId(),
                progress,
                instance.getTargetCount(),
                instance.getGamestageAtAccept(),
                canTurnIn
            ));
        }

        // Send the payload
        ServerPlayNetworking.send(player, new QuestSyncPayload(offeredIds, activeEntries));
    }

    /**
     * Count items of a specific type in a player's inventory.
     */
    private static int countItems(ServerPlayerEntity player, Item item) {
        int count = 0;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == item) {
                count += stack.getCount();
            }
        }
        return count;
    }
}
