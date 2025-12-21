package com.stn.traders.client;

import com.stn.traders.client.screen.QuestScreen;
import com.stn.traders.network.QuestSyncPayload;
import com.stn.traders.registry.STNTraderEntities;
import com.stn.traders.registry.STNTraderScreens;
import com.stn.traders.screen.QuestScreenHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.entity.WanderingTraderEntityRenderer;

public class STNTradersClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register entity renderers - use wandering trader renderer
        EntityRendererRegistry.register(STNTraderEntities.SURVIVAL_TRADER,
            ctx -> new WanderingTraderEntityRenderer(ctx));

        // Register screen handlers
        HandledScreens.register(STNTraderScreens.QUEST_SCREEN_HANDLER, QuestScreen::new);

        // Set up client data suppliers for the screen handler
        QuestScreenHandler.setClientDataSuppliers(
            ClientQuestData::getOfferedQuests,
            ClientQuestData::getActiveQuests
        );

        // Register client network handler for quest sync
        ClientPlayNetworking.registerGlobalReceiver(QuestSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientQuestData.update(payload);
            });
        });
    }
}
