package com.stn.skeletons.registry;

import com.stn.skeletons.entity.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * Registry for all STN skeleton entity types.
 */
public class STNSkeletonEntities {

    private static final String MOD_ID = "stn_skeletons";

    // Bow Skeletons
    public static final EntityType<MarksmanSkeletonEntity> MARKSMAN_SKELETON = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "marksman_skeleton"),
        EntityType.Builder.create(MarksmanSkeletonEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.99f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "marksman_skeleton")))
    );

    public static final EntityType<SuppressorSkeletonEntity> SUPPRESSOR_SKELETON = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "suppressor_skeleton"),
        EntityType.Builder.create(SuppressorSkeletonEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.99f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "suppressor_skeleton")))
    );

    public static final EntityType<FlameArcherSkeletonEntity> FLAME_ARCHER_SKELETON = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "flame_archer_skeleton"),
        EntityType.Builder.create(FlameArcherSkeletonEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.99f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "flame_archer_skeleton")))
    );

    // Black Skeletons (Wither Skeleton size)
    public static final EntityType<VanguardSkeletonEntity> VANGUARD_SKELETON = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "vanguard_skeleton"),
        EntityType.Builder.create(VanguardSkeletonEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.7f, 2.4f) // Wither skeleton size
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "vanguard_skeleton")))
    );

    public static final EntityType<DuelistSkeletonEntity> DUELIST_SKELETON = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "duelist_skeleton"),
        EntityType.Builder.create(DuelistSkeletonEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.7f, 2.4f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "duelist_skeleton")))
    );

    public static final EntityType<ReaperSkeletonEntity> REAPER_SKELETON = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "reaper_skeleton"),
        EntityType.Builder.create(ReaperSkeletonEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.7f, 2.4f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "reaper_skeleton")))
    );

    public static void register() {
        // Register attributes for all skeleton types
        FabricDefaultAttributeRegistry.register(MARKSMAN_SKELETON, MarksmanSkeletonEntity.createMarksmanAttributes());
        FabricDefaultAttributeRegistry.register(SUPPRESSOR_SKELETON, SuppressorSkeletonEntity.createSuppressorAttributes());
        FabricDefaultAttributeRegistry.register(FLAME_ARCHER_SKELETON, FlameArcherSkeletonEntity.createFlameArcherAttributes());
        FabricDefaultAttributeRegistry.register(VANGUARD_SKELETON, VanguardSkeletonEntity.createVanguardAttributes());
        FabricDefaultAttributeRegistry.register(DUELIST_SKELETON, DuelistSkeletonEntity.createDuelistAttributes());
        FabricDefaultAttributeRegistry.register(REAPER_SKELETON, ReaperSkeletonEntity.createReaperAttributes());
    }
}
