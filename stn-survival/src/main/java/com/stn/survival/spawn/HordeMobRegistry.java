package com.stn.survival.spawn;

import com.stn.survival.STNSurvival;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralized registry of all mobs that can spawn during Survival Night events.
 * Each mob has a weight (spawn chance), minimum gamestage, and spawn category.
 */
public class HordeMobRegistry {

    /**
     * Record representing a mob type that can spawn in the horde.
     */
    public record HordeMob(
        String id,
        EntityType<? extends MobEntity> entityType,
        int weight,
        int minGamestage,
        MobCategory category,
        boolean isCustom
    ) {}

    private static final List<HordeMob> HORDE_MOBS = new ArrayList<>();

    static {
        // ========== TIER 1: Gamestage 0-10 (Base horde) ==========

        // Vanilla zombies
        register("zombie", EntityType.ZOMBIE, 20, 0, MobCategory.GROUND, false);
        register("husk", EntityType.HUSK, 10, 0, MobCategory.GROUND, false);
        register("zombie_villager", EntityType.ZOMBIE_VILLAGER, 8, 0, MobCategory.GROUND, false);
        register("drowned", EntityType.DROWNED, 6, 0, MobCategory.GROUND, false);

        // Custom zombies will be registered by stn-zombies if present
        // They use the registerCustomMob() method

        STNSurvival.LOGGER.info("Registered {} base horde mob types", HORDE_MOBS.size());
    }

    private static void register(String id, EntityType<? extends MobEntity> type, int weight, int minGamestage, MobCategory category, boolean isCustom) {
        HORDE_MOBS.add(new HordeMob(id, type, weight, minGamestage, category, isCustom));
    }

    /**
     * Register a custom mob type from an addon mod.
     */
    public static void registerCustomMob(String id, EntityType<? extends MobEntity> type, int weight, int minGamestage, MobCategory category) {
        HORDE_MOBS.add(new HordeMob(id, type, weight, minGamestage, category, true));
        STNSurvival.LOGGER.info("Registered custom horde mob: {} (weight: {}, gamestage: {})", id, weight, minGamestage);
    }

    /**
     * Get all mobs available at the current gamestage.
     */
    public static List<HordeMob> getAvailableMobs(int gamestage) {
        return HORDE_MOBS.stream()
            .filter(mob -> gamestage >= mob.minGamestage())
            .toList();
    }

    /**
     * Get mobs of a specific category available at the current gamestage.
     */
    public static List<HordeMob> getAvailableMobs(int gamestage, MobCategory category) {
        return HORDE_MOBS.stream()
            .filter(mob -> gamestage >= mob.minGamestage() && mob.category() == category)
            .toList();
    }

    /**
     * Select a random mob based on weights and current gamestage.
     */
    public static HordeMob selectRandomMob(Random random, int gamestage) {
        List<HordeMob> available = getAvailableMobs(gamestage);
        if (available.isEmpty()) {
            return new HordeMob("zombie", EntityType.ZOMBIE, 20, 0, MobCategory.GROUND, false);
        }

        int totalWeight = available.stream().mapToInt(HordeMob::weight).sum();
        int roll = random.nextInt(totalWeight);

        int accumulated = 0;
        for (HordeMob mob : available) {
            accumulated += mob.weight();
            if (roll < accumulated) {
                return mob;
            }
        }

        return available.get(0);
    }

    /**
     * Select a random mob of a specific category.
     */
    public static HordeMob selectRandomMob(Random random, int gamestage, MobCategory category) {
        List<HordeMob> available = getAvailableMobs(gamestage, category);
        if (available.isEmpty()) {
            return null;
        }

        int totalWeight = available.stream().mapToInt(HordeMob::weight).sum();
        int roll = random.nextInt(totalWeight);

        int accumulated = 0;
        for (HordeMob mob : available) {
            accumulated += mob.weight();
            if (roll < accumulated) {
                return mob;
            }
        }

        return available.get(0);
    }

    /**
     * Check if boss mobs should have a chance to spawn.
     */
    public static boolean shouldAttemptBossSpawn(Random random, int gamestage) {
        if (gamestage < 100) return false;
        int chance = 1 + (gamestage - 100) / 25;
        return random.nextInt(100) < chance;
    }

    /**
     * Get a boss mob for high gamestage.
     */
    public static HordeMob selectBossMob(Random random, int gamestage) {
        List<HordeMob> bosses = HORDE_MOBS.stream()
            .filter(mob -> mob.id().equals("giant") || mob.id().equals("warden"))
            .filter(mob -> gamestage >= mob.minGamestage())
            .toList();

        if (bosses.isEmpty()) return null;

        int totalWeight = bosses.stream().mapToInt(HordeMob::weight).sum();
        int roll = random.nextInt(totalWeight);

        int accumulated = 0;
        for (HordeMob boss : bosses) {
            accumulated += boss.weight();
            if (roll < accumulated) {
                return boss;
            }
        }

        return bosses.get(0);
    }

    public static List<HordeMob> getAllMobs() {
        return new ArrayList<>(HORDE_MOBS);
    }
}
