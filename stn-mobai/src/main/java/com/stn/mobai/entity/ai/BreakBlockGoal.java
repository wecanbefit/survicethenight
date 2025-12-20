package com.stn.mobai.entity.ai;

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

        targetBlock = findBlockToBreak();
        return targetBlock != null;
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
        blockDamage += damagePerTick;

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

        // Check if block is broken
        if (blockDamage >= 1.0f) {
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
        }
    }

    private void spawnBreakingParticles(BlockState state) {
        World world = mob.getWorld();

        for (int i = 0; i < 3; i++) {
            double offsetX = mob.getRandom().nextGaussian() * 0.2;
            double offsetY = mob.getRandom().nextGaussian() * 0.2;
            double offsetZ = mob.getRandom().nextGaussian() * 0.2;

            world.addParticle(
                ParticleTypes.CRIT,
                targetBlock.getX() + 0.5 + offsetX,
                targetBlock.getY() + 0.5 + offsetY,
                targetBlock.getZ() + 0.5 + offsetZ,
                0, 0, 0
            );
        }
    }

    private BlockPos findBlockToBreak() {
        if (mob.getTarget() == null) {
            return null;
        }

        Vec3d targetPos = mob.getTarget().getPos();
        Vec3d mobPos = mob.getPos();
        Vec3d direction = targetPos.subtract(mobPos).normalize();

        // Check blocks in front of the mob
        for (int height = 0; height <= 2; height++) {
            for (Direction dir : Direction.Type.HORIZONTAL) {
                BlockPos checkPos = mob.getBlockPos().offset(dir).up(height);
                BlockState state = mob.getWorld().getBlockState(checkPos);

                if (isBreakableBlock(state, checkPos)) {
                    return checkPos;
                }
            }
        }

        // Check block directly in path
        BlockPos directPath = mob.getBlockPos().add(
            (int) Math.signum(direction.x),
            0,
            (int) Math.signum(direction.z)
        );

        for (int height = 0; height <= 2; height++) {
            BlockPos checkPos = directPath.up(height);
            BlockState state = mob.getWorld().getBlockState(checkPos);

            if (isBreakableBlock(state, checkPos)) {
                return checkPos;
            }
        }

        return null;
    }

    private boolean isBreakableBlock(BlockState state, BlockPos pos) {
        if (state.isAir()) {
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

        // Priority targets: doors, fences, glass
        if (state.isIn(BlockTags.DOORS) ||
            state.isIn(BlockTags.FENCES) ||
            state.isIn(BlockTags.FENCE_GATES) ||
            block == Blocks.GLASS ||
            block == Blocks.GLASS_PANE) {
            return true;
        }

        return state.isSolidBlock(mob.getWorld(), pos);
    }
}
