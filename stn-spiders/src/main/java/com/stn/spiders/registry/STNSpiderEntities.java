package com.stn.spiders.registry;

import com.stn.spiders.entity.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/**
 * Registry for all STN spider entity types.
 */
public class STNSpiderEntities {

    public static final EntityType<StalkerSpiderEntity> STALKER_SPIDER = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of("stn_spiders", "stalker_spider"),
        EntityType.Builder.create(StalkerSpiderEntity::new, SpawnGroup.MONSTER)
            .dimensions(1.4f * 0.9f, 0.9f * 0.9f) // Scaled down
            .build()
    );

    public static final EntityType<WebspinnerSpiderEntity> WEBSPINNER_SPIDER = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of("stn_spiders", "webspinner_spider"),
        EntityType.Builder.create(WebspinnerSpiderEntity::new, SpawnGroup.MONSTER)
            .dimensions(1.4f * 1.1f, 0.9f * 1.1f) // Scaled up
            .build()
    );

    public static final EntityType<LeaperSpiderEntity> LEAPER_SPIDER = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of("stn_spiders", "leaper_spider"),
        EntityType.Builder.create(LeaperSpiderEntity::new, SpawnGroup.MONSTER)
            .dimensions(1.4f, 0.9f) // Normal size
            .build()
    );

    public static final EntityType<BroodmotherSpiderEntity> BROODMOTHER_SPIDER = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of("stn_spiders", "broodmother_spider"),
        EntityType.Builder.create(BroodmotherSpiderEntity::new, SpawnGroup.MONSTER)
            .dimensions(1.4f * 1.6f, 0.9f * 1.6f) // Large
            .build()
    );

    public static final EntityType<VenomSpiderEntity> VENOM_SPIDER = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of("stn_spiders", "venom_spider"),
        EntityType.Builder.create(VenomSpiderEntity::new, SpawnGroup.MONSTER)
            .dimensions(1.4f * 1.15f, 0.9f * 1.15f)
            .build()
    );

    public static final EntityType<BurdenSpiderEntity> BURDEN_SPIDER = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of("stn_spiders", "burden_spider"),
        EntityType.Builder.create(BurdenSpiderEntity::new, SpawnGroup.MONSTER)
            .dimensions(1.4f * 1.4f, 0.9f * 1.4f) // Large tank
            .build()
    );

    public static void register() {
        FabricDefaultAttributeRegistry.register(STALKER_SPIDER, StalkerSpiderEntity.createStalkerAttributes());
        FabricDefaultAttributeRegistry.register(WEBSPINNER_SPIDER, WebspinnerSpiderEntity.createWebspinnerAttributes());
        FabricDefaultAttributeRegistry.register(LEAPER_SPIDER, LeaperSpiderEntity.createLeaperAttributes());
        FabricDefaultAttributeRegistry.register(BROODMOTHER_SPIDER, BroodmotherSpiderEntity.createBroodmotherAttributes());
        FabricDefaultAttributeRegistry.register(VENOM_SPIDER, VenomSpiderEntity.createVenomAttributes());
        FabricDefaultAttributeRegistry.register(BURDEN_SPIDER, BurdenSpiderEntity.createBurdenAttributes());
    }
}
