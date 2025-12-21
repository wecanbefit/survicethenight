package com.stn.traders.structure;

import com.stn.traders.STNTraders;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;

import java.util.*;

/**
 * Registry for all trader structures.
 * Provides weighted random selection based on gamestage.
 */
public class TraderStructureRegistry {

    private static final Map<Identifier, TraderStructure> STRUCTURES = new HashMap<>();

    /**
     * Register a trader structure.
     */
    public static void register(TraderStructure structure) {
        STRUCTURES.put(structure.id(), structure);
        STNTraders.LOGGER.debug("Registered trader structure: {}", structure.id());
    }

    /**
     * Get a structure by ID.
     */
    public static TraderStructure get(Identifier id) {
        return STRUCTURES.get(id);
    }

    /**
     * Get all registered structures.
     */
    public static Collection<TraderStructure> getAll() {
        return Collections.unmodifiableCollection(STRUCTURES.values());
    }

    /**
     * Select a random structure appropriate for the given gamestage.
     * Uses weighted random selection.
     */
    public static TraderStructure selectForGamestage(Random random, int gamestage) {
        // Filter by gamestage
        List<TraderStructure> eligible = STRUCTURES.values().stream()
            .filter(s -> s.minGamestage() <= gamestage)
            .toList();

        if (eligible.isEmpty()) {
            // Fallback to any structure
            eligible = new ArrayList<>(STRUCTURES.values());
        }

        if (eligible.isEmpty()) {
            return null;
        }

        // Weighted random selection
        int totalWeight = eligible.stream().mapToInt(TraderStructure::weight).sum();
        if (totalWeight <= 0) {
            return eligible.get(random.nextInt(eligible.size()));
        }

        int roll = random.nextInt(totalWeight);
        int current = 0;

        for (TraderStructure s : eligible) {
            current += s.weight();
            if (roll < current) {
                return s;
            }
        }

        return eligible.get(0);
    }

    /**
     * Register default built-in structures.
     * More structures can be added later.
     */
    public static void registerDefaults() {
        // Small trader booth - always available
        register(TraderStructure.create("trader_small_1", TraderStructure.StructureType.BOOTH,
            100, 0, new Vec3i(7, 6, 7)));

        STNTraders.LOGGER.info("Registered {} trader structures", STRUCTURES.size());
    }
}
