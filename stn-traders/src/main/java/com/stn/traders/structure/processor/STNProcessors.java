package com.stn.traders.structure.processor;

import com.stn.traders.STNTraders;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.Identifier;

/**
 * Registry for custom structure processors.
 */
public class STNProcessors {

    public static final StructureProcessorType<BiomeWoodProcessor> BIOME_WOOD =
        register("biome_wood", BiomeWoodProcessor.CODEC);

    private static <P extends net.minecraft.structure.processor.StructureProcessor> StructureProcessorType<P> register(
            String name, com.mojang.serialization.MapCodec<P> codec) {
        return Registry.register(
            Registries.STRUCTURE_PROCESSOR,
            Identifier.of(STNTraders.MOD_ID, name),
            () -> codec
        );
    }

    public static void register() {
        STNTraders.LOGGER.info("Registered structure processors");
    }
}
