package com.stn.survival.progression;

import com.stn.core.api.IGamestageProvider;
import com.stn.survival.STNSurvival;
import com.stn.survival.config.STNSurvivalConfig;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages the gamestage progression system.
 * Gamestage determines zombie spawn weights and difficulty scaling.
 */
public class GamestageManager implements IGamestageProvider {
    private final MinecraftServer server;
    private final Map<UUID, PlayerGamestage> playerGamestages = new HashMap<>();

    private int worldGamestage = 0;
    private long lastDayCheck = 0;
    private int survivalNightsSurvived = 0;

    public GamestageManager(MinecraftServer server) {
        this.server = server;

        loadData();

        ServerLivingEntityEvents.AFTER_DEATH.register(this::onEntityDeath);

        STNSurvival.LOGGER.info("Gamestage Manager initialized");
    }

    private void onEntityDeath(LivingEntity entity, net.minecraft.entity.damage.DamageSource source) {
        if (entity.getWorld().isClient()) {
            return;
        }

        if (entity instanceof ZombieEntity && source.getAttacker() instanceof ServerPlayerEntity player) {
            PlayerGamestage playerData = getOrCreatePlayerData(player);
            playerData.addZombieKill();
            recalculateWorldGamestage();
        }
    }

    public void tick() {
        ServerWorld overworld = server.getOverworld();
        if (overworld == null) return;

        long currentDay = overworld.getTimeOfDay() / 24000;
        if (currentDay > lastDayCheck) {
            lastDayCheck = currentDay;
            onNewDay(currentDay);
        }
    }

    private void onNewDay(long day) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerGamestage playerData = getOrCreatePlayerData(player);
            playerData.incrementDaysSurvived();
        }

        recalculateWorldGamestage();
        checkGamestageMilestones();
    }

    public void onSurvivalNightSurvived() {
        survivalNightsSurvived++;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerGamestage playerData = getOrCreatePlayerData(player);
            playerData.addSurvivalNightSurvived();
        }

        recalculateWorldGamestage();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(
                Text.literal("Survival Night survived! Gamestage increased.")
                    .formatted(Formatting.RED),
                false
            );
        }
    }

    public void onPlayerDeath(ServerPlayerEntity player) {
        PlayerGamestage playerData = getOrCreatePlayerData(player);
        playerData.onDeath();
        recalculateWorldGamestage();
    }

    private void recalculateWorldGamestage() {
        if (server.getPlayerManager().getPlayerList().isEmpty()) {
            return;
        }

        int totalGamestage = 0;
        int playerCount = 0;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerGamestage playerData = getOrCreatePlayerData(player);
            totalGamestage += playerData.calculateGamestage();
            playerCount++;
        }

        if (playerCount > 0) {
            worldGamestage = totalGamestage / playerCount;
        }
    }

    private void checkGamestageMilestones() {
        int[] milestones = {11, 26, 51, 76, 100};

        for (int milestone : milestones) {
            if (worldGamestage >= milestone && worldGamestage < milestone + 5) {
                String message = getGamestageMessage(milestone);
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    player.sendMessage(
                        Text.literal(message).formatted(Formatting.DARK_RED),
                        false
                    );
                }
                break;
            }
        }
    }

    private String getGamestageMessage(int milestone) {
        return switch (milestone) {
            case 11 -> "Gamestage 11+: Feral zombies now appear in hordes!";
            case 26 -> "Gamestage 26+: Sprinter zombies are common. Run.";
            case 51 -> "Gamestage 51+: Demolishers have arrived. Fortify your base!";
            case 76 -> "Gamestage 76+: Screamers roam the night. Silence is survival.";
            case 100 -> "Gamestage 100+: Giants walk the earth. Nowhere is safe.";
            default -> "";
        };
    }

    public PlayerGamestage getOrCreatePlayerData(ServerPlayerEntity player) {
        return playerGamestages.computeIfAbsent(
            player.getUuid(),
            uuid -> new PlayerGamestage(uuid, player.getName().getString())
        );
    }

    @Override
    public int getWorldGamestage() {
        return worldGamestage;
    }

    public void setWorldGamestage(int value) {
        this.worldGamestage = Math.max(0, Math.min(200, value));
        STNSurvival.LOGGER.info("Gamestage manually set to {}", worldGamestage);
    }

    @Override
    public int getPlayerGamestage(ServerPlayerEntity player) {
        return getOrCreatePlayerData(player).calculateGamestage();
    }

    @Override
    public int getPlayerGamestage(UUID playerId) {
        PlayerGamestage data = playerGamestages.get(playerId);
        return data != null ? data.calculateGamestage() : 0;
    }

    @Override
    public void addGamestage(ServerPlayerEntity player, int amount) {
        PlayerGamestage data = getOrCreatePlayerData(player);
        // Add zombie kills to simulate gamestage gain
        for (int i = 0; i < amount * STNSurvivalConfig.GAMESTAGE_ZOMBIE_KILLS_DIVISOR; i++) {
            data.addZombieKill();
        }
        recalculateWorldGamestage();
    }

    @Override
    public void removeGamestage(ServerPlayerEntity player, int amount) {
        PlayerGamestage data = getOrCreatePlayerData(player);
        // Increment death count to reduce gamestage
        for (int i = 0; i < amount / STNSurvivalConfig.GAMESTAGE_DEATH_PENALTY; i++) {
            data.onDeath();
        }
        recalculateWorldGamestage();
    }

    public boolean canSpawnZombieType(String zombieType) {
        return switch (zombieType) {
            case "feral" -> worldGamestage >= STNSurvivalConfig.GAMESTAGE_FERAL_THRESHOLD;
            case "sprinter" -> worldGamestage >= STNSurvivalConfig.GAMESTAGE_SPRINTER_THRESHOLD;
            case "demolisher" -> worldGamestage >= STNSurvivalConfig.GAMESTAGE_DEMOLISHER_THRESHOLD;
            case "screamer" -> worldGamestage >= STNSurvivalConfig.GAMESTAGE_SCREAMER_THRESHOLD;
            case "spider_jockey" -> worldGamestage >= STNSurvivalConfig.GAMESTAGE_SPIDER_JOCKEY_THRESHOLD;
            default -> true;
        };
    }

    @Override
    public float getHordeSizeMultiplier() {
        if (worldGamestage <= 10) return 1.0f;
        if (worldGamestage <= 25) return 1.25f;
        if (worldGamestage <= 50) return 1.5f;
        if (worldGamestage <= 75) return 1.75f;
        if (worldGamestage <= 100) return 2.0f;
        return 2.5f;
    }

    public float getBlockBreakSpeedMultiplier() {
        if (worldGamestage < 50) return 1.0f;
        if (worldGamestage < 75) return 1.25f;
        if (worldGamestage < 100) return 1.5f;
        return 2.0f;
    }

    private void loadData() {
        STNSurvival.LOGGER.info("Loading gamestage data...");
    }

    public void saveData() {
        STNSurvival.LOGGER.info("Saving gamestage data...");
    }
}
