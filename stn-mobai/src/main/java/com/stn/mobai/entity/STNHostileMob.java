package com.stn.mobai.entity;

import com.stn.mobai.entity.ai.BreakBlockGoal;
import com.stn.mobai.entity.ai.MobSenseGoal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Base class for STN hostile mobs with enhanced AI.
 * Provides block breaking and sensory detection capabilities.
 * Extend this for zombies, skeletons, creepers, or any hostile mob.
 */
public abstract class STNHostileMob extends HostileEntity implements ISensoryMob, IBlockBreaker {

    private boolean isBreakingBlock = false;

    // Required gamestage to spawn this mob type
    protected int requiredGamestage = 0;

    protected STNHostileMob(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        // Add block breaking and sense detection
        this.goalSelector.add(2, new BreakBlockGoal(this, getBlockBreakSpeed(), getBaseBreakTime()));
        this.goalSelector.add(3, new MobSenseGoal(this));
    }

    /**
     * Get the minimum gamestage required for this mob to spawn.
     */
    public int getRequiredGamestage() {
        return requiredGamestage;
    }

    public static DefaultAttributeContainer.Builder createSTNMobAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.FOLLOW_RANGE, 48.0);
    }

    // ISensoryMob defaults - override in subclasses as needed

    @Override
    public double getSoundDetectionRange() {
        return 32.0;
    }

    @Override
    public double getSmellRange() {
        return 64.0;
    }

    @Override
    public double getVillageDetectionRange() {
        return 96.0;
    }

    // IBlockBreaker defaults - override in subclasses as needed

    @Override
    public double getBlockBreakSpeed() {
        return 1.0;
    }

    @Override
    public int getBaseBreakTime() {
        return 40;
    }

    @Override
    public boolean canBreakBlock(BlockState state, BlockPos pos) {
        return true;
    }

    // BlockBreakAnimatable implementation

    @Override
    public void triggerBlockBreakSwing() {
        this.swingHand(this.getActiveHand());
    }

    @Override
    public boolean isBreakingBlock() {
        return this.isBreakingBlock;
    }

    @Override
    public void setBreakingBlock(boolean breaking) {
        this.isBreakingBlock = breaking;
    }
}
