package com.stn.mobai.entity.ai;

import com.stn.core.STNCore;
import com.stn.fortifications.durability.BlockDurabilityManager;
import com.stn.fortifications.network.FortificationsNetworking;
import com.stn.mobai.config.STNMobAIConfig;
import com.stn.mobai.entity.BlockBreakAnimatable;
import com.stn.mobai.entity.IBlockBreaker;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

/**
 * AI Goal for mobs to break blocks when pathfinding is blocked.
 * Works with any MobEntity that implements IBlockBreaker.
 * Inspired by 7 Days to Die's mob block destruction mechanics.
 */
public class BreakBlockGoal extends Goal {
    private final MobEntity mob;
    private final double breakSpeedMultiplier;
    private final int baseBreakTime;
    private final int staggerOffset; // Unique offset per mob

    private BlockPos targetBlock;
    private float blockDamage;
    private int breakProgress;
    private int lastBreakProgress = -1;
    private int canStartCooldown;

    private static final int CAN_START_COOLDOWN = 20; // Check every 1 second (was 0.5)
    private static final int STAGGER_RANGE = 10; // Spread checks over 0.5 second

    // Blocks that mobs should not break
    private static final Set<Block> UNBREAKABLE_BLOCKS = new HashSet<>();

    static {
        UNBREAKABLE_BLOCKS.add(Blocks.BEDROCK);
        UNBREAKABLE_BLOCKS.add(Blocks.END_PORTAL_FRAME);
        UNBREAKABLE_BLOCKS.add(Blocks.END_PORTAL);
        UNBREAKABLE_BLOCKS.add(Blocks.NETHER_PORTAL);
        UNBREAKABLE_BLOCKS.add(Blocks.COMMAND_BLOCK);
        UNBREAKABLE_BLOCKS.add(Blocks.CHAIN_COMMAND_BLOCK);
        UNBREAKABLE_BLOCKS.add(Blocks.REPEATING_COMMAND_BLOCK);
        UNBREAKABLE_BLOCKS.add(Blocks.BARRIER);
        UNBREAKABLE_BLOCKS.add(Blocks.STRUCTURE_BLOCK);
        UNBREAKABLE_BLOCKS.add(Blocks.STRUCTURE_VOID);
        UNBREAKABLE_BLOCKS.add(Blocks.JIGSAW);
    }

    public BreakBlockGoal(MobEntity mob, double breakSpeedMultiplier, int baseBreakTime) {
        this.mob = mob;
        this.breakSpeedMultiplier = breakSpeedMultiplier;
        this.baseBreakTime = baseBreakTime;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        // Stagger based on entity ID to spread load across ticks
        this.staggerOffset = Math.abs(mob.getId() % STAGGER_RANGE);
        this.canStartCooldown = staggerOffset; // Initial stagger
    }

    @Override
    public boolean canStart() {
        // Throttle checks
        if (canStartCooldown > 0) {
            canStartCooldown--;
            return false;
        }
        canStartCooldown = CAN_START_COOLDOWN;

        if (!STNMobAIConfig.MOBS_BREAK_BLOCKS) {
            return false;
        }

        if (mob.getTarget() == null) {
            return false;
        }

        // Only break blocks if within 5 blocks of target
        double distanceToTarget = mob.distanceTo(mob.getTarget());
        if (distanceToTarget > 5.0) {
            return false;
        }

        // Only break blocks if path to target is blocked
        if (!isPathBlocked()) {
            return false;
        }

        targetBlock = findBlockToBreak();
        return targetBlock != null;
    }

    /**
     * Check if the mob's path to its target is blocked.
     * Returns true if the mob cannot navigate to the target.
     */
    private boolean isPathBlocked() {
        if (mob.getTarget() == null) {
            return false;
        }

        // If mob can see target and is close, path isn't truly blocked
        if (mob.canSee(mob.getTarget())) {
            double distance = mob.squaredDistanceTo(mob.getTarget());
            if (distance < 9.0) { // Within 3 blocks and visible
                return false;
            }
        }

        // Check if navigation has a valid path
        var nav = mob.getNavigation();
        if (nav.isIdle()) {
            // No path at all - try to find one
            boolean canPath = nav.startMovingTo(mob.getTarget(), 1.0);
            if (!canPath) {
                return true; // Can't path to target
            }
            // Path was found, check if it's complete
            var path = nav.getCurrentPath();
            if (path != null && !path.reachesTarget()) {
                return true; // Path doesn't reach target
            }
        } else {
            // Already navigating - check if path is stuck
            var path = nav.getCurrentPath();
            if (path != null && !path.reachesTarget()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean shouldContinue() {
        if (targetBlock == null) {
            return false;
        }

        BlockState state = mob.getWorld().getBlockState(targetBlock);
        return !state.isAir() && !UNBREAKABLE_BLOCKS.contains(state.getBlock());
    }

    @Override
    public void start() {
        blockDamage = 0;
        breakProgress = 0;
        lastBreakProgress = -1;

        if (mob instanceof BlockBreakAnimatable animatable) {
            animatable.setBreakingBlock(true);
        }
    }

    @Override
    public void stop() {
        if (targetBlock != null) {
            mob.getWorld().setBlockBreakingInfo(mob.getId(), targetBlock, -1);
        }
        targetBlock = null;
        blockDamage = 0;
        breakProgress = 0;

        if (mob instanceof BlockBreakAnimatable animatable) {
            animatable.setBreakingBlock(false);
        }
    }

    @Override
    public void tick() {
        if (targetBlock == null) {
            return;
        }

        World world = mob.getWorld();
        BlockState state = world.getBlockState(targetBlock);

        if (state.isAir()) {
            targetBlock = null;
            return;
        }

        // Look at the block
        mob.getLookControl().lookAt(
            targetBlock.getX() + 0.5,
            targetBlock.getY() + 0.5,
            targetBlock.getZ() + 0.5
        );

        // Calculate break speed
        float hardness = state.getHardness(world, targetBlock);
        if (hardness < 0) {
            targetBlock = null;
            return;
        }

        float damagePerTick = (float) (breakSpeedMultiplier * STNMobAIConfig.BLOCK_BREAK_SPEED_MULTIPLIER);

        // Apply material-specific multipliers if mob implements IBlockBreaker
        if (mob instanceof IBlockBreaker breaker) {
            if (state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.PLANKS) ||
                state.isIn(BlockTags.WOODEN_DOORS) || state.isIn(BlockTags.WOODEN_FENCES)) {
                damagePerTick *= breaker.getWoodBreakMultiplier();
            } else if (state.isIn(BlockTags.BASE_STONE_OVERWORLD) ||
                       state.isIn(BlockTags.STONE_BRICKS)) {
                damagePerTick *= breaker.getStoneBreakMultiplier();
            }
        }

        if (hardness > 0) {
            damagePerTick /= (hardness * baseBreakTime);
        } else {
            damagePerTick = 1.0f;
        }

        // Server-side: use durability system as source of truth
        if (world instanceof ServerWorld serverWorld) {
            BlockDurabilityManager manager = BlockDurabilityManager.get(serverWorld);

            // Track block if not already tracked
            if (!manager.isTracked(world, targetBlock)) {
                manager.registerBlock(targetBlock, state);
            }

            // Deal damage to durability
            int durabilityDamage = Math.max(1, (int) (damagePerTick * 50));
            boolean shouldBreak = manager.damageBlock(world, targetBlock, durabilityDamage);

            // Sync blockDamage from durability (repairs will be reflected here)
            float durabilityPercent = manager.getDurabilityPercent(world, targetBlock);
            blockDamage = 1.0f - durabilityPercent;

            // Broadcast durability update to nearby players
            int current = manager.getDurability(world, targetBlock);
            int max = manager.getMaxDurabilityAt(world, targetBlock);
            FortificationsNetworking.broadcastDurabilityUpdate(serverWorld, targetBlock, current, max);

            if (shouldBreak) {
                // Durability reached 0, break the block
                world.breakBlock(targetBlock, true, mob);
                world.setBlockBreakingInfo(mob.getId(), targetBlock, -1);

                world.playSound(
                    null,
                    targetBlock,
                    state.getSoundGroup().getBreakSound(),
                    SoundCategory.HOSTILE,
                    1.0f,
                    0.8f
                );

                targetBlock = null;
                blockDamage = 0;
                return;
            }
        }

        // Update visual break progress
        int newProgress = (int) (blockDamage * 10.0f);
        if (newProgress != lastBreakProgress) {
            world.setBlockBreakingInfo(mob.getId(), targetBlock, Math.min(newProgress, 9));
            lastBreakProgress = newProgress;

            if (mob instanceof BlockBreakAnimatable animatable) {
                animatable.triggerBlockBreakSwing();
            }
            mob.swingHand(mob.getActiveHand());

            // Play breaking sound
            if (mob.getRandom().nextInt(5) == 0) {
                world.playSound(
                    null,
                    targetBlock,
                    state.getSoundGroup().getHitSound(),
                    SoundCategory.HOSTILE,
                    0.5f,
                    0.8f + mob.getRandom().nextFloat() * 0.4f
                );
            }

            spawnBreakingParticles(state);
        }
    }

    private void spawnBreakingParticles(BlockState state) {
        World world = mob.getWorld();

        // Use server-side particle spawning
        if (world instanceof ServerWorld serverWorld) {
            for (int i = 0; i < 3; i++) {
                double offsetX = mob.getRandom().nextGaussian() * 0.2;
                double offsetY = mob.getRandom().nextGaussian() * 0.2;
                double offsetZ = mob.getRandom().nextGaussian() * 0.2;

                serverWorld.spawnParticles(
                    ParticleTypes.CRIT,
                    targetBlock.getX() + 0.5 + offsetX,
                    targetBlock.getY() + 0.5 + offsetY,
                    targetBlock.getZ() + 0.5 + offsetZ,
                    1, 0, 0, 0, 0
                );
            }
        }
    }

    private BlockPos findBlockToBreak() {
        if (mob.getTarget() == null) {
            return null;
        }

        Vec3d targetPos = mob.getTarget().getPos();
        Vec3d mobPos = mob.getPos();
        Vec3d direction = targetPos.subtract(mobPos).normalize();

        // Calculate the primary direction toward target
        int dx = (int) Math.signum(direction.x);
        int dz = (int) Math.signum(direction.z);

        // Priority 1: Check for doors in the direct path to target
        BlockPos doorInPath = findDoorInPath(dx, dz);
        if (doorInPath != null) {
            return doorInPath;
        }

        // Priority 2: If there's a door within 10 blocks, don't break other blocks
        // Let the mob keep trying to path to the door instead
        if (findNearbyDoor() != null) {
            return null;
        }

        // Priority 3: No doors nearby - check for breakable blocks in path
        BlockPos directPath = mob.getBlockPos().add(dx, 0, dz);
        for (int height = 0; height <= 2; height++) {
            BlockPos checkPos = directPath.up(height);
            BlockState state = mob.getWorld().getBlockState(checkPos);

            if (isBreakableBlock(state, checkPos)) {
                return checkPos;
            }
        }

        // Priority 4: If diagonal, check the component directions
        if (dx != 0 && dz != 0) {
            // Check X direction
            BlockPos xPath = mob.getBlockPos().add(dx, 0, 0);
            for (int height = 0; height <= 2; height++) {
                BlockPos checkPos = xPath.up(height);
                BlockState state = mob.getWorld().getBlockState(checkPos);
                if (isBreakableBlock(state, checkPos)) {
                    return checkPos;
                }
            }

            // Check Z direction
            BlockPos zPath = mob.getBlockPos().add(0, 0, dz);
            for (int height = 0; height <= 2; height++) {
                BlockPos checkPos = zPath.up(height);
                BlockState state = mob.getWorld().getBlockState(checkPos);
                if (isBreakableBlock(state, checkPos)) {
                    return checkPos;
                }
            }
        }

        return null;
    }

    /**
     * Search for doors in the path toward the target.
     * Doors are prioritized because mobs should path through doorways.
     */
    private BlockPos findDoorInPath(int dx, int dz) {
        // Search up to 3 blocks ahead for doors
        for (int dist = 1; dist <= 3; dist++) {
            BlockPos checkBase = mob.getBlockPos().add(dx * dist, 0, dz * dist);

            for (int height = 0; height <= 1; height++) {
                BlockPos checkPos = checkBase.up(height);
                BlockState state = mob.getWorld().getBlockState(checkPos);

                if (state.isIn(BlockTags.DOORS) || state.isIn(BlockTags.FENCE_GATES)) {
                    return checkPos;
                }
            }

            // Also check component directions for diagonal movement
            if (dx != 0 && dz != 0) {
                BlockPos xCheck = mob.getBlockPos().add(dx * dist, 0, 0);
                BlockPos zCheck = mob.getBlockPos().add(0, 0, dz * dist);

                for (BlockPos base : new BlockPos[]{xCheck, zCheck}) {
                    for (int height = 0; height <= 1; height++) {
                        BlockPos checkPos = base.up(height);
                        BlockState state = mob.getWorld().getBlockState(checkPos);

                        if (state.isIn(BlockTags.DOORS) || state.isIn(BlockTags.FENCE_GATES)) {
                            return checkPos;
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Search for any door or fence gate within 10 blocks.
     * If a door exists nearby, mobs should try to path to it rather than breaking walls.
     */
    private BlockPos findNearbyDoor() {
        BlockPos mobPos = mob.getBlockPos();
        int searchRadius = 10;

        // Search in expanding squares for efficiency (check closer doors first)
        for (int radius = 1; radius <= searchRadius; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    // Only check the perimeter of each radius level
                    if (Math.abs(x) != radius && Math.abs(z) != radius) {
                        continue;
                    }

                    for (int y = -1; y <= 2; y++) {
                        BlockPos checkPos = mobPos.add(x, y, z);
                        BlockState state = mob.getWorld().getBlockState(checkPos);

                        if (state.isIn(BlockTags.DOORS) || state.isIn(BlockTags.FENCE_GATES)) {
                            return checkPos;
                        }
                    }
                }
            }
        }

        return null;
    }

    private boolean isBreakableBlock(BlockState state, BlockPos pos) {
        if (state.isAir()) {
            return false;
        }

        // Check if block is protected by trader structures
        if (STNCore.isBlockProtected(mob.getWorld(), pos)) {
            return false;
        }

        Block block = state.getBlock();

        if (UNBREAKABLE_BLOCKS.contains(block)) {
            return false;
        }

        // Check if mob-specific block breaking restrictions apply
        if (mob instanceof IBlockBreaker breaker) {
            if (!breaker.canBreakBlock(state, pos)) {
                return false;
            }
        }

        float hardness = state.getHardness(mob.getWorld(), pos);
        if (hardness < 0) {
            return false;
        }

        // Priority defensive structures: doors, fences, glass
        if (state.isIn(BlockTags.DOORS) ||
            state.isIn(BlockTags.FENCES) ||
            state.isIn(BlockTags.FENCE_GATES) ||
            block == Blocks.GLASS ||
            block == Blocks.GLASS_PANE ||
            block == Blocks.IRON_BARS) {
            return true;
        }

        // Crafted wood blocks
        if (state.isIn(BlockTags.PLANKS) ||
            state.isIn(BlockTags.WOODEN_SLABS) ||
            state.isIn(BlockTags.WOODEN_STAIRS) ||
            state.isIn(BlockTags.WOODEN_TRAPDOORS)) {
            return true;
        }

        // Crafted stone blocks (processed from raw stone)
        if (block == Blocks.COBBLESTONE ||
            block == Blocks.COBBLESTONE_STAIRS ||
            block == Blocks.COBBLESTONE_SLAB ||
            block == Blocks.COBBLESTONE_WALL ||
            block == Blocks.MOSSY_COBBLESTONE ||
            block == Blocks.MOSSY_COBBLESTONE_STAIRS ||
            block == Blocks.MOSSY_COBBLESTONE_SLAB ||
            block == Blocks.MOSSY_COBBLESTONE_WALL) {
            return true;
        }

        // Stone bricks (crafted)
        if (state.isIn(BlockTags.STONE_BRICKS)) {
            return true;
        }

        // Bricks (crafted from clay)
        if (block == Blocks.BRICKS ||
            block == Blocks.BRICK_STAIRS ||
            block == Blocks.BRICK_SLAB ||
            block == Blocks.BRICK_WALL) {
            return true;
        }

        // Sandstone (crafted)
        if (block == Blocks.SANDSTONE ||
            block == Blocks.SANDSTONE_STAIRS ||
            block == Blocks.SANDSTONE_SLAB ||
            block == Blocks.SANDSTONE_WALL ||
            block == Blocks.SMOOTH_SANDSTONE ||
            block == Blocks.SMOOTH_SANDSTONE_STAIRS ||
            block == Blocks.SMOOTH_SANDSTONE_SLAB ||
            block == Blocks.CUT_SANDSTONE ||
            block == Blocks.CUT_SANDSTONE_SLAB ||
            block == Blocks.RED_SANDSTONE ||
            block == Blocks.RED_SANDSTONE_STAIRS ||
            block == Blocks.RED_SANDSTONE_SLAB ||
            block == Blocks.RED_SANDSTONE_WALL ||
            block == Blocks.SMOOTH_RED_SANDSTONE ||
            block == Blocks.SMOOTH_RED_SANDSTONE_STAIRS ||
            block == Blocks.SMOOTH_RED_SANDSTONE_SLAB ||
            block == Blocks.CUT_RED_SANDSTONE ||
            block == Blocks.CUT_RED_SANDSTONE_SLAB) {
            return true;
        }

        // Other common building materials
        if (state.isIn(BlockTags.WOOL) ||
            state.isIn(BlockTags.TERRACOTTA) ||
            block == Blocks.BOOKSHELF ||
            block == Blocks.CRAFTING_TABLE ||
            block == Blocks.FURNACE ||
            block == Blocks.CHEST ||
            block == Blocks.BARREL) {
            return true;
        }

        // Deepslate variants (crafted)
        if (block == Blocks.COBBLED_DEEPSLATE ||
            block == Blocks.COBBLED_DEEPSLATE_STAIRS ||
            block == Blocks.COBBLED_DEEPSLATE_SLAB ||
            block == Blocks.COBBLED_DEEPSLATE_WALL ||
            block == Blocks.POLISHED_DEEPSLATE ||
            block == Blocks.POLISHED_DEEPSLATE_STAIRS ||
            block == Blocks.POLISHED_DEEPSLATE_SLAB ||
            block == Blocks.POLISHED_DEEPSLATE_WALL ||
            block == Blocks.DEEPSLATE_BRICKS ||
            block == Blocks.DEEPSLATE_BRICK_STAIRS ||
            block == Blocks.DEEPSLATE_BRICK_SLAB ||
            block == Blocks.DEEPSLATE_BRICK_WALL ||
            block == Blocks.DEEPSLATE_TILES ||
            block == Blocks.DEEPSLATE_TILE_STAIRS ||
            block == Blocks.DEEPSLATE_TILE_SLAB ||
            block == Blocks.DEEPSLATE_TILE_WALL) {
            return true;
        }

        // Don't break natural terrain (dirt, stone, logs, etc.)
        return false;
    }
}
