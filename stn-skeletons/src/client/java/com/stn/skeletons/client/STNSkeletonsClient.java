package com.stn.skeletons.client;

import com.stn.skeletons.client.render.ScaledSkeletonRenderer;
import com.stn.skeletons.client.render.ScaledWitherSkeletonRenderer;
import com.stn.skeletons.registry.STNSkeletonEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

/**
 * Client-side initialization for STN Skeletons.
 * Registers entity renderers with custom scales.
 */
public class STNSkeletonsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // All skeletons get custom texture paths
        // Resource packs can override: assets/stn_skeletons/textures/entity/<name>.png

        // Bow Skeletons - use regular skeleton model
        EntityRendererRegistry.register(STNSkeletonEntities.MARKSMAN_SKELETON,
            ctx -> new ScaledSkeletonRenderer(ctx, 1.0f, "marksman_skeleton"));
        EntityRendererRegistry.register(STNSkeletonEntities.SUPPRESSOR_SKELETON,
            ctx -> new ScaledSkeletonRenderer(ctx, 1.0f, "suppressor_skeleton"));
        EntityRendererRegistry.register(STNSkeletonEntities.FLAME_ARCHER_SKELETON,
            ctx -> new ScaledSkeletonRenderer(ctx, 1.0f, "flame_archer_skeleton"));

        // Black Skeletons - use wither skeleton model
        EntityRendererRegistry.register(STNSkeletonEntities.VANGUARD_SKELETON,
            ctx -> new ScaledWitherSkeletonRenderer(ctx, 1.25f, "vanguard_skeleton"));
        EntityRendererRegistry.register(STNSkeletonEntities.DUELIST_SKELETON,
            ctx -> new ScaledWitherSkeletonRenderer(ctx, 1.0f, "duelist_skeleton"));
        EntityRendererRegistry.register(STNSkeletonEntities.REAPER_SKELETON,
            ctx -> new ScaledWitherSkeletonRenderer(ctx, 1.0f, "reaper_skeleton"));
    }
}
