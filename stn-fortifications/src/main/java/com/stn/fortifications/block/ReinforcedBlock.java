package com.stn.fortifications.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Reinforced block with enhanced durability against zombie attacks.
 * Some variants cannot be broken by zombies at all.
 */
public class ReinforcedBlock extends Block {

    private final boolean zombieProof;

    public ReinforcedBlock(Settings settings, boolean zombieProof) {
        super(settings);
        this.zombieProof = zombieProof;
    }

    /**
     * Returns whether zombies can break this block.
     * Used by the BreakBlockGoal AI to determine valid targets.
     */
    public boolean isZombieProof() {
        return zombieProof;
    }

    /**
     * Get the hardness multiplier for zombie breaking attempts.
     * Higher values make it harder for zombies to break.
     */
    public float getZombieBreakResistance() {
        if (zombieProof) {
            return Float.MAX_VALUE; // Effectively unbreakable
        }
        // Return a multiplier based on blast resistance
        return this.getBlastResistance() / 6.0f;
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        // Players can still break reinforced blocks normally
        return super.calcBlockBreakingDelta(state, player, world, pos);
    }
}
