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

import java.util.UUID;

/**
 * Manages the gamestage progression system.
 * Gamestage determines zombie spawn weights and difficulty scaling.
 */
public class GamestageManager implements IGamestageProvider {
    private final MinecraftServer server;
    private GamestageState state;

    public GamestageManager(MinecraftServer server) {
        this.server = server;

        // Load persistent state from world data
        this.state = GamestageState.get(server);

        ServerLivingEntityEvents.AFTER_DEATH.register(this::onEntityDeath);

        STNSurvival.LOGGER.info("Gamestage Manager initialized - World gamestage: {}", state.getWorldGamestage());
    }

    private void onEntityDeath(LivingEntity entity, net.minecraft.entity.damage.DamageSource source) {
        if (entity.getWorld().isClient()) {
            return;
        }

        if (entity instanceof ZombieEntity && source.getAttacker() instanceof ServerPlayerEntity player) {
            PlayerGamestage playerData = getOrCreatePlayerData(player);
            playerData.addZombieKill();
            recalculateWorldGamestage();
            state.markDirty();
        }
    }

    public void tick() {
        ServerWorld overworld = server.getOverworld();
        if (overworld == null) return;

        long currentDay = overworld.getTimeOfDay() / 24000;
        if (currentDay > state.getLastDayCheck()) {
            state.setLastDayCheck(currentDay);
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
        state.markDirty();
    }

    public void onSurvivalNightSurvived() {
        state.setSurvivalNightsSurvived(state.getSurvivalNightsSurvived() + 1);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            PlayerGamestage playerData = getOrCreatePlayerData(player);
            playerData.addSurvivalNightSurvived();
        }

        recalculateWorldGamestage();
        state.markDirty();

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
        state.markDirty();
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
            state.setWorldGamestage(totalGamestage / playerCount);
        }
    }

    private void checkGamestageMilestones() {
        int[] milestones = {11, 26, 51, 76, 100};
        int worldGamestage = state.getWorldGamestage();

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
        return state.getOrCreatePlayerData(player.getUuid(), player.getName().getString());
    }

    @Override
    public int getWorldGamestage() {
        return state.getWorldGamestage();
    }

    public void setWorldGamestage(int value) {
        state.setWorldGamestage(Math.max(0, Math.min(200, value)));
        STNSurvival.LOGGER.info("Gamestage manually set to {}", state.getWorldGamestage());
    }

    @Override
    public int getPlayerGamestage(ServerPlayerEntity player) {
        return getOrCreatePlayerData(player).calculateGamestage();
    }

    @Override
    public int getPlayerGamestage(UUID playerId) {
        PlayerGamestage data = state.getPlayerData().get(playerId);
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
        state.markDirty();
    }

    @Override
    public void removeGamestage(ServerPlayerEntity player, int amount) {
        PlayerGamestage data = getOrCreatePlayerData(player);
        // Increment death count to reduce gamestage
        for (int i = 0; i < amount / STNSurvivalConfig.GAMESTAGE_DEATH_PENALTY; i++) {
            data.onDeath();
        }
        recalculateWorldGamestage();
        state.markDirty();
    }

    public boolean canSpawnZombieType(String zombieType) {
        int worldGamestage = state.getWorldGamestage();
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
        int worldGamestage = state.getWorldGamestage();
        if (worldGamestage <= 10) return 1.0f;
        if (worldGamestage <= 25) return 1.25f;
        if (worldGamestage <= 50) return 1.5f;
        if (worldGamestage <= 75) return 1.75f;
        if (worldGamestage <= 100) return 2.0f;
        return 2.5f;
    }

    public float getBlockBreakSpeedMultiplier() {
        int worldGamestage = state.getWorldGamestage();
        if (worldGamestage < 50) return 1.0f;
        if (worldGamestage < 75) return 1.25f;
        if (worldGamestage < 100) return 1.5f;
        return 2.0f;
    }

    public void saveData() {
        // Data is saved automatically by PersistentState when marked dirty
        state.markDirty();
        STNSurvival.LOGGER.info("Gamestage data marked for save");
    }
}
