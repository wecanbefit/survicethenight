package com.stn.spiders.client;

import com.stn.spiders.client.render.ScaledSpiderRenderer;
import com.stn.spiders.registry.STNSpiderEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.SpiderEntityRenderer;

/**
 * Client-side initialization for STN Spiders.
 * Registers entity renderers with custom scales.
 */
public class STNSpidersClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // All spiders get custom texture paths
        // Resource packs can override: assets/stn_spiders/textures/entity/<name>.png

        EntityRendererRegistry.register(STNSpiderEntities.STALKER_SPIDER,
            ctx -> new ScaledSpiderRenderer<>(ctx, 0.9f, "stalker_spider"));
        EntityRendererRegistry.register(STNSpiderEntities.WEBSPINNER_SPIDER,
            ctx -> new ScaledSpiderRenderer<>(ctx, 1.1f, "webspinner_spider"));
        EntityRendererRegistry.register(STNSpiderEntities.LEAPER_SPIDER,
            ctx -> new ScaledSpiderRenderer<>(ctx, 1.0f, "leaper_spider"));
        EntityRendererRegistry.register(STNSpiderEntities.BROODMOTHER_SPIDER,
            ctx -> new ScaledSpiderRenderer<>(ctx, 1.6f, "broodmother_spider"));
        EntityRendererRegistry.register(STNSpiderEntities.VENOM_SPIDER,
            ctx -> new ScaledSpiderRenderer<>(ctx, 1.15f, "venom_spider"));
        EntityRendererRegistry.register(STNSpiderEntities.BURDEN_SPIDER,
            ctx -> new ScaledSpiderRenderer<>(ctx, 1.4f, "burden_spider"));
    }
}
