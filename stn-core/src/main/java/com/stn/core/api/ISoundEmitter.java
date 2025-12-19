package com.stn.core.api;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Interface for emitting sounds that zombies can detect.
 * Implemented by stn-zombies to provide sound registration for other mods.
 */
public interface ISoundEmitter {

    /**
     * Register a sound event at a position.
     * @param world the server world
     * @param position where the sound originated
     * @param volume sound volume (0.0 - 1.0)
     * @param source entity that caused the sound (nullable)
     * @param type type of sound
     */
    void emitSound(ServerWorld world, BlockPos position, float volume, LivingEntity source, SoundType type);

    /**
     * Register a sound event without a source entity.
     * @param world the server world
     * @param position where the sound originated
     * @param volume sound volume (0.0 - 1.0)
     * @param type type of sound
     */
    default void emitSound(ServerWorld world, BlockPos position, float volume, SoundType type) {
        emitSound(world, position, volume, null, type);
    }

    /**
     * Types of sounds that can be emitted.
     */
    enum SoundType {
        GENERIC,
        BLOCK_BREAK,
        BLOCK_PLACE,
        DOOR_USE,
        CHEST_OPEN,
        COMBAT,
        MOVEMENT,
        EXPLOSION
    }
}
