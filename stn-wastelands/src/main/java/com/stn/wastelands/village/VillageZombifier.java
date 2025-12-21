package com.stn.wastelands.village;

import com.stn.core.STNCore;
import com.stn.core.api.IGamestageProvider;
import com.stn.wastelands.config.WastelandConfig;
import com.stn.zombies.registry.STNZombieEntities;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Collections;

/**
 * Handles replacing villagers with zombies in villages to create wasteland towns.
 */
public class VillageZombifier {
    private static final Logger LOGGER = LoggerFactory.getLogger("STN-Wastelands");
    private static final Random RANDOM = new Random();

    // Track processed entities with IdentityHashMap for O(1) identity-based lookup
    private static final Set<Entity> processedEntities = Collections.newSetFromMap(new IdentityHashMap<>());

    // Queue for entities to convert (processed on next tick, single-threaded access)
    private static final List<EntityConversion> conversionQueue = new ArrayList<>();

    // Cached zombie type lists per gamestage tier (lazily initialized to avoid compile-time type resolution)
    private static List<EntityType<?>> BASIC_TIER_ZOMBIES;
    private static List<EntityType<?>> LOW_TIER_ZOMBIES;
    private static List<EntityType<?>> MID_TIER_ZOMBIES;
    private static List<EntityType<?>> HIGH_TIER_ZOMBIES;
    private static List<EntityType<?>> ELITE_TIER_ZOMBIES;

    private static void initZombieLists() {
        if (BASIC_TIER_ZOMBIES != null) return; // Already initialized

        BASIC_TIER_ZOMBIES = List.of(
                STNZombieEntities.SPRINTER_ZOMBIE,
                EntityType.ZOMBIE,
                EntityType.ZOMBIE  // Higher chance for regular zombies
        );
        LOW_TIER_ZOMBIES = List.of(
                STNZombieEntities.SPRINTER_ZOMBIE,
                STNZombieEntities.SPITTER_ZOMBIE,
                STNZombieEntities.HOWLER_ZOMBIE
        );
        MID_TIER_ZOMBIES = List.of(
                STNZombieEntities.BRUISER_ZOMBIE,
                STNZombieEntities.SHIELDED_ZOMBIE,
                STNZombieEntities.SPITTER_ZOMBIE,
                STNZombieEntities.HOWLER_ZOMBIE
        );
        HIGH_TIER_ZOMBIES = List.of(
                STNZombieEntities.PLAGUE_ZOMBIE,
                STNZombieEntities.LEECH_ZOMBIE,
                STNZombieEntities.LUMBERJACK_ZOMBIE,
                STNZombieEntities.BRUISER_ZOMBIE,
                STNZombieEntities.SHIELDED_ZOMBIE
        );
        ELITE_TIER_ZOMBIES = List.of(
                STNZombieEntities.ELECTRIC_ZOMBIE,
                STNZombieEntities.PLAGUE_ZOMBIE,
                STNZombieEntities.LEECH_ZOMBIE,
                STNZombieEntities.LUMBERJACK_ZOMBIE
        );
    }

    public static void init() {
        // Use ENTITY_LOAD with IdentityHashMap tracking for efficient lookups
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!WastelandConfig.enableVillageZombification) {
                return;
            }

            // Only convert in overworld
            if (world.getRegistryKey() != World.OVERWORLD) {
                return;
            }

            // Check if already processed
            if (processedEntities.contains(entity)) {
                return;
            }

            // Mark as processed
            processedEntities.add(entity);

            // Queue villagers for conversion
            if (entity instanceof VillagerEntity villager && !villager.hasCustomName()) {
                conversionQueue.add(new EntityConversion(villager, (ServerWorld) world, ConversionType.VILLAGER));
            }
            // Queue iron golems for conversion
            else if (entity instanceof IronGolemEntity golem && !golem.isPlayerCreated()) {
                conversionQueue.add(new EntityConversion(golem, (ServerWorld) world, ConversionType.GOLEM));
            }
        });

        // Process conversion queue on server tick
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!conversionQueue.isEmpty()) {
                List<EntityConversion> toConvert = new ArrayList<>(conversionQueue);
                conversionQueue.clear();

                // Fetch gamestage once per tick for all conversions
                IGamestageProvider provider = STNCore.getGamestageProvider();
                int gamestage = provider != null ? provider.getWorldGamestage() : 0;

                for (EntityConversion conversion : toConvert) {
                    if (!conversion.entity.isRemoved() && conversion.entity.isAlive()) {
                        if (conversion.type == ConversionType.VILLAGER) {
                            convertVillagerToZombie((VillagerEntity) conversion.entity, conversion.world, gamestage);
                        } else if (conversion.type == ConversionType.GOLEM) {
                            convertGolemToZombie((IronGolemEntity) conversion.entity, conversion.world, gamestage);
                        }
                    }
                }
            }
        });

        LOGGER.info("Village Zombification system initialized");
    }

    private enum ConversionType {
        VILLAGER,
        GOLEM
    }

    private record EntityConversion(Entity entity, ServerWorld world, ConversionType type) {
    }

    /**
     * Converts a villager to either a zombie villager or regular zombie.
     * Uses Minecraft's built-in convertTo() method for proper conversion.
     */
    private static void convertVillagerToZombie(VillagerEntity villager, ServerWorld world, int gamestage) {
        BlockPos pos = villager.getBlockPos();

        // Manually spawn zombie villager and remove villager
        ZombieVillagerEntity zombieVillager = EntityType.ZOMBIE_VILLAGER.create(world, SpawnReason.CONVERSION);
        if (zombieVillager != null) {
            zombieVillager.refreshPositionAndAngles(villager.getX(), villager.getY(), villager.getZ(), villager.getYaw(), villager.getPitch());
            zombieVillager.setVillagerData(villager.getVillagerData());
            zombieVillager.setBaby(villager.isBaby());
            world.spawnEntity(zombieVillager);
            villager.discard();
        }

        if (zombieVillager != null) {
            zombieVillager.setPersistent(); // Prevent despawning
            makeSunlightImmune(zombieVillager); // Immune to sunlight

            // Random chance to spawn extra zombies
            if (RANDOM.nextFloat() < WastelandConfig.extraZombieSpawnChance) {
                int extraCount = WastelandConfig.minExtraZombies +
                    RANDOM.nextInt(WastelandConfig.maxExtraZombies - WastelandConfig.minExtraZombies + 1);
                spawnExtraZombies(world, pos, extraCount, gamestage);
            }
        }
    }

    /**
     * Spawns extra zombies near a position to make villages more dangerous.
     */
    private static void spawnExtraZombies(ServerWorld world, BlockPos centerPos, int count, int gamestage) {
        for (int i = 0; i < count; i++) {
            // Spawn in a 3x3 area around the villager
            double offsetX = (RANDOM.nextDouble() - 0.5) * 3.0;
            double offsetZ = (RANDOM.nextDouble() - 0.5) * 3.0;
            BlockPos spawnPos = centerPos.add((int)offsetX, 0, (int)offsetZ);

            // Create zombie based on gamestage
            Entity zombie = createZombieByGamestage(gamestage, world);
            if (zombie != null) {
                zombie.refreshPositionAndAngles(
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    RANDOM.nextFloat() * 360.0F,
                    0.0F
                );
                if (zombie instanceof ZombieEntity zombieEntity) {
                    zombieEntity.initialize(world, world.getLocalDifficulty(spawnPos), SpawnReason.REINFORCEMENT, null);
                    zombieEntity.setPersistent();
                    makeSunlightImmune(zombieEntity);
                }
                world.spawnEntity(zombie);
            }
        }
    }

    /**
     * Converts an iron golem to a game stage-appropriate zombie.
     */
    private static void convertGolemToZombie(IronGolemEntity golem, ServerWorld world, int gamestage) {
        BlockPos pos = golem.getBlockPos();

        // Spawn appropriate zombie based on gamestage BEFORE removing golem
        Entity zombie = createZombieByGamestage(gamestage, world);
        if (zombie != null) {
            zombie.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
            if (zombie instanceof ZombieEntity zombieEntity) {
                zombieEntity.initialize(world, world.getLocalDifficulty(pos), SpawnReason.CONVERSION, null);
                zombieEntity.setPersistent(); // Prevent despawning
                makeSunlightImmune(zombieEntity); // Immune to sunlight
            }
            boolean spawned = world.spawnEntity(zombie);

            // Only remove golem if zombie spawned successfully
            if (spawned) {
                golem.discard();
            }
        }
    }

    /**
     * Creates a zombie entity based on the current gamestage.
     * Higher gamestages spawn more dangerous zombies.
     */
    private static Entity createZombieByGamestage(int gamestage, ServerWorld world) {
        // Initialize cached lists on first use
        initZombieLists();

        // Select tier based on gamestage (uses cached lists)
        List<EntityType<?>> possibleTypes;
        if (gamestage >= 80) {
            possibleTypes = ELITE_TIER_ZOMBIES;
        } else if (gamestage >= 60) {
            possibleTypes = HIGH_TIER_ZOMBIES;
        } else if (gamestage >= 40) {
            possibleTypes = MID_TIER_ZOMBIES;
        } else if (gamestage >= 20) {
            possibleTypes = LOW_TIER_ZOMBIES;
        } else {
            possibleTypes = BASIC_TIER_ZOMBIES;
        }

        // Select random type from available options
        EntityType<?> selectedType = possibleTypes.get(RANDOM.nextInt(possibleTypes.size()));
        return selectedType.create(world, SpawnReason.MOB_SUMMONED);
    }

    /**
     * Makes a zombie immune to sunlight by equipping an invisible helmet.
     * Uses a stone button which is invisible when worn but prevents burning.
     * Inspired by the NoBurn datapack method.
     */
    private static void makeSunlightImmune(ZombieEntity zombie) {
        // Give the zombie a stone button as helmet - invisible when worn!
        // Set drop chance to 0 so it doesn't drop when killed
        ItemStack helmet = new ItemStack(Items.STONE_BUTTON);
        zombie.equipStack(EquipmentSlot.HEAD, helmet);
        zombie.setEquipmentDropChance(EquipmentSlot.HEAD, 0.0f);
    }
}
