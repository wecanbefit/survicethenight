package com.stn.survival.command;

import com.stn.survival.STNSurvival;
import com.stn.survival.config.STNSurvivalConfig;
import com.stn.survival.event.SurvivalNightManager;
import com.stn.survival.network.SurvivalNightSyncPayload;
import com.stn.survival.progression.GamestageManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.registry.Registries;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Commands for controlling Survival Night events and gamestage.
 */
public class SurvivalNightCommand {

    // Suggest hostile mob entity IDs for spawn control
    private static final SuggestionProvider<ServerCommandSource> ENTITY_ID_SUGGESTIONS = (context, builder) -> {
        String[] commonHostiles = {
            "minecraft:zombie", "minecraft:skeleton", "minecraft:creeper", "minecraft:spider",
            "minecraft:enderman", "minecraft:witch", "minecraft:slime", "minecraft:phantom",
            "minecraft:drowned", "minecraft:husk", "minecraft:stray", "minecraft:cave_spider",
            "minecraft:silverfish", "minecraft:blaze", "minecraft:ghast", "minecraft:magma_cube",
            "minecraft:wither_skeleton", "minecraft:pillager", "minecraft:vindicator", "minecraft:ravager"
        };
        for (String id : commonHostiles) {
            if (id.startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(id);
            }
        }
        return builder.buildFuture();
    };

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("survivalnight")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.literal("start")
                .executes(SurvivalNightCommand::startSurvivalNight))
            .then(CommandManager.literal("stop")
                .executes(SurvivalNightCommand::stopSurvivalNight))
            .then(CommandManager.literal("status")
                .executes(SurvivalNightCommand::showStatus))
            .then(CommandManager.literal("gamestage")
                .executes(SurvivalNightCommand::showGamestage)
                .then(CommandManager.literal("set")
                    .then(CommandManager.argument("value", IntegerArgumentType.integer(0, 200))
                        .executes(SurvivalNightCommand::setGamestage))))
            .then(CommandManager.literal("mob")
                .then(CommandManager.literal("disable")
                    .then(CommandManager.argument("entity_id", StringArgumentType.string())
                        .suggests(ENTITY_ID_SUGGESTIONS)
                        .executes(SurvivalNightCommand::disableMobSpawn)))
                .then(CommandManager.literal("enable")
                    .then(CommandManager.argument("entity_id", StringArgumentType.string())
                        .suggests(ENTITY_ID_SUGGESTIONS)
                        .executes(SurvivalNightCommand::enableMobSpawn)))
                .then(CommandManager.literal("list")
                    .executes(SurvivalNightCommand::listDisabledMobs)))
            .executes(SurvivalNightCommand::showHelp)
        );

        // Also register shorter alias
        dispatcher.register(CommandManager.literal("stn")
            .requires(source -> source.hasPermissionLevel(2))
            .redirect(dispatcher.getRoot().getChild("survivalnight"))
        );
    }

    private static int showHelp(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        source.sendFeedback(() -> Text.literal("=== Survival Night Commands ===").formatted(Formatting.DARK_RED, Formatting.BOLD), false);
        source.sendFeedback(() -> Text.literal("/survivalnight start").formatted(Formatting.RED).append(Text.literal(" - Force start a Survival Night").formatted(Formatting.GRAY)), false);
        source.sendFeedback(() -> Text.literal("/survivalnight stop").formatted(Formatting.RED).append(Text.literal(" - Force end current Survival Night").formatted(Formatting.GRAY)), false);
        source.sendFeedback(() -> Text.literal("/survivalnight status").formatted(Formatting.RED).append(Text.literal(" - Show horde status").formatted(Formatting.GRAY)), false);
        source.sendFeedback(() -> Text.literal("/survivalnight gamestage").formatted(Formatting.RED).append(Text.literal(" - Show current gamestage").formatted(Formatting.GRAY)), false);
        source.sendFeedback(() -> Text.literal("/survivalnight gamestage set <value>").formatted(Formatting.RED).append(Text.literal(" - Set gamestage (0-200)").formatted(Formatting.GRAY)), false);
        source.sendFeedback(() -> Text.literal("/survivalnight mob disable <id>").formatted(Formatting.RED).append(Text.literal(" - Disable mob spawning").formatted(Formatting.GRAY)), false);
        source.sendFeedback(() -> Text.literal("/survivalnight mob enable <id>").formatted(Formatting.RED).append(Text.literal(" - Enable mob spawning").formatted(Formatting.GRAY)), false);
        source.sendFeedback(() -> Text.literal("/survivalnight mob list").formatted(Formatting.RED).append(Text.literal(" - List disabled mobs").formatted(Formatting.GRAY)), false);
        source.sendFeedback(() -> Text.literal("Alias: /stn").formatted(Formatting.GRAY), false);
        return 1;
    }

    private static int startSurvivalNight(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        SurvivalNightManager manager = STNSurvival.getSurvivalNightManager();

        if (manager == null) {
            source.sendError(Text.literal("Survival Night manager not initialized!"));
            return 0;
        }

        if (manager.isSurvivalNightActive()) {
            source.sendError(Text.literal("A Survival Night is already active!"));
            return 0;
        }

        manager.forceStart();
        source.sendFeedback(() -> Text.literal("Survival Night started!").formatted(Formatting.DARK_RED, Formatting.BOLD), true);
        return 1;
    }

    private static int stopSurvivalNight(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        SurvivalNightManager manager = STNSurvival.getSurvivalNightManager();

        if (manager == null) {
            source.sendError(Text.literal("Survival Night manager not initialized!"));
            return 0;
        }

        if (manager.isSurvivalNightActive()) {
            manager.forceStop();
            source.sendFeedback(() -> Text.literal("Survival Night ended!").formatted(Formatting.GOLD), true);
        } else {
            // Even if server thinks survival night is inactive, resync all clients
            ServerWorld world = source.getWorld();
            for (ServerPlayerEntity player : world.getPlayers()) {
                ServerPlayNetworking.send(player, new SurvivalNightSyncPayload(false));
            }
            source.sendFeedback(() -> Text.literal("Survival Night state resynced to all players.").formatted(Formatting.GOLD), true);
        }
        return 1;
    }

    private static int showStatus(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        SurvivalNightManager manager = STNSurvival.getSurvivalNightManager();
        GamestageManager gamestage = STNSurvival.getGamestageManager();

        if (manager == null) {
            source.sendError(Text.literal("Survival Night manager not initialized!"));
            return 0;
        }

        source.sendFeedback(() -> Text.literal("=== Survival Night Status ===").formatted(Formatting.DARK_RED, Formatting.BOLD), false);

        boolean active = manager.isSurvivalNightActive();
        source.sendFeedback(() -> Text.literal("Active: ").formatted(Formatting.GRAY)
            .append(Text.literal(active ? "YES" : "NO").formatted(active ? Formatting.RED : Formatting.GREEN)), false);

        if (active) {
            int spawned = manager.getMobsSpawned();
            int target = manager.getTargetHordeSize();
            int activeCount = manager.getActiveHordeCount();

            source.sendFeedback(() -> Text.literal("Zombies Spawned: ").formatted(Formatting.GRAY)
                .append(Text.literal(spawned + "/" + target).formatted(Formatting.RED)), false);
            source.sendFeedback(() -> Text.literal("Active Horde: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.valueOf(activeCount)).formatted(Formatting.RED)), false);
        } else {
            int daysUntil = manager.getDaysUntilSurvivalNight();
            source.sendFeedback(() -> Text.literal("Days Until Next: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.valueOf(daysUntil)).formatted(Formatting.GOLD)), false);
        }

        if (gamestage != null) {
            int worldStage = gamestage.getWorldGamestage();
            source.sendFeedback(() -> Text.literal("World Gamestage: ").formatted(Formatting.GRAY)
                .append(Text.literal(String.valueOf(worldStage)).formatted(Formatting.GOLD)), false);
        }

        return 1;
    }

    private static int showGamestage(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        GamestageManager gamestage = STNSurvival.getGamestageManager();

        if (gamestage == null) {
            source.sendError(Text.literal("Gamestage manager not initialized!"));
            return 0;
        }

        int worldStage = gamestage.getWorldGamestage();
        float hordeMultiplier = gamestage.getHordeSizeMultiplier();

        source.sendFeedback(() -> Text.literal("=== Gamestage Info ===").formatted(Formatting.GOLD, Formatting.BOLD), false);
        source.sendFeedback(() -> Text.literal("World Gamestage: ").formatted(Formatting.GRAY)
            .append(Text.literal(String.valueOf(worldStage)).formatted(Formatting.GOLD)), false);
        source.sendFeedback(() -> Text.literal("Horde Multiplier: ").formatted(Formatting.GRAY)
            .append(Text.literal(String.format("%.1fx", hordeMultiplier)).formatted(Formatting.RED)), false);

        // Show what's unlocked
        source.sendFeedback(() -> Text.literal("Unlocked Zombies:").formatted(Formatting.GRAY), false);

        StringBuilder unlocked = new StringBuilder();
        unlocked.append("Normal, Bloated");
        if (gamestage.canSpawnZombieType("feral")) unlocked.append(", Feral");
        if (gamestage.canSpawnZombieType("sprinter")) unlocked.append(", Sprinter");
        if (gamestage.canSpawnZombieType("demolisher")) unlocked.append(", Demolisher");
        if (gamestage.canSpawnZombieType("screamer")) unlocked.append(", Screamer");
        if (gamestage.canSpawnZombieType("spider_jockey")) unlocked.append(", Spider Jockey");

        String unlockedStr = unlocked.toString();
        source.sendFeedback(() -> Text.literal("  " + unlockedStr).formatted(Formatting.RED), false);

        return 1;
    }

    private static int setGamestage(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        GamestageManager gamestage = STNSurvival.getGamestageManager();

        if (gamestage == null) {
            source.sendError(Text.literal("Gamestage manager not initialized!"));
            return 0;
        }

        int value = IntegerArgumentType.getInteger(context, "value");
        gamestage.setWorldGamestage(value);

        source.sendFeedback(() -> Text.literal("Gamestage set to " + value).formatted(Formatting.GOLD), true);
        return 1;
    }

    private static int disableMobSpawn(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String entityId = StringArgumentType.getString(context, "entity_id");

        // Validate entity ID exists
        if (!Registries.ENTITY_TYPE.containsId(net.minecraft.util.Identifier.of(entityId))) {
            source.sendError(Text.literal("Unknown entity type: " + entityId));
            source.sendError(Text.literal("Use format: minecraft:zombie, minecraft:slime, etc."));
            return 0;
        }

        if (STNSurvivalConfig.DISABLED_MOB_SPAWNS.contains(entityId)) {
            source.sendFeedback(() -> Text.literal(entityId + " is already disabled").formatted(Formatting.YELLOW), false);
            return 1;
        }

        STNSurvivalConfig.disableMobSpawn(entityId);
        source.sendFeedback(() -> Text.literal("Disabled spawning for: ").formatted(Formatting.GRAY)
            .append(Text.literal(entityId).formatted(Formatting.RED)), true);
        source.sendFeedback(() -> Text.literal("Note: Existing mobs are not affected").formatted(Formatting.GRAY), false);
        return 1;
    }

    private static int enableMobSpawn(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String entityId = StringArgumentType.getString(context, "entity_id");

        if (!STNSurvivalConfig.DISABLED_MOB_SPAWNS.contains(entityId)) {
            source.sendFeedback(() -> Text.literal(entityId + " is not disabled").formatted(Formatting.YELLOW), false);
            return 1;
        }

        STNSurvivalConfig.enableMobSpawn(entityId);
        source.sendFeedback(() -> Text.literal("Enabled spawning for: ").formatted(Formatting.GRAY)
            .append(Text.literal(entityId).formatted(Formatting.GREEN)), true);
        return 1;
    }

    private static int listDisabledMobs(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        source.sendFeedback(() -> Text.literal("=== Disabled Mob Spawns ===").formatted(Formatting.DARK_RED, Formatting.BOLD), false);

        if (STNSurvivalConfig.DISABLED_MOB_SPAWNS.isEmpty()) {
            source.sendFeedback(() -> Text.literal("No mobs are currently disabled").formatted(Formatting.GRAY), false);
        } else {
            for (String entityId : STNSurvivalConfig.DISABLED_MOB_SPAWNS) {
                source.sendFeedback(() -> Text.literal("  - " + entityId).formatted(Formatting.RED), false);
            }
            source.sendFeedback(() -> Text.literal("Total: " + STNSurvivalConfig.DISABLED_MOB_SPAWNS.size() + " disabled").formatted(Formatting.GRAY), false);
        }

        return 1;
    }
}
