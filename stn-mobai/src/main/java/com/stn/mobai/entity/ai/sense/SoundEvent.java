package com.stn.mobai.entity.ai.sense;

import com.stn.core.api.ISoundEmitter;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * Represents a sound event in the world that mobs can detect.
 * Sound events have a position, volume, and decay over time.
 */
public class SoundEvent {
    private final BlockPos position;
    private final float volume;
    private final long createdTick;
    private final UUID sourceId;
    private final ISoundEmitter.SoundType soundType;

    public SoundEvent(BlockPos position, float volume, long createdTick, UUID sourceId, ISoundEmitter.SoundType soundType) {
        this.position = position;
        this.volume = Math.min(1.0f, Math.max(0.0f, volume));
        this.createdTick = createdTick;
        this.sourceId = sourceId;
        this.soundType = soundType;
    }

    public SoundEvent(BlockPos position, float volume, long createdTick) {
        this(position, volume, createdTick, null, ISoundEmitter.SoundType.GENERIC);
    }

    public BlockPos getPosition() {
        return position;
    }

    public float getVolume() {
        return volume;
    }

    public long getCreatedTick() {
        return createdTick;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public ISoundEmitter.SoundType getSoundType() {
        return soundType;
    }

    /**
     * Calculate the effective volume at a given distance, accounting for decay over time.
     * @param listenerPos Position of the listener
     * @param currentTick Current game tick
     * @param decayTicks Total ticks until sound fully decays
     * @return Effective volume (0.0 - 1.0)
     */
    public float getEffectiveVolume(BlockPos listenerPos, long currentTick, int decayTicks) {
        long age = currentTick - createdTick;
        if (age >= decayTicks) {
            return 0.0f;
        }
        float timeDecay = 1.0f - ((float) age / decayTicks);
        return volume * timeDecay;
    }

    /**
     * Check if this sound has expired.
     */
    public boolean isExpired(long currentTick, int decayTicks) {
        return (currentTick - createdTick) >= decayTicks;
    }

    /**
     * Get the maximum detection range for this sound.
     * Based on volume and base detection range.
     */
    public double getMaxDetectionRange(double baseRange) {
        return baseRange * volume;
    }
}
