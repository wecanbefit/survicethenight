package com.stn.core.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Central event registry for Survive The Night mods.
 * Other mods can register listeners to respond to STN events.
 */
public final class STNEvents {

    private STNEvents() {}

    /**
     * Initialize the event system.
     */
    public static void init() {
        // Events are initialized statically, nothing to do here
    }

    // ==================== Blood Moon Events ====================

    /**
     * Called when a blood moon starts.
     */
    public static final Event<BloodMoonStart> BLOOD_MOON_START = EventFactory.createArrayBacked(
        BloodMoonStart.class,
        callbacks -> (world, gameDay) -> {
            for (BloodMoonStart callback : callbacks) {
                callback.onBloodMoonStart(world, gameDay);
            }
        }
    );

    /**
     * Called when a blood moon ends.
     */
    public static final Event<BloodMoonEnd> BLOOD_MOON_END = EventFactory.createArrayBacked(
        BloodMoonEnd.class,
        callbacks -> (world, mobsSpawned, survived) -> {
            for (BloodMoonEnd callback : callbacks) {
                callback.onBloodMoonEnd(world, mobsSpawned, survived);
            }
        }
    );

    // ==================== Gamestage Events ====================

    /**
     * Called when a player's gamestage changes.
     */
    public static final Event<GamestageChanged> GAMESTAGE_CHANGED = EventFactory.createArrayBacked(
        GamestageChanged.class,
        callbacks -> (player, oldStage, newStage) -> {
            for (GamestageChanged callback : callbacks) {
                callback.onGamestageChanged(player, oldStage, newStage);
            }
        }
    );

    // ==================== Sound Events ====================

    /**
     * Called when a sound is registered that zombies can detect.
     */
    public static final Event<SoundRegistered> SOUND_REGISTERED = EventFactory.createArrayBacked(
        SoundRegistered.class,
        callbacks -> (world, pos, volume, type) -> {
            for (SoundRegistered callback : callbacks) {
                callback.onSoundRegistered(world, pos, volume, type);
            }
        }
    );

    // ==================== Block Durability Events ====================

    /**
     * Called when a block takes durability damage.
     */
    public static final Event<BlockDamaged> BLOCK_DAMAGED = EventFactory.createArrayBacked(
        BlockDamaged.class,
        callbacks -> (world, pos, damage, remainingDurability) -> {
            for (BlockDamaged callback : callbacks) {
                callback.onBlockDamaged(world, pos, damage, remainingDurability);
            }
        }
    );

    /**
     * Called when a block is repaired.
     */
    public static final Event<BlockRepaired> BLOCK_REPAIRED = EventFactory.createArrayBacked(
        BlockRepaired.class,
        callbacks -> (world, pos, amount, newDurability) -> {
            for (BlockRepaired callback : callbacks) {
                callback.onBlockRepaired(world, pos, amount, newDurability);
            }
        }
    );

    /**
     * Called when a tracked block is destroyed (durability reaches 0).
     */
    public static final Event<BlockDestroyed> BLOCK_DESTROYED = EventFactory.createArrayBacked(
        BlockDestroyed.class,
        callbacks -> (world, pos) -> {
            for (BlockDestroyed callback : callbacks) {
                callback.onBlockDestroyed(world, pos);
            }
        }
    );

    // ==================== Event Interfaces ====================

    @FunctionalInterface
    public interface BloodMoonStart {
        void onBloodMoonStart(ServerWorld world, long gameDay);
    }

    @FunctionalInterface
    public interface BloodMoonEnd {
        void onBloodMoonEnd(ServerWorld world, int mobsSpawned, boolean survived);
    }

    @FunctionalInterface
    public interface GamestageChanged {
        void onGamestageChanged(ServerPlayerEntity player, int oldStage, int newStage);
    }

    @FunctionalInterface
    public interface SoundRegistered {
        void onSoundRegistered(ServerWorld world, BlockPos pos, float volume, ISoundEmitter.SoundType type);
    }

    @FunctionalInterface
    public interface BlockDamaged {
        void onBlockDamaged(ServerWorld world, BlockPos pos, int damage, int remainingDurability);
    }

    @FunctionalInterface
    public interface BlockRepaired {
        void onBlockRepaired(ServerWorld world, BlockPos pos, int amount, int newDurability);
    }

    @FunctionalInterface
    public interface BlockDestroyed {
        void onBlockDestroyed(ServerWorld world, BlockPos pos);
    }
}
