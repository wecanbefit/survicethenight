package com.stn.survbar.client.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.stn.survbar.config.STNSurvbarConfig;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.text.Text;

/**
 * Client command to adjust survival bar HUD position.
 * Usage: /stnsb x <value> or /stnsb y <value>
 */
public class SurvbarCommand {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(
                ClientCommandManager.literal("stnsb")
                    .then(ClientCommandManager.literal("x")
                        .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(0, 200))
                            .executes(context -> {
                                int value = IntegerArgumentType.getInteger(context, "value");
                                STNSurvbarConfig.setXOffset(value);
                                context.getSource().sendFeedback(Text.literal("Survival bar X offset set to " + value));
                                return 1;
                            })
                        )
                    )
                    .then(ClientCommandManager.literal("y")
                        .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(0, 200))
                            .executes(context -> {
                                int value = IntegerArgumentType.getInteger(context, "value");
                                STNSurvbarConfig.setYOffset(value);
                                context.getSource().sendFeedback(Text.literal("Survival bar Y offset set to " + value));
                                return 1;
                            })
                        )
                    )
                    .executes(context -> {
                        context.getSource().sendFeedback(Text.literal("Survival bar position: x=" + STNSurvbarConfig.xOffset + ", y=" + STNSurvbarConfig.yOffset));
                        return 1;
                    })
            );
        });
    }
}
