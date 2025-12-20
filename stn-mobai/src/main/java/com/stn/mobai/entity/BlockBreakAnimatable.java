package com.stn.mobai.entity;

/**
 * Interface for entities that can play animations when breaking blocks.
 */
public interface BlockBreakAnimatable {

    /**
     * Called when the entity swings at a block during breaking.
     * Should trigger an attack/swing animation.
     */
    void triggerBlockBreakSwing();

    /**
     * Check if the entity is currently breaking a block.
     */
    boolean isBreakingBlock();

    /**
     * Set whether the entity is breaking a block.
     */
    void setBreakingBlock(boolean breaking);
}
