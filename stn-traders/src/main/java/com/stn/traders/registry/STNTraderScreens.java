package com.stn.traders.registry;

import com.stn.traders.STNTraders;
import com.stn.traders.screen.QuestScreenHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

/**
 * Registry for screen handlers.
 */
public class STNTraderScreens {

    public static final ScreenHandlerType<QuestScreenHandler> QUEST_SCREEN_HANDLER =
        Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier.of(STNTraders.MOD_ID, "quest_screen"),
            new ScreenHandlerType<>(QuestScreenHandler::new, FeatureFlags.VANILLA_FEATURES)
        );

    /**
     * Initialize screen handlers.
     */
    public static void register() {
        STNTraders.LOGGER.info("Registering STN trader screen handlers...");
        // Static fields initialize on class load
        STNTraders.LOGGER.info("Registered screen handlers");
    }
}
