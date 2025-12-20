package com.stn.mobai.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.stn.mobai.debug.SenseDebugger;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Debug commands for stn-mobai.
 *
 * Commands:
 * - /mobai debug - Show current debug status
 * - /mobai debug sounds - Toggle sound event chat messages
 * - /mobai debug awareness - Toggle mob awareness action bar
 * - /mobai debug all - Toggle all debug features
 * - /mobai env - Show environment info (light/heat sources)
 */
public class DebugCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            CommandManager.literal("mobai")
                .then(CommandManager.literal("debug")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(DebugCommand::showStatus)
                    .then(CommandManager.literal("sounds")
                        .executes(DebugCommand::toggleSounds)
                    )
                    .then(CommandManager.literal("awareness")
                        .executes(DebugCommand::toggleAwareness)
                    )
                    .then(CommandManager.literal("all")
                        .executes(DebugCommand::toggleAll)
                    )
                )
                .then(CommandManager.literal("env")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(DebugCommand::showEnvironment)
                )
        );
    }

    private static int showStatus(CommandContext<ServerCommandSource> context) {
        boolean sounds = SenseDebugger.isSoundsEnabled();
        boolean awareness = SenseDebugger.isAwarenessEnabled();

        context.getSource().sendFeedback(
            () -> Text.literal("[STN] Debug Status:").formatted(Formatting.GOLD),
            false
        );
        context.getSource().sendFeedback(
            () -> Text.literal("  sounds: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(sounds ? "ON" : "OFF")
                    .formatted(sounds ? Formatting.GREEN : Formatting.RED))
                .append(Text.literal(" - sound events in chat").formatted(Formatting.DARK_GRAY)),
            false
        );
        context.getSource().sendFeedback(
            () -> Text.literal("  awareness: ")
                .formatted(Formatting.GRAY)
                .append(Text.literal(awareness ? "ON" : "OFF")
                    .formatted(awareness ? Formatting.GREEN : Formatting.RED))
                .append(Text.literal(" - mob detection on action bar").formatted(Formatting.DARK_GRAY)),
            false
        );
        context.getSource().sendFeedback(
            () -> Text.literal("Usage: /mobai debug <sounds|awareness|all>").formatted(Formatting.DARK_GRAY),
            false
        );

        return 1;
    }

    private static int toggleSounds(CommandContext<ServerCommandSource> context) {
        SenseDebugger.toggleSounds();
        boolean enabled = SenseDebugger.isSoundsEnabled();

        context.getSource().sendFeedback(
            () -> Text.literal("[STN] Sound debug: ")
                .formatted(Formatting.GOLD)
                .append(Text.literal(enabled ? "ON" : "OFF")
                    .formatted(enabled ? Formatting.GREEN : Formatting.RED)),
            true
        );

        return 1;
    }

    private static int toggleAwareness(CommandContext<ServerCommandSource> context) {
        SenseDebugger.toggleAwareness();
        boolean enabled = SenseDebugger.isAwarenessEnabled();

        context.getSource().sendFeedback(
            () -> Text.literal("[STN] Awareness debug: ")
                .formatted(Formatting.GOLD)
                .append(Text.literal(enabled ? "ON" : "OFF")
                    .formatted(enabled ? Formatting.GREEN : Formatting.RED)),
            true
        );

        return 1;
    }

    private static int toggleAll(CommandContext<ServerCommandSource> context) {
        SenseDebugger.toggleAll();
        boolean anyEnabled = SenseDebugger.isAnyEnabled();

        context.getSource().sendFeedback(
            () -> Text.literal("[STN] All debug: ")
                .formatted(Formatting.GOLD)
                .append(Text.literal(anyEnabled ? "ON" : "OFF")
                    .formatted(anyEnabled ? Formatting.GREEN : Formatting.RED)),
            true
        );

        return 1;
    }

    private static int showEnvironment(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (source.getPlayer() != null) {
            SenseDebugger.sendEnvironmentInfo(source.getPlayer());
        }
        return 1;
    }
}
