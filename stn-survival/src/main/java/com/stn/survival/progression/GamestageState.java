package com.stn.survival.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.stn.survival.STNSurvival;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
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

    // Codec for PlayerGamestage
    private static final Codec<PlayerGamestage> PLAYER_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Uuids.CODEC.fieldOf("uuid").forGetter(PlayerGamestage::getPlayerUuid),
            Codec.STRING.fieldOf("name").forGetter(PlayerGamestage::getPlayerName),
            Codec.INT.fieldOf("daysSurvived").forGetter(PlayerGamestage::getDaysSurvived),
            Codec.INT.fieldOf("zombieKills").forGetter(PlayerGamestage::getZombieKills),
            Codec.INT.fieldOf("survivalNightsSurvived").forGetter(PlayerGamestage::getSurvivalNightsSurvived),
            Codec.INT.fieldOf("deathCount").forGetter(PlayerGamestage::getDeathCount)
        ).apply(instance, (uuid, name, days, kills, nights, deaths) -> {
            PlayerGamestage p = new PlayerGamestage(uuid, name);
            p.setDaysSurvived(days);
            p.setZombieKills(kills);
            p.setSurvivalNightsSurvived(nights);
            p.setDeathCount(deaths);
            return p;
        })
    );

    // Codec for GamestageState
    private static final Codec<GamestageState> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.INT.fieldOf("worldGamestage").forGetter(s -> s.worldGamestage),
            Codec.INT.fieldOf("survivalNightsSurvived").forGetter(s -> s.survivalNightsSurvived),
            Codec.LONG.fieldOf("lastDayCheck").forGetter(s -> s.lastDayCheck),
            Codec.list(PLAYER_CODEC).fieldOf("players").forGetter(s -> List.copyOf(s.playerData.values()))
        ).apply(instance, (worldGs, survNights, lastDay, players) -> {
            GamestageState state = new GamestageState();
            state.worldGamestage = worldGs;
            state.survivalNightsSurvived = survNights;
            state.lastDayCheck = lastDay;
            for (PlayerGamestage p : players) {
                state.playerData.put(p.getPlayerUuid(), p);
            }
            STNSurvival.LOGGER.info("Loaded gamestage data: worldGamestage={}, players={}",
                state.worldGamestage, state.playerData.size());
            return state;
        })
    );

    private static final PersistentStateType<GamestageState> TYPE = new PersistentStateType<>(
        STATE_ID,
        GamestageState::new,
        CODEC,
        null
    );

    public GamestageState() {
    }

    public static GamestageState get(MinecraftServer server) {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Overworld not available");
        }
        GamestageState state = overworld.getPersistentStateManager().getOrCreate(TYPE);
        state.markDirty();
        return state;
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
