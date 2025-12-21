package com.stn.mobai.entity.ai;

import com.stn.mobai.entity.ISensoryMob;
import com.stn.mobai.entity.ai.sense.HeatDetection;
import com.stn.mobai.entity.ai.sense.LightDetection;
import com.stn.mobai.entity.ai.sense.SenseManager;
import com.stn.mobai.entity.ai.sense.SoundEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * AI Goal that allows mobs to detect targets through multiple senses.
 * Optimized for multiplayer with staggered processing and caching.
 */
public class MobSenseGoal extends Goal {
    private final MobEntity mob;
    private final ISensoryMob sensoryMob;
    private final int staggerOffset; // Unique offset per mob for staggered processing

    // Current sense target info
    private BlockPos targetPosition;
    private LivingEntity targetEntity;
    private SenseType activeSense;
    private int ticksUntilNextCheck;
    private int canStartCooldown;
    private boolean lastCanStartResult;

    // Check intervals - increased for multiplayer performance
    private static final int SENSE_CHECK_INTERVAL = 60; // 3 seconds (was 2)
    private static final int CAN_START_COOLDOWN = 40; // 2 seconds (was 1)
    private static final int STAGGER_RANGE = 20; // Spread checks over 1 second

    // Callback for checking survival night
    private static SurvivalNightChecker survivalNightChecker = () -> false;

    public MobSenseGoal(MobEntity mob) {
        this.mob = mob;
        this.sensoryMob = (mob instanceof ISensoryMob) ? (ISensoryMob) mob : new DefaultSensoryMob();
        this.setControls(EnumSet.of(Control.TARGET));
        // Stagger based on entity ID to spread load across ticks
        this.staggerOffset = Math.abs(mob.getId() % STAGGER_RANGE);
        this.canStartCooldown = staggerOffset; // Initial stagger
    }

    public static void setSurvivalNightChecker(SurvivalNightChecker checker) {
        survivalNightChecker = checker;
    }

    @Override
    public boolean canStart() {
        // Throttle expensive sense checks with staggered timing
        if (canStartCooldown > 0) {
            canStartCooldown--;
            return lastCanStartResult;
        }
        canStartCooldown = CAN_START_COOLDOWN;

        // Quick exit if already has visible target
        if (mob.getTarget() != null && mob.canSee(mob.getTarget())) {
            lastCanStartResult = false;
            return false;
        }

        lastCanStartResult = evaluateSenses();
        return lastCanStartResult;
    }

    @Override
    public boolean shouldContinue() {
        if (mob.getTarget() != null && mob.canSee(mob.getTarget())) {
            return false;
        }

        if (targetPosition != null) {
            double distance = mob.getBlockPos().getSquaredDistance(targetPosition);
            if (distance < 4.0) {
                return false;
            }
        }

        return targetPosition != null || targetEntity != null;
    }

    @Override
    public void start() {
        ticksUntilNextCheck = SENSE_CHECK_INTERVAL + staggerOffset;
    }

    @Override
    public void stop() {
        targetPosition = null;
        targetEntity = null;
        activeSense = null;
    }

    @Override
    public void tick() {
        ticksUntilNextCheck--;

        if (ticksUntilNextCheck <= 0) {
            ticksUntilNextCheck = SENSE_CHECK_INTERVAL;
            evaluateSenses();
        }

        if (targetEntity != null && targetEntity.isAlive()) {
            mob.setTarget(targetEntity);
        } else if (targetPosition != null) {
            mob.getNavigation().startMovingTo(
                targetPosition.getX() + 0.5,
                targetPosition.getY(),
                targetPosition.getZ() + 0.5,
                1.0
            );
        }
    }

    private boolean evaluateSenses() {
        if (!(mob.getWorld() instanceof ServerWorld serverWorld)) {
            return false;
        }

        SenseTarget bestTarget = null;
        double bestScore = 0;

        // Check for players FIRST - highest priority, works all the time
        SenseTarget playerTarget = evaluatePlayer(serverWorld);
        if (playerTarget != null && playerTarget.score > bestScore) {
            bestScore = playerTarget.score;
            bestTarget = playerTarget;
        }

        // Check sound - uses existing sound list
        SenseTarget soundTarget = evaluateSound(serverWorld);
        if (soundTarget != null && soundTarget.score > bestScore) {
            bestScore = soundTarget.score;
            bestTarget = soundTarget;
        }

        // Check smell (only during survival night) - extends detection through walls
        if (sensoryMob.canSmell() && survivalNightChecker.isSurvivalNight()) {
            SenseTarget smellTarget = evaluateSmell(serverWorld);
            if (smellTarget != null && smellTarget.score > bestScore) {
                bestScore = smellTarget.score;
                bestTarget = smellTarget;
            }
        }

        // Light and heat use caching now, so they're much cheaper
        if (sensoryMob.canDetectLight()) {
            SenseTarget lightTarget = evaluateLight(serverWorld);
            if (lightTarget != null && lightTarget.score > bestScore) {
                bestScore = lightTarget.score;
                bestTarget = lightTarget;
            }
        }

        if (sensoryMob.canDetectHeat()) {
            SenseTarget heatTarget = evaluateHeat(serverWorld);
            if (heatTarget != null && heatTarget.score > bestScore) {
                bestScore = heatTarget.score;
                bestTarget = heatTarget;
            }
        }

        // Village targeting - uses POI storage (already indexed)
        if (sensoryMob.canTargetVillages()) {
            SenseTarget villageTarget = evaluateVillage(serverWorld);
            if (villageTarget != null && villageTarget.score > bestScore) {
                bestTarget = villageTarget;
            }
        }

        if (bestTarget != null) {
            this.targetPosition = bestTarget.position;
            this.targetEntity = bestTarget.entity;
            this.activeSense = bestTarget.sense;
            return true;
        }

        return false;
    }

    private SenseTarget evaluatePlayer(ServerWorld world) {
        BlockPos mobPos = mob.getBlockPos();
        double playerRange = sensoryMob.getPlayerDetectionRange();
        Box searchBox = new Box(mobPos).expand(playerRange);

        SenseTarget bestTarget = null;
        double bestScore = 0;

        List<PlayerEntity> players = world.getEntitiesByClass(
            PlayerEntity.class,
            searchBox,
            player -> player.isAlive() && !player.isSpectator() && !player.isCreative()
        );

        for (PlayerEntity player : players) {
            double distance = mobPos.getSquaredDistance(player.getBlockPos());
            if (distance > playerRange * playerRange) continue;

            double normalizedDistance = Math.sqrt(distance) / playerRange;
            // High base score for players - they are the primary target
            double score = (1.0 - normalizedDistance) * 150.0 * sensoryMob.getPlayerWeight();

            if (score > bestScore) {
                bestScore = score;
                bestTarget = new SenseTarget(player.getBlockPos(), player, SenseType.PLAYER, score);
            }
        }

        return bestTarget;
    }

    private SenseTarget evaluateSound(ServerWorld world) {
        BlockPos mobPos = mob.getBlockPos();
        SenseManager senseManager = SenseManager.getInstance();
        double soundRange = sensoryMob.getSoundDetectionRange();

        Optional<SoundEvent> loudestSound = senseManager.getLoudestSound(world, mobPos, soundRange);

        if (loudestSound.isPresent()) {
            SoundEvent sound = loudestSound.get();
            double score = senseManager.calculateSoundScore(world, mobPos, sound, soundRange);
            score *= sensoryMob.getSoundWeight();

            if (score > 0) {
                return new SenseTarget(sound.getPosition(), null, SenseType.SOUND, score);
            }
        }

        return null;
    }

    private SenseTarget evaluateSmell(ServerWorld world) {
        BlockPos mobPos = mob.getBlockPos();
        double smellRange = sensoryMob.getSmellRange();
        Box searchBox = new Box(mobPos).expand(smellRange);

        SenseTarget bestTarget = null;
        double bestScore = 0;

        // Combined search for living entities - more efficient than two separate queries
        List<LivingEntity> entities = world.getEntitiesByClass(
            LivingEntity.class,
            searchBox,
            entity -> (entity instanceof PlayerEntity player &&
                       player.isAlive() && !player.isSpectator() && !player.isCreative()) ||
                      (entity instanceof VillagerEntity villager && villager.isAlive())
        );

        for (LivingEntity entity : entities) {
            double distance = mobPos.getSquaredDistance(entity.getBlockPos());
            if (distance > smellRange * smellRange) continue;

            double normalizedDistance = Math.sqrt(distance) / smellRange;
            // Players worth more than villagers
            double baseScore = (entity instanceof PlayerEntity) ? 100.0 : 75.0;
            double score = (1.0 - normalizedDistance) * baseScore * sensoryMob.getSmellWeight();

            if (score > bestScore) {
                bestScore = score;
                bestTarget = new SenseTarget(entity.getBlockPos(), entity, SenseType.SMELL, score);
            }
        }

        return bestTarget;
    }

    private SenseTarget evaluateLight(ServerWorld world) {
        BlockPos mobPos = mob.getBlockPos();
        double lightRange = sensoryMob.getLightDetectionRange();

        LightDetection.LightSource brightestLight = LightDetection.findBrightestLight(world, mobPos, lightRange);

        if (brightestLight != null) {
            double score = LightDetection.calculateLightScore(mobPos, brightestLight, lightRange, sensoryMob.getLightWeight());
            if (score > 0) {
                return new SenseTarget(brightestLight.position, null, SenseType.LIGHT, score);
            }
        }

        return null;
    }

    private SenseTarget evaluateHeat(ServerWorld world) {
        BlockPos mobPos = mob.getBlockPos();
        double heatRange = sensoryMob.getHeatDetectionRange();

        HeatDetection.HeatSource hottestSource = HeatDetection.findHottestSource(world, mobPos, heatRange);

        if (hottestSource != null) {
            double score = HeatDetection.calculateHeatScore(mobPos, hottestSource, heatRange, sensoryMob.getHeatWeight());
            if (score > 0) {
                return new SenseTarget(hottestSource.position, null, SenseType.HEAT, score);
            }
        }

        return null;
    }

    private SenseTarget evaluateVillage(ServerWorld world) {
        BlockPos mobPos = mob.getBlockPos();
        double villageRange = sensoryMob.getVillageDetectionRange();
        PointOfInterestStorage poiStorage = world.getPointOfInterestStorage();

        // Just target the POI directly - no expensive block scanning
        Optional<BlockPos> nearestVillagePoi = poiStorage.getNearestPosition(
            type -> type.matchesKey(PointOfInterestTypes.HOME) ||
                    type.matchesKey(PointOfInterestTypes.MEETING),
            mobPos,
            (int) villageRange,
            PointOfInterestStorage.OccupationStatus.ANY
        );

        if (nearestVillagePoi.isEmpty()) {
            return null;
        }

        BlockPos poiPos = nearestVillagePoi.get();
        double distance = Math.sqrt(mobPos.getSquaredDistance(poiPos));
        double score = (1.0 - (distance / villageRange)) * 50.0 * sensoryMob.getVillageWeight();

        // Target the POI position directly instead of expensive door/gate search
        return new SenseTarget(poiPos, null, SenseType.VILLAGE, score);
    }

    public SenseType getActiveSense() {
        return activeSense;
    }

    public BlockPos getTargetPosition() {
        return targetPosition;
    }

    public enum SenseType {
        PLAYER,
        SOUND,
        SMELL,
        LIGHT,
        HEAT,
        VILLAGE
    }

    @FunctionalInterface
    public interface SurvivalNightChecker {
        boolean isSurvivalNight();
    }

    private static class DefaultSensoryMob implements ISensoryMob {
        // Uses all default values from the interface
    }

    private static class SenseTarget {
        final BlockPos position;
        final LivingEntity entity;
        final SenseType sense;
        final double score;

        SenseTarget(BlockPos position, LivingEntity entity, SenseType sense, double score) {
            this.position = position;
            this.entity = entity;
            this.sense = sense;
            this.score = score;
        }
    }
}
