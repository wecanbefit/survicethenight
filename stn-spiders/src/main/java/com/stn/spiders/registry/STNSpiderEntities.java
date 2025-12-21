package com.stn.spiders.registry;

import com.stn.spiders.entity.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * Registry for all STN spider entity types.
 */
public class STNSpiderEntities {

    private static final String MOD_ID = "stn_spiders";

    public static final EntityType<StalkerSpiderEntity> STALKER_SPIDER = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "stalker_spider"),
        EntityType.Builder.create(StalkerSpiderEntity::new, SpawnGroup.MONSTER)
            .dimensions(1.4f * 0.9f, 0.9f * 0.9f) // Scaled down
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "stalker_spider")))
    );

    public static final EntityType<WebspinnerSpiderEntity> WEBSPINNER_SPIDER = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "webspinner_spider"),
        EntityType.Builder.create(WebspinnerSpiderEntity::new, SpawnGroup.MONSTER)
            .dimensions(1.4f * 1.1f, 0.9f * 1.1f) // Scaled up
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "webspinner_spider")))
    );

    public static final EntityType<LeaperSpiderEntity> LEAPER_SPIDER = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "leaper_spider"),
        EntityType.Builder.create(LeaperSpiderEntity::new, SpawnGroup.MONSTER)
            .dimensions(1.4f, 0.9f) // Normal size
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "leaper_spider")))
    );

    public static final EntityType<BroodmotherSpiderEntity> BROODMOTHER_SPIDER = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "broodmother_spider"),
        EntityType.Builder.create(BroodmotherSpiderEntity::new, SpawnGroup.MONSTER)
            .dimensions(1.4f * 1.6f, 0.9f * 1.6f) // Large
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "broodmother_spider")))
    );

    public static final EntityType<VenomSpiderEntity> VENOM_SPIDER = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "venom_spider"),
        EntityType.Builder.create(VenomSpiderEntity::new, SpawnGroup.MONSTER)
            .dimensions(1.4f * 1.15f, 0.9f * 1.15f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "venom_spider")))
    );

    public static final EntityType<BurdenSpiderEntity> BURDEN_SPIDER = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "burden_spider"),
        EntityType.Builder.create(BurdenSpiderEntity::new, SpawnGroup.MONSTER)
            .dimensions(1.4f * 1.4f, 0.9f * 1.4f) // Large tank
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "burden_spider")))
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
