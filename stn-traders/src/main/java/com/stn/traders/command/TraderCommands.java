package com.stn.traders.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.stn.traders.entity.SurvivalTraderEntity;
import com.stn.traders.registry.STNTraderEntities;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.List;

/**
 * Commands for the STN Traders mod.
 */
public class TraderCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(TraderCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
                                         CommandRegistryAccess registryAccess,
                                         CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
            CommandManager.literal("stntrader")
                .then(CommandManager.literal("locate")
                    .executes(TraderCommands::locateTrader))
                .then(CommandManager.literal("spawn")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(TraderCommands::spawnTrader))
        );
    }

    private static int spawnTrader(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendError(Text.literal("This command must be run by a player"));
            return 0;
        }

        ServerWorld world = (ServerWorld) player.getWorld();

        // Create and spawn the trader at player's position
        SurvivalTraderEntity trader = new SurvivalTraderEntity(STNTraderEntities.SURVIVAL_TRADER, world);
        trader.setPosition(player.getX(), player.getY(), player.getZ());
        world.spawnEntity(trader);

        source.sendFeedback(() -> Text.literal("Spawned SurvivalTrader at your position")
            .formatted(Formatting.GREEN), true);

        return 1;
    }

    private static int locateTrader(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            source.sendError(Text.literal("This command must be run by a player"));
            return 0;
        }

        ServerWorld world = (ServerWorld) player.getWorld();
        BlockPos playerPos = player.getBlockPos();

        // Search in expanding radius
        int searchRadius = 1000;
        Box searchBox = new Box(
            playerPos.getX() - searchRadius, world.getBottomY(), playerPos.getZ() - searchRadius,
            playerPos.getX() + searchRadius, world.getTopYInclusive(), playerPos.getZ() + searchRadius
        );

        List<SurvivalTraderEntity> traders = world.getEntitiesByType(
            STNTraderEntities.SURVIVAL_TRADER,
            searchBox,
            entity -> true
        );

        if (traders.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No traders found within " + searchRadius + " blocks")
                .formatted(Formatting.YELLOW), false);
            return 0;
        }

        // Find nearest
        SurvivalTraderEntity nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (SurvivalTraderEntity trader : traders) {
            double dist = trader.squaredDistanceTo(player);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = trader;
            }
        }

        if (nearest != null) {
            int x = nearest.getBlockX();
            int y = nearest.getBlockY();
            int z = nearest.getBlockZ();
            int distance = (int) Math.sqrt(nearestDist);

            source.sendFeedback(() -> Text.literal("Nearest trader: ")
                .formatted(Formatting.GREEN)
                .append(Text.literal(x + " " + y + " " + z)
                    .formatted(Formatting.AQUA))
                .append(Text.literal(" (" + distance + " blocks)")
                    .formatted(Formatting.GRAY)), false);
        }

        return 1;
    }
}
