package com.stn.mobai.entity.ai.sense;

import com.stn.core.api.ISoundEmitter;
import com.stn.mobai.STNMobAI;
import com.stn.mobai.config.STNMobAIConfig;
import com.stn.mobai.debug.SenseDebugger;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages sensory events across all dimensions.
 * Provides a centralized system for registering and querying sounds
 * that mobs can detect.
 *
 * Implements ISoundEmitter to allow other mods to emit sounds.
 */
public class SenseManager implements ISoundEmitter {
    private static SenseManager instance;

    // Sound events per dimension
    private final Map<String, List<SoundEvent>> soundEventsByDimension = new ConcurrentHashMap<>();

    // Configuration
    private int soundDecayTicks = 100; // 5 seconds default

    private SenseManager() {}

    public static SenseManager getInstance() {
        if (instance == null) {
            instance = new SenseManager();
        }
        return instance;
    }

    /**
     * Initialize or reset the manager with config values.
     */
    public void init() {
        soundDecayTicks = STNMobAIConfig.SOUND_DECAY_TICKS;
        STNMobAI.LOGGER.info("SenseManager initialized with {} tick sound decay", soundDecayTicks);
    }

    // ISoundEmitter implementation
    @Override
    public void emitSound(ServerWorld world, BlockPos position, float volume, LivingEntity source, SoundType type) {
        registerSound(world, position, volume, source, type);
    }

    /**
     * Register a sound event at a position.
     */
    public void registerSound(ServerWorld world, BlockPos position, float volume, LivingEntity source, ISoundEmitter.SoundType type) {
        String dimensionKey = world.getRegistryKey().getValue().toString();
        long currentTick = world.getTime();

        UUID sourceId = source != null ? source.getUuid() : null;
        SoundEvent event = new SoundEvent(position, volume, currentTick, sourceId, type);

        soundEventsByDimension.computeIfAbsent(dimensionKey, k -> Collections.synchronizedList(new ArrayList<>()))
            .add(event);

        // Debug output
        SenseDebugger.onSoundEmitted(world, position, volume, type.name());
    }

    /**
     * Register a sound event without a source entity.
     */
    public void registerSound(ServerWorld world, BlockPos position, float volume, ISoundEmitter.SoundType type) {
        registerSound(world, position, volume, null, type);
    }

    /**
     * Get all sounds within range of a position that haven't decayed.
     */
    public List<SoundEvent> getSoundsInRange(ServerWorld world, BlockPos listenerPos, double maxRange) {
        String dimensionKey = world.getRegistryKey().getValue().toString();
        long currentTick = world.getTime();

        List<SoundEvent> dimensionSounds = soundEventsByDimension.get(dimensionKey);
        if (dimensionSounds == null || dimensionSounds.isEmpty()) {
            return Collections.emptyList();
        }

        List<SoundEvent> result = new ArrayList<>();

        synchronized (dimensionSounds) {
            for (SoundEvent event : dimensionSounds) {
                if (event.isExpired(currentTick, soundDecayTicks)) {
                    continue;
                }

                double distance = listenerPos.getSquaredDistance(event.getPosition());
                double effectiveRange = event.getMaxDetectionRange(maxRange);

                if (distance <= effectiveRange * effectiveRange) {
                    result.add(event);
                }
            }
        }

        return result;
    }

    /**
     * Get the loudest/closest sound for a listener.
     */
    public Optional<SoundEvent> getLoudestSound(ServerWorld world, BlockPos listenerPos, double maxRange) {
        List<SoundEvent> sounds = getSoundsInRange(world, listenerPos, maxRange);
        long currentTick = world.getTime();

        return sounds.stream()
            .max(Comparator.comparingDouble(event -> {
                double distance = Math.sqrt(listenerPos.getSquaredDistance(event.getPosition()));
                double effectiveRange = event.getMaxDetectionRange(maxRange);
                double proximity = 1.0 - (distance / effectiveRange);
                return event.getEffectiveVolume(listenerPos, currentTick, soundDecayTicks) * proximity;
            }));
    }

    /**
     * Calculate a weighted score for a sound event.
     */
    public double calculateSoundScore(ServerWorld world, BlockPos listenerPos, SoundEvent event, double maxRange) {
        long currentTick = world.getTime();
        double distance = Math.sqrt(listenerPos.getSquaredDistance(event.getPosition()));
        double effectiveRange = event.getMaxDetectionRange(maxRange);

        if (distance > effectiveRange) {
            return 0.0;
        }

        float effectiveVolume = event.getEffectiveVolume(listenerPos, currentTick, soundDecayTicks);
        double proximity = 1.0 - (distance / effectiveRange);

        return effectiveVolume * proximity * 100.0;
    }

    /**
     * Tick the manager - remove expired sound events.
     */
    public void tick(ServerWorld world) {
        String dimensionKey = world.getRegistryKey().getValue().toString();
        long currentTick = world.getTime();

        List<SoundEvent> dimensionSounds = soundEventsByDimension.get(dimensionKey);
        if (dimensionSounds != null) {
            synchronized (dimensionSounds) {
                dimensionSounds.removeIf(event -> event.isExpired(currentTick, soundDecayTicks));
            }
        }
    }

    /**
     * Clear all sound events.
     */
    public void clear() {
        soundEventsByDimension.clear();
    }

    /**
     * Clear sounds for a specific dimension.
     */
    public void clearDimension(String dimensionKey) {
        soundEventsByDimension.remove(dimensionKey);
    }

    /**
     * Get count of active sounds in a dimension.
     */
    public int getActiveSoundCount(ServerWorld world) {
        String dimensionKey = world.getRegistryKey().getValue().toString();
        List<SoundEvent> sounds = soundEventsByDimension.get(dimensionKey);
        return sounds != null ? sounds.size() : 0;
    }

    public int getSoundDecayTicks() {
        return soundDecayTicks;
    }

    public void setSoundDecayTicks(int ticks) {
        this.soundDecayTicks = ticks;
    }
}
