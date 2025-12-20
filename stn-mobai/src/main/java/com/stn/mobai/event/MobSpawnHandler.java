package com.stn.mobai.event;

import com.stn.mobai.config.STNMobAIConfig;
import com.stn.mobai.entity.ai.BreakBlockGoal;
import com.stn.mobai.entity.ai.MobSenseGoal;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * Handles mob spawn events to inject sensory AI goals.
 */
public class MobSpawnHandler {

    // Track entities that already have AI injected to prevent duplicates
    private static final Set<UUID> injectedEntities = Collections.newSetFromMap(new WeakHashMap<>());

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register(MobSpawnHandler::onEntityLoad);
    }

    private static void onEntityLoad(Entity entity, ServerWorld world) {
        if (!(entity instanceof HostileEntity hostile)) {
            return;
        }

        // Check if already injected
        if (injectedEntities.contains(entity.getUuid())) {
            return;
        }

        // Check config
        String entityId = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
        if (!STNMobAIConfig.shouldInjectAI(entityId)) {
            return;
        }

        // Mark as injected
        injectedEntities.add(entity.getUuid());

        // Add sensory AI goals
        // Priority 2-3 so they run before standard attack goals (usually priority 4+)
        hostile.goalSelector.add(2, new BreakBlockGoal(hostile,
            STNMobAIConfig.BLOCK_BREAK_SPEED_MULTIPLIER,
            40));
        hostile.goalSelector.add(3, new MobSenseGoal(hostile));
    }
}
