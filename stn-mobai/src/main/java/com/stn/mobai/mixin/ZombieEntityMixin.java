package com.stn.mobai.mixin;

import com.stn.mobai.entity.BlockBreakAnimatable;
import com.stn.mobai.entity.IBlockBreaker;
import com.stn.mobai.entity.ISensoryMob;
import com.stn.mobai.entity.ai.BreakBlockGoal;
import com.stn.mobai.entity.ai.MobSenseGoal;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to inject STN AI goals into vanilla zombies.
 * This allows testing the AI system without creating custom entities.
 */
@Mixin(ZombieEntity.class)
public abstract class ZombieEntityMixin extends HostileEntity implements ISensoryMob, IBlockBreaker {

    @Unique
    private boolean stn_isBreakingBlock = false;

    protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void stn_injectGoals(CallbackInfo ci) {
        // Add STN AI goals to vanilla zombies
        this.goalSelector.add(2, new BreakBlockGoal(this, getBlockBreakSpeed(), getBaseBreakTime()));
        this.goalSelector.add(3, new MobSenseGoal(this));
    }

    // ISensoryMob implementation - zombies have good senses
    @Override
    public double getSoundDetectionRange() {
        return 32.0;
    }

    @Override
    public float getSoundWeight() {
        return 1.0f;
    }

    @Override
    public double getSmellRange() {
        return 64.0;
    }

    @Override
    public float getSmellWeight() {
        return 0.8f;
    }

    @Override
    public boolean canSmell() {
        return true;
    }

    @Override
    public double getLightDetectionRange() {
        return 48.0;
    }

    @Override
    public float getLightWeight() {
        return 0.6f;
    }

    @Override
    public boolean canDetectLight() {
        return true;
    }

    @Override
    public double getHeatDetectionRange() {
        return 32.0;
    }

    @Override
    public float getHeatWeight() {
        return 0.7f;
    }

    @Override
    public boolean canDetectHeat() {
        return true;
    }

    @Override
    public double getVillageDetectionRange() {
        return 96.0;
    }

    @Override
    public float getVillageWeight() {
        return 0.5f;
    }

    @Override
    public boolean canTargetVillages() {
        return true;
    }

    // IBlockBreaker implementation
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

    @Override
    public double getWoodBreakMultiplier() {
        return 1.0;
    }

    @Override
    public double getStoneBreakMultiplier() {
        return 0.5; // Zombies are slower at stone
    }

    // BlockBreakAnimatable implementation
    @Override
    public void triggerBlockBreakSwing() {
        this.swingHand(this.getActiveHand());
    }

    @Override
    public boolean isBreakingBlock() {
        return stn_isBreakingBlock;
    }

    @Override
    public void setBreakingBlock(boolean breaking) {
        stn_isBreakingBlock = breaking;
    }
}
