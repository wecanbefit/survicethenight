package com.stn.zombies.client;

import com.stn.zombies.client.render.ScaledZombieRenderer;
import com.stn.zombies.registry.STNZombieEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.ZombieEntityRenderer;

/**
 * Client-side initialization for STN Zombies.
 * Registers entity renderers with custom scales.
 */
public class STNZombiesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // All zombies get custom texture paths - defaults to vanilla zombie texture
        // Resource packs can override: assets/stn_zombies/textures/entity/<name>.png

        EntityRendererRegistry.register(STNZombieEntities.BRUISER_ZOMBIE,
            ctx -> new ScaledZombieRenderer(ctx, 1.5f, "bruiser_zombie"));
        EntityRendererRegistry.register(STNZombieEntities.SPRINTER_ZOMBIE,
            ctx -> new ScaledZombieRenderer(ctx, 0.85f, "sprinter_zombie"));
        EntityRendererRegistry.register(STNZombieEntities.HOWLER_ZOMBIE,
            ctx -> new ScaledZombieRenderer(ctx, 0.75f, "howler_zombie"));
        EntityRendererRegistry.register(STNZombieEntities.LUMBERJACK_ZOMBIE,
            ctx -> new ScaledZombieRenderer(ctx, 1.25f, "lumberjack_zombie"));
        EntityRendererRegistry.register(STNZombieEntities.LEECH_ZOMBIE,
            ctx -> new ScaledZombieRenderer(ctx, 0.95f, "leech_zombie"));
        EntityRendererRegistry.register(STNZombieEntities.SPITTER_ZOMBIE,
            ctx -> new ScaledZombieRenderer(ctx, 1.0f, "spitter_zombie"));
        EntityRendererRegistry.register(STNZombieEntities.ZOMBABIE,
            ctx -> new ScaledZombieRenderer(ctx, 1.0f, "zombabie"));
        EntityRendererRegistry.register(STNZombieEntities.PLAGUE_ZOMBIE,
            ctx -> new ScaledZombieRenderer(ctx, 1.0f, "plague_zombie"));
        EntityRendererRegistry.register(STNZombieEntities.SHIELDED_ZOMBIE,
            ctx -> new ScaledZombieRenderer(ctx, 1.0f, "shielded_zombie"));
        EntityRendererRegistry.register(STNZombieEntities.ELECTRIC_ZOMBIE,
            ctx -> new ScaledZombieRenderer(ctx, 1.0f, "electric_zombie"));
    }
}
