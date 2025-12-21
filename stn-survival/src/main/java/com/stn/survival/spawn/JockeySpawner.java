package com.stn.survival.spawn;

import com.stn.survival.STNSurvival;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles spawning of all jockey combinations (rider + mount pairs).
 */
public class JockeySpawner {

    /**
     * Record representing a jockey combination.
     */
    public record JockeyType(
        String id,
        EntityType<?> mountType,
        EntityType<? extends MobEntity> riderType,
        int weight,
        int minGamestage,
        boolean riderIsBaby
    ) {}

    private static final List<JockeyType> JOCKEY_TYPES = new ArrayList<>();

    static {
        // Spider Jockeys
        register("spider_zombie", EntityType.SPIDER, EntityType.ZOMBIE, 10, 25, false);
        register("spider_skeleton", EntityType.SPIDER, EntityType.SKELETON, 10, 25, false);
        register("spider_stray", EntityType.SPIDER, EntityType.STRAY, 6, 40, false);

        // Chicken Jockeys
        register("chicken_baby_zombie", EntityType.CHICKEN, EntityType.ZOMBIE, 8, 15, true);
        register("chicken_baby_husk", EntityType.CHICKEN, EntityType.HUSK, 5, 20, true);
        register("chicken_baby_drowned", EntityType.CHICKEN, EntityType.DROWNED, 4, 25, true);

        // Skeleton Horse Jockeys (Horsemen)
        register("skeleton_horseman", EntityType.SKELETON_HORSE, EntityType.SKELETON, 4, 50, false);
        register("wither_horseman", EntityType.SKELETON_HORSE, EntityType.WITHER_SKELETON, 2, 80, false);
        register("stray_horseman", EntityType.SKELETON_HORSE, EntityType.STRAY, 3, 60, false);

        // Zombie Horse Jockeys
        register("zombie_horseman", EntityType.ZOMBIE_HORSE, EntityType.HUSK, 4, 60, false);
        register("zombie_drowned_rider", EntityType.ZOMBIE_HORSE, EntityType.DROWNED, 3, 55, false);

        // Ravager Jockeys
        register("ravager_pillager", EntityType.RAVAGER, EntityType.PILLAGER, 3, 75, false);
        register("ravager_vindicator", EntityType.RAVAGER, EntityType.VINDICATOR, 2, 85, false);
        register("ravager_evoker", EntityType.RAVAGER, EntityType.EVOKER, 1, 100, false);

        // Hoglin Jockeys
        register("hoglin_piglin_brute", EntityType.HOGLIN, EntityType.PIGLIN_BRUTE, 3, 65, false);

        // Strider Jockey
        register("strider_zombie", EntityType.STRIDER, EntityType.ZOMBIE, 2, 70, false);

        STNSurvival.LOGGER.info("Registered {} jockey types", JOCKEY_TYPES.size());
    }

    private static void register(String id, EntityType<?> mount, EntityType<? extends MobEntity> rider, int weight, int minGamestage, boolean riderIsBaby) {
        JOCKEY_TYPES.add(new JockeyType(id, mount, rider, weight, minGamestage, riderIsBaby));
    }

    /**
     * Register a custom jockey type from an addon mod.
     */
    public static void registerCustomJockey(String id, EntityType<?> mount, EntityType<? extends MobEntity> rider, int weight, int minGamestage, boolean riderIsBaby) {
        JOCKEY_TYPES.add(new JockeyType(id, mount, rider, weight, minGamestage, riderIsBaby));
        STNSurvival.LOGGER.info("Registered custom jockey: {}", id);
    }

    /**
     * Get all jockeys available at the current gamestage.
     */
    public static List<JockeyType> getAvailableJockeys(int gamestage) {
        return JOCKEY_TYPES.stream()
            .filter(jockey -> gamestage >= jockey.minGamestage())
            .toList();
    }

    /**
     * Check if a jockey should spawn instead of a regular mob.
     */
    public static boolean shouldSpawnJockey(Random random, int gamestage) {
        if (gamestage < 15) return false;

        int chance = 5 + (gamestage / 20);
        return random.nextInt(100) < Math.min(chance, 20);
    }

    /**
     * Select a random jockey type based on weights and gamestage.
     */
    public static JockeyType selectRandomJockey(Random random, int gamestage) {
        List<JockeyType> available = getAvailableJockeys(gamestage);
        if (available.isEmpty()) return null;

        int totalWeight = available.stream().mapToInt(JockeyType::weight).sum();
        int roll = random.nextInt(totalWeight);

        int accumulated = 0;
        for (JockeyType jockey : available) {
            accumulated += jockey.weight();
            if (roll < accumulated) {
                return jockey;
            }
        }

        return available.get(0);
    }

    /**
     * Spawn a jockey at the given position.
     */
    public static List<Entity> spawnJockey(ServerWorld world, BlockPos pos, Random random, int gamestage, ServerPlayerEntity target) {
        List<Entity> spawned = new ArrayList<>();

        JockeyType jockeyType = selectRandomJockey(random, gamestage);
        if (jockeyType == null) return spawned;

        Entity mount = jockeyType.mountType().create(world, SpawnReason.MOB_SUMMONED);
        if (mount == null) return spawned;

        MobEntity rider = jockeyType.riderType().create(world, SpawnReason.MOB_SUMMONED);
        if (rider == null) {
            mount.discard();
            return spawned;
        }

        if (jockeyType.riderIsBaby() && rider instanceof ZombieEntity zombie) {
            zombie.setBaby(true);
        }

        mount.refreshPositionAndAngles(pos, random.nextFloat() * 360f, 0);

        if (mount instanceof MobEntity mobMount) {
            mobMount.setPersistent();
        }

        if (mount instanceof HoglinEntity hoglin) {
            hoglin.setImmuneToZombification(true);
        }

        world.spawnEntity(mount);
        spawned.add(mount);

        rider.refreshPositionAndAngles(pos, random.nextFloat() * 360f, 0);
        rider.startRiding(mount, true);
        rider.setPersistent();

        if (target != null) {
            rider.setTarget(target);
        }

        world.spawnEntity(rider);
        spawned.add(rider);

        STNSurvival.LOGGER.debug("Spawned jockey: {} at {}", jockeyType.id(), pos);

        return spawned;
    }

    public static List<JockeyType> getAllJockeys() {
        return new ArrayList<>(JOCKEY_TYPES);
    }
}
