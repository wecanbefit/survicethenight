package com.stn.traders;

import com.stn.traders.command.TraderCommands;
import com.stn.traders.loot.TrophyLootModifier;
import com.stn.traders.network.QuestActionPayload;
import com.stn.traders.network.QuestNetworking;
import com.stn.traders.network.QuestSyncPayload;
import com.stn.traders.protection.BlockProtectionEvents;
import com.stn.traders.protection.TraderProtectionManager;
import com.stn.traders.quest.QuestEventHandlers;
import com.stn.traders.quest.QuestManager;
import com.stn.traders.registry.STNTraderBlocks;
import com.stn.traders.registry.STNTraderEntities;
import com.stn.traders.registry.STNTraderItems;
import com.stn.traders.registry.STNTraderScreens;
import com.stn.traders.structure.processor.STNProcessors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Survive The Night - Traders
 * Trading structures, merchant entities, and quest system.
 */
public class STNTraders implements ModInitializer {
    public static final String MOD_ID = "stn_traders";
    public static final Logger LOGGER = LoggerFactory.getLogger("STN-Traders");

    @Override
    public void onInitialize() {
        LOGGER.info("Survive The Night - Traders initializing...");

        // Register network payloads
        PayloadTypeRegistry.playS2C().register(QuestSyncPayload.ID, QuestSyncPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(QuestActionPayload.ID, QuestActionPayload.CODEC);

        // Handle quest action packets from clients
        ServerPlayNetworking.registerGlobalReceiver(QuestActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var player = context.player();
                switch (payload.action()) {
                    case ACCEPT -> {
                        boolean success = QuestManager.acceptQuest(player, payload.questId());
                        if (success) {
                            // Send updated quest data back to client
                            QuestNetworking.sendQuestSync(player);
                        }
                    }
                    case TURN_IN -> {
                        boolean success = QuestManager.turnInQuest(player, payload.questId(), payload.rewardType());
                        if (success) {
                            // Send updated quest data back to client
                            QuestNetworking.sendQuestSync(player);
                        }
                    }
                }
            });
        });

        // Initialize block protection system
        TraderProtectionManager.init();
        BlockProtectionEvents.register();

        // Register trader entities
        STNTraderEntities.register();

        // Register trophy items and loot modifications
        STNTraderItems.register();
        TrophyLootModifier.register();

        // Register blocks and screen handlers
        STNTraderBlocks.register();
        STNTraderScreens.register();

        // Register structure processors (biome-aware wood replacement)
        STNProcessors.register();
        // Structure generation is handled via datapack (worldgen JSON files)

        // Register quest system
        QuestManager.init();
        QuestEventHandlers.register();

        // Register commands
        TraderCommands.register();

        LOGGER.info("Survive The Night - Traders initialized!");
    }
}
