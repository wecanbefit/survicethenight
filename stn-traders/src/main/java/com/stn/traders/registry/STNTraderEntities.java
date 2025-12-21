package com.stn.traders.registry;

import com.stn.traders.STNTraders;
import com.stn.traders.entity.SurvivalTraderEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * Registry for STN trader entity types.
 */
public class STNTraderEntities {

    public static final EntityType<SurvivalTraderEntity> SURVIVAL_TRADER = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(STNTraders.MOD_ID, "survival_trader"),
        EntityType.Builder.create(SurvivalTraderEntity::new, SpawnGroup.CREATURE)
            .dimensions(0.6f, 1.95f) // Same as villager
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(STNTraders.MOD_ID, "survival_trader")))
    );

    /**
     * Register all trader entities and their attributes.
     */
    public static void register() {
        STNTraders.LOGGER.info("Registering STN trader entities...");

        FabricDefaultAttributeRegistry.register(
            SURVIVAL_TRADER,
            SurvivalTraderEntity.createTraderAttributes()
        );

        STNTraders.LOGGER.info("Registered 1 trader type");
    }
}
