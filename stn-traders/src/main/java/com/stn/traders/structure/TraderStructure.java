package com.stn.traders.structure;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;

/**
 * Represents a trader structure definition.
 * Structures are loaded from NBT files in data/stn_traders/structures/
 */
public record TraderStructure(
    Identifier id,
    Identifier templateId,    // Points to data/stn_traders/structures/xxx.nbt
    StructureType type,
    int weight,               // Spawn weight (higher = more common)
    int minGamestage,         // Minimum gamestage to spawn this variant
    Vec3i dimensions,         // Structure dimensions for placement validation
    boolean spawnsMerchant    // Whether to spawn trader NPC inside
) {

    public enum StructureType {
        BOOTH,    // Small roadside stand
        SHOP,     // Medium trading post
        BUNKER    // Fortified safe house
    }

    /**
     * Create a structure with default settings.
     * Template ID maps to data/stn_traders/structure/<name>.nbt
     */
    public static TraderStructure create(String name, StructureType type, int weight, int minGamestage, Vec3i dimensions) {
        Identifier id = Identifier.of("stn_traders", name);
        // StructureTemplateManager automatically looks in data/<namespace>/structure/
        Identifier templateId = Identifier.of("stn_traders", name);
        return new TraderStructure(id, templateId, type, weight, minGamestage, dimensions, true);
    }
}
