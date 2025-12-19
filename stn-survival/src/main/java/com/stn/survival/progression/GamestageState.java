package com.stn.survival.progression;

import com.stn.survival.STNSurvival;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent state for gamestage data.
 * Saves to world/data/stn_gamestage.dat
 */
public class GamestageState extends PersistentState {
    private static final String STATE_ID = "stn_gamestage";

    private int worldGamestage = 0;
    private int survivalNightsSurvived = 0;
    private long lastDayCheck = 0;
    private final Map<UUID, PlayerGamestage> playerData = new HashMap<>();

    public static Type<GamestageState> TYPE = new Type<>(
        GamestageState::new,
        GamestageState::fromNbt,
        DataFixTypes.LEVEL
    );

    public GamestageState() {
    }

    public static GamestageState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        GamestageState state = new GamestageState();

        state.worldGamestage = nbt.getInt("worldGamestage");
        state.survivalNightsSurvived = nbt.getInt("survivalNightsSurvived");
        state.lastDayCheck = nbt.getLong("lastDayCheck");

        // Load player data
        NbtList playerList = nbt.getList("players", NbtCompound.COMPOUND_TYPE);
        for (int i = 0; i < playerList.size(); i++) {
            NbtCompound playerNbt = playerList.getCompound(i);
            UUID uuid = playerNbt.getUuid("uuid");
            String name = playerNbt.getString("name");

            PlayerGamestage player = new PlayerGamestage(uuid, name);
            player.setDaysSurvived(playerNbt.getInt("daysSurvived"));
            player.setZombieKills(playerNbt.getInt("zombieKills"));
            player.setSurvivalNightsSurvived(playerNbt.getInt("survivalNightsSurvived"));
            player.setDeathCount(playerNbt.getInt("deathCount"));

            state.playerData.put(uuid, player);
        }

        STNSurvival.LOGGER.info("Loaded gamestage data: worldGamestage={}, players={}",
            state.worldGamestage, state.playerData.size());

        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("worldGamestage", worldGamestage);
        nbt.putInt("survivalNightsSurvived", survivalNightsSurvived);
        nbt.putLong("lastDayCheck", lastDayCheck);

        // Save player data
        NbtList playerList = new NbtList();
        for (Map.Entry<UUID, PlayerGamestage> entry : playerData.entrySet()) {
            PlayerGamestage player = entry.getValue();
            NbtCompound playerNbt = new NbtCompound();

            playerNbt.putUuid("uuid", player.getPlayerUuid());
            playerNbt.putString("name", player.getPlayerName());
            playerNbt.putInt("daysSurvived", player.getDaysSurvived());
            playerNbt.putInt("zombieKills", player.getZombieKills());
            playerNbt.putInt("survivalNightsSurvived", player.getSurvivalNightsSurvived());
            playerNbt.putInt("deathCount", player.getDeathCount());

            playerList.add(playerNbt);
        }
        nbt.put("players", playerList);

        STNSurvival.LOGGER.info("Saved gamestage data: worldGamestage={}, players={}",
            worldGamestage, playerData.size());

        return nbt;
    }

    public static GamestageState get(MinecraftServer server) {
        PersistentStateManager stateManager = server.getOverworld().getPersistentStateManager();
        return stateManager.getOrCreate(TYPE, STATE_ID);
    }

    // Getters and setters
    public int getWorldGamestage() {
        return worldGamestage;
    }

    public void setWorldGamestage(int value) {
        this.worldGamestage = value;
        markDirty();
    }

    public int getSurvivalNightsSurvived() {
        return survivalNightsSurvived;
    }

    public void setSurvivalNightsSurvived(int value) {
        this.survivalNightsSurvived = value;
        markDirty();
    }

    public long getLastDayCheck() {
        return lastDayCheck;
    }

    public void setLastDayCheck(long value) {
        this.lastDayCheck = value;
        markDirty();
    }

    public Map<UUID, PlayerGamestage> getPlayerData() {
        return playerData;
    }

    public PlayerGamestage getOrCreatePlayerData(UUID uuid, String name) {
        return playerData.computeIfAbsent(uuid, id -> new PlayerGamestage(id, name));
    }
}
