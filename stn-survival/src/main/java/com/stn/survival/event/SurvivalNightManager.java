package com.stn.survival.event;

import com.stn.core.api.ISurvivalNightProvider;
import com.stn.core.api.STNEvents;
import com.stn.survival.STNSurvival;
import com.stn.survival.config.STNSurvivalConfig;
import com.stn.survival.network.SurvivalNightSyncPayload;
import com.stn.survival.progression.GamestageManager;
import com.stn.survival.spawn.HordeMobRegistry;
import com.stn.survival.spawn.JockeySpawner;
import com.stn.survival.spawn.MobCategory;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the Survival Night event, including horde spawning and tracking.
 */
public class SurvivalNightManager implements ISurvivalNightProvider {
    private final MinecraftServer server;
    private boolean survivalNightActive = false;
    private long lastSurvivalNightDay = -1;
    private int mobsSpawnedThisNight = 0;
    private int targetHordeSize = 0;
    private int ticksSinceLastWave = 0;
    private final List<Entity> activeHordeEntities = new ArrayList<>();

    private GamestageManager gamestageManager;

    public SurvivalNightManager(MinecraftServer server) {
        this.server = server;
    }

    public void setGamestageManager(GamestageManager manager) {
        this.gamestageManager = manager;
    }

    public void tick() {
        ServerWorld overworld = server.getOverworld();
        if (overworld == null) return;

        long timeOfDay = overworld.getTimeOfDay() % 24000;
        long gameDay = overworld.getTimeOfDay() / 24000;

        if (!survivalNightActive && shouldStartSurvivalNight(gameDay, timeOfDay)) {
            startSurvivalNight(gameDay);
        }

        if (survivalNightActive && shouldEndSurvivalNight(timeOfDay)) {
            endSurvivalNight();
        }

        if (survivalNightActive) {
            tickSurvivalNight(overworld);
        }

        activeHordeEntities.removeIf(entity -> !entity.isAlive() || entity.isRemoved());
    }

    private boolean shouldStartSurvivalNight(long gameDay, long timeOfDay) {
        boolean isNight = timeOfDay >= 13000 && timeOfDay < 23000;
        boolean isSurvivalNightDay = STNSurvivalConfig.isSurvivalNightDay(gameDay);
        boolean notAlreadyTriggered = gameDay != lastSurvivalNightDay;

        return isNight && isSurvivalNightDay && notAlreadyTriggered;
    }

    private boolean shouldEndSurvivalNight(long timeOfDay) {
        return timeOfDay >= 23000 || timeOfDay < 13000;
    }

    private void startSurvivalNight(long gameDay) {
        survivalNightActive = true;
        lastSurvivalNightDay = gameDay;
        mobsSpawnedThisNight = 0;
        ticksSinceLastWave = 0;

        int playerCount = server.getPlayerManager().getPlayerList().size();

        float gamestageMultiplier = gamestageManager != null ? gamestageManager.getHordeSizeMultiplier() : 1.0f;

        targetHordeSize = (int) (STNSurvivalConfig.calculateHordeSize((int) gameDay, playerCount) * gamestageMultiplier);

        int worldGamestage = gamestageManager != null ? gamestageManager.getWorldGamestage() : 0;
        STNSurvival.LOGGER.info("SURVIVAL NIGHT RISING! Day {}, Gamestage {}, Horde size: {}",
            gameDay, worldGamestage, targetHordeSize);

        // Fire event
        ServerWorld overworld = server.getOverworld();
        if (overworld != null) {
            STNEvents.SURVIVAL_NIGHT_START.invoker().onSurvivalNightStart(overworld, gameDay);
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(
                Text.literal("THE HORDE IS COMING!")
                    .formatted(Formatting.DARK_RED, Formatting.BOLD),
                false
            );

            String subtitle = getSubtitleForGamestage(worldGamestage);
            player.sendMessage(
                Text.literal(subtitle).formatted(Formatting.RED),
                false
            );

            player.getWorld().playSound(
                null,
                player.getBlockPos(),
                SoundEvents.ENTITY_WITHER_SPAWN,
                SoundCategory.HOSTILE,
                1.0f,
                0.5f
            );

            ServerPlayNetworking.send(player, new SurvivalNightSyncPayload(true));
        }
    }

    private String getSubtitleForGamestage(int gamestage) {
        if (gamestage >= 100) return "The apocalypse is upon you...";
        if (gamestage >= 75) return "The horde hungers for blood...";
        if (gamestage >= 50) return "Darkness brings terrors...";
        if (gamestage >= 25) return "The dead walk tonight...";
        return "Survive until dawn...";
    }

    private void endSurvivalNight() {
        survivalNightActive = false;

        STNSurvival.LOGGER.info("Survival Night ended. Mobs spawned: {}/{}", mobsSpawnedThisNight, targetHordeSize);

        if (gamestageManager != null) {
            gamestageManager.onSurvivalNightSurvived();
        }

        // Fire event
        ServerWorld overworld = server.getOverworld();
        if (overworld != null) {
            STNEvents.SURVIVAL_NIGHT_END.invoker().onSurvivalNightEnd(overworld, mobsSpawnedThisNight, true);
        }

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.sendMessage(
                Text.literal("Dawn breaks. You survived the night.")
                    .formatted(Formatting.GOLD),
                false
            );

            ServerPlayNetworking.send(player, new SurvivalNightSyncPayload(false));
        }

        int removed = 0;
        for (Entity entity : activeHordeEntities) {
            if (entity.isAlive() && !entity.isRemoved()) {
                entity.discard();
                removed++;
            }
        }
        activeHordeEntities.clear();

        if (removed > 0) {
            STNSurvival.LOGGER.info("Cleaned up {} remaining horde entities", removed);
        }
    }

    private void tickSurvivalNight(ServerWorld world) {
        ticksSinceLastWave++;

        if (ticksSinceLastWave >= STNSurvivalConfig.SPAWN_WAVE_INTERVAL) {
            ticksSinceLastWave = 0;
            spawnHordeWave(world);
        }
    }

    private void spawnHordeWave(ServerWorld world) {
        if (mobsSpawnedThisNight >= targetHordeSize) {
            return;
        }

        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        if (players.isEmpty()) return;

        Random random = world.getRandom();
        int gamestage = gamestageManager != null ? gamestageManager.getWorldGamestage() : 0;

        int mobsToSpawn = Math.min(
            10 + random.nextInt(10),
            targetHordeSize - mobsSpawnedThisNight
        );

        for (int i = 0; i < mobsToSpawn; i++) {
            ServerPlayerEntity targetPlayer = players.get(random.nextInt(players.size()));

            if (HordeMobRegistry.shouldAttemptBossSpawn(random, gamestage)) {
                HordeMobRegistry.HordeMob bossMob = HordeMobRegistry.selectBossMob(random, gamestage);
                if (bossMob != null) {
                    BlockPos spawnPos = findSpawnPosition(world, targetPlayer.getBlockPos(), random, bossMob.category());
                    if (spawnPos != null) {
                        spawnBossMob(world, spawnPos, random, bossMob, targetPlayer);
                        continue;
                    }
                }
            }

            if (JockeySpawner.shouldSpawnJockey(random, gamestage)) {
                BlockPos spawnPos = findSpawnPosition(world, targetPlayer.getBlockPos(), random, MobCategory.GROUND);
                if (spawnPos != null) {
                    List<Entity> jockeyEntities = JockeySpawner.spawnJockey(world, spawnPos, random, gamestage, targetPlayer);
                    activeHordeEntities.addAll(jockeyEntities);
                    mobsSpawnedThisNight++;
                    continue;
                }
            }

            HordeMobRegistry.HordeMob selectedMob = HordeMobRegistry.selectRandomMob(random, gamestage);
            BlockPos spawnPos = findSpawnPosition(world, targetPlayer.getBlockPos(), random, selectedMob.category());

            if (spawnPos != null) {
                Entity spawned = spawnHordeMob(world, spawnPos, random, selectedMob, targetPlayer);
                if (spawned != null) {
                    activeHordeEntities.add(spawned);
                    mobsSpawnedThisNight++;
                }
            }
        }

        STNSurvival.LOGGER.debug("Spawned wave: {}/{} total entities", mobsSpawnedThisNight, targetHordeSize);
    }

    private BlockPos findSpawnPosition(ServerWorld world, BlockPos playerPos, Random random, MobCategory category) {
        return switch (category) {
            case AERIAL -> findAerialSpawnPosition(world, playerPos, random);
            case PHASING -> findPhasingSpawnPosition(playerPos, random);
            case AQUATIC -> findAquaticSpawnPosition(world, playerPos, random);
            default -> findGroundSpawnPosition(world, playerPos, random);
        };
    }

    private BlockPos findGroundSpawnPosition(ServerWorld world, BlockPos playerPos, Random random) {
        for (int attempts = 0; attempts < 10; attempts++) {
            int offsetX = STNSurvivalConfig.SPAWN_RADIUS_MIN +
                random.nextInt(STNSurvivalConfig.SPAWN_RADIUS_MAX - STNSurvivalConfig.SPAWN_RADIUS_MIN);
            int offsetZ = STNSurvivalConfig.SPAWN_RADIUS_MIN +
                random.nextInt(STNSurvivalConfig.SPAWN_RADIUS_MAX - STNSurvivalConfig.SPAWN_RADIUS_MIN);

            if (random.nextBoolean()) offsetX = -offsetX;
            if (random.nextBoolean()) offsetZ = -offsetZ;

            int x = playerPos.getX() + offsetX;
            int z = playerPos.getZ() + offsetZ;
            int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);

            BlockPos spawnPos = new BlockPos(x, y, z);

            if (world.getBlockState(spawnPos).isAir() &&
                world.getBlockState(spawnPos.up()).isAir()) {
                return spawnPos;
            }
        }
        return null;
    }

    private BlockPos findAerialSpawnPosition(ServerWorld world, BlockPos playerPos, Random random) {
        int offsetX = (random.nextInt(40) - 20);
        int offsetZ = (random.nextInt(40) - 20);
        int offsetY = 20 + random.nextInt(30);

        return playerPos.add(offsetX, offsetY, offsetZ);
    }

    private BlockPos findPhasingSpawnPosition(BlockPos playerPos, Random random) {
        int offsetX = random.nextInt(30) - 15;
        int offsetY = random.nextInt(10) - 5;
        int offsetZ = random.nextInt(30) - 15;

        return playerPos.add(offsetX, offsetY, offsetZ);
    }

    private BlockPos findAquaticSpawnPosition(ServerWorld world, BlockPos playerPos, Random random) {
        for (int attempts = 0; attempts < 10; attempts++) {
            int offsetX = random.nextInt(40) - 20;
            int offsetZ = random.nextInt(40) - 20;

            int x = playerPos.getX() + offsetX;
            int z = playerPos.getZ() + offsetZ;

            for (int y = playerPos.getY() + 10; y > playerPos.getY() - 20; y--) {
                BlockPos checkPos = new BlockPos(x, y, z);
                if (world.getBlockState(checkPos).getFluidState().isStill()) {
                    return checkPos;
                }
            }
        }
        return findGroundSpawnPosition(world, playerPos, random);
    }

    private Entity spawnHordeMob(ServerWorld world, BlockPos pos, Random random, HordeMobRegistry.HordeMob mobType, ServerPlayerEntity target) {
        MobEntity mob = (MobEntity) mobType.entityType().create(world);
        if (mob == null) return null;

        mob.refreshPositionAndAngles(pos, random.nextFloat() * 360f, 0);
        mob.setPersistent();

        if (target != null) {
            mob.setTarget(target);
        }

        handleSpecialMobSetup(mob, random);

        world.spawnEntity(mob);
        return mob;
    }

    private void spawnBossMob(ServerWorld world, BlockPos pos, Random random, HordeMobRegistry.HordeMob bossMob, ServerPlayerEntity target) {
        MobEntity boss = (MobEntity) bossMob.entityType().create(world);
        if (boss == null) return;

        boss.refreshPositionAndAngles(pos, random.nextFloat() * 360f, 0);
        boss.setPersistent();

        if (target != null) {
            boss.setTarget(target);
        }

        world.spawnEntity(boss);
        activeHordeEntities.add(boss);
        mobsSpawnedThisNight++;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            String bossName = bossMob.id().substring(0, 1).toUpperCase() + bossMob.id().substring(1);
            player.sendMessage(
                Text.literal("A " + bossName + " has joined the horde!")
                    .formatted(Formatting.DARK_RED, Formatting.BOLD),
                false
            );

            player.getWorld().playSound(
                null,
                player.getBlockPos(),
                SoundEvents.ENTITY_WITHER_AMBIENT,
                SoundCategory.HOSTILE,
                1.0f,
                0.5f
            );
        }

        STNSurvival.LOGGER.info("Spawned boss mob: {} at {}", bossMob.id(), pos);
    }

    private void handleSpecialMobSetup(MobEntity mob, Random random) {
        if (mob instanceof SlimeEntity slime) {
            slime.setSize(1 + random.nextInt(2), false);
        }

        if (mob instanceof PhantomEntity phantom) {
            phantom.setPhantomSize(random.nextInt(2));
        }
    }

    @Override
    public void forceStart() {
        if (!survivalNightActive) {
            ServerWorld overworld = server.getOverworld();
            if (overworld != null) {
                long gameDay = overworld.getTimeOfDay() / 24000;
                startSurvivalNight(gameDay);
            }
        }
    }

    @Override
    public void forceStop() {
        if (survivalNightActive) {
            endSurvivalNight();
        }
    }

    @Override
    public boolean isSurvivalNightActive() {
        return survivalNightActive;
    }

    @Override
    public int getDaysUntilSurvivalNight() {
        ServerWorld overworld = server.getOverworld();
        if (overworld == null) return -1;

        long currentDay = overworld.getTimeOfDay() / 24000;
        int interval = STNSurvivalConfig.SURVIVAL_NIGHT_INTERVAL;
        return interval - (int)(currentDay % interval);
    }

    @Override
    public int getSurvivalNightInterval() {
        return STNSurvivalConfig.SURVIVAL_NIGHT_INTERVAL;
    }

    @Override
    public int getMobsSpawned() {
        return mobsSpawnedThisNight;
    }

    @Override
    public int getTargetHordeSize() {
        return targetHordeSize;
    }

    public int getActiveHordeCount() {
        return activeHordeEntities.size();
    }
}
