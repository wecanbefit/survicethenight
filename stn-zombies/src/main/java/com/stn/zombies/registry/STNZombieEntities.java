package com.stn.zombies.registry;

import com.stn.zombies.STNZombies;
import com.stn.zombies.entity.*;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * Registry for all STN zombie entity types.
 */
public class STNZombieEntities {

    public static final EntityType<BruiserZombieEntity> BRUISER_ZOMBIE = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(STNZombies.MOD_ID, "bruiser_zombie"),
        EntityType.Builder.create(BruiserZombieEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.95f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(STNZombies.MOD_ID, "bruiser_zombie")))
    );

    public static final EntityType<SprinterZombieEntity> SPRINTER_ZOMBIE = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(STNZombies.MOD_ID, "sprinter_zombie"),
        EntityType.Builder.create(SprinterZombieEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.95f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(STNZombies.MOD_ID, "sprinter_zombie")))
    );

    public static final EntityType<SpitterZombieEntity> SPITTER_ZOMBIE = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(STNZombies.MOD_ID, "spitter_zombie"),
        EntityType.Builder.create(SpitterZombieEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.95f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(STNZombies.MOD_ID, "spitter_zombie")))
    );

    public static final EntityType<ZombabieEntity> ZOMBABIE = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(STNZombies.MOD_ID, "zombabie"),
        EntityType.Builder.create(ZombabieEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.4f, 0.8f) // Small enough for 1-block gaps
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(STNZombies.MOD_ID, "zombabie")))
    );

    public static final EntityType<HowlerZombieEntity> HOWLER_ZOMBIE = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(STNZombies.MOD_ID, "howler_zombie"),
        EntityType.Builder.create(HowlerZombieEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.95f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(STNZombies.MOD_ID, "howler_zombie")))
    );

    public static final EntityType<PlagueZombieEntity> PLAGUE_ZOMBIE = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(STNZombies.MOD_ID, "plague_zombie"),
        EntityType.Builder.create(PlagueZombieEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.95f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(STNZombies.MOD_ID, "plague_zombie")))
    );

    public static final EntityType<ShieldedZombieEntity> SHIELDED_ZOMBIE = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(STNZombies.MOD_ID, "shielded_zombie"),
        EntityType.Builder.create(ShieldedZombieEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.95f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(STNZombies.MOD_ID, "shielded_zombie")))
    );

    public static final EntityType<ElectricZombieEntity> ELECTRIC_ZOMBIE = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(STNZombies.MOD_ID, "electric_zombie"),
        EntityType.Builder.create(ElectricZombieEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.95f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(STNZombies.MOD_ID, "electric_zombie")))
    );

    public static final EntityType<LeechZombieEntity> LEECH_ZOMBIE = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(STNZombies.MOD_ID, "leech_zombie"),
        EntityType.Builder.create(LeechZombieEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.95f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(STNZombies.MOD_ID, "leech_zombie")))
    );

    public static final EntityType<LumberjackZombieEntity> LUMBERJACK_ZOMBIE = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(STNZombies.MOD_ID, "lumberjack_zombie"),
        EntityType.Builder.create(LumberjackZombieEntity::new, SpawnGroup.MONSTER)
            .dimensions(0.6f, 1.95f)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(STNZombies.MOD_ID, "lumberjack_zombie")))
    );

    public static void register() {
        STNZombies.LOGGER.info("Registering STN zombie entities...");

        // Register attributes
        FabricDefaultAttributeRegistry.register(BRUISER_ZOMBIE, BruiserZombieEntity.createBruiserAttributes());
        FabricDefaultAttributeRegistry.register(SPRINTER_ZOMBIE, SprinterZombieEntity.createSprinterAttributes());
        FabricDefaultAttributeRegistry.register(SPITTER_ZOMBIE, SpitterZombieEntity.createSpitterAttributes());
        FabricDefaultAttributeRegistry.register(ZOMBABIE, ZombabieEntity.createZombabieAttributes());
        FabricDefaultAttributeRegistry.register(HOWLER_ZOMBIE, HowlerZombieEntity.createHowlerAttributes());
        FabricDefaultAttributeRegistry.register(PLAGUE_ZOMBIE, PlagueZombieEntity.createPlagueAttributes());
        FabricDefaultAttributeRegistry.register(SHIELDED_ZOMBIE, ShieldedZombieEntity.createShieldedAttributes());
        FabricDefaultAttributeRegistry.register(ELECTRIC_ZOMBIE, ElectricZombieEntity.createElectricAttributes());
        FabricDefaultAttributeRegistry.register(LEECH_ZOMBIE, LeechZombieEntity.createLeechAttributes());
        FabricDefaultAttributeRegistry.register(LUMBERJACK_ZOMBIE, LumberjackZombieEntity.createLumberjackAttributes());

        STNZombies.LOGGER.info("Registered {} zombie types", 10);
    }
}
