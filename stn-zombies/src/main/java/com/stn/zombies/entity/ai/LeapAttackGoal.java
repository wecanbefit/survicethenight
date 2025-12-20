package com.stn.zombies.entity.ai;

import com.stn.zombies.entity.ZombabieEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * AI Goal for Zombabie to leap at targets.
 */
public class LeapAttackGoal extends Goal {

    private final ZombabieEntity zombabie;
    private final double leapVelocity;
    private LivingEntity target;

    public LeapAttackGoal(ZombabieEntity zombabie, double leapVelocity) {
        this.zombabie = zombabie;
        this.leapVelocity = leapVelocity;
        this.setControls(EnumSet.of(Control.JUMP, Control.MOVE));
    }

    @Override
    public boolean canStart() {
        this.target = this.zombabie.getTarget();
        if (this.target == null) return false;

        double distance = this.zombabie.squaredDistanceTo(this.target);
        // Leap when in range (3-6 blocks)
        return distance >= 9.0 && distance <= 36.0 && this.zombabie.canLeap() && this.zombabie.canSee(this.target);
    }

    @Override
    public boolean shouldContinue() {
        return false; // Single leap, don't continue
    }

    @Override
    public void start() {
        this.zombabie.leap();
    }
}
