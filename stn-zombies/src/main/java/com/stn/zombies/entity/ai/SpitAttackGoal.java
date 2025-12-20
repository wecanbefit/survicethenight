package com.stn.zombies.entity.ai;

import com.stn.zombies.entity.SpitterZombieEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

import java.util.EnumSet;

/**
 * AI Goal for Spitter Zombie to shoot acid projectiles.
 */
public class SpitAttackGoal extends Goal {

    private final SpitterZombieEntity spitter;
    private final double speed;
    private final int attackInterval;
    private final float maxRange;
    private int ticksUntilAttack;
    private int seeTime;

    public SpitAttackGoal(SpitterZombieEntity spitter, double speed, int attackInterval, float maxRange) {
        this.spitter = spitter;
        this.speed = speed;
        this.attackInterval = attackInterval;
        this.maxRange = maxRange;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        LivingEntity target = this.spitter.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean shouldContinue() {
        return this.canStart();
    }

    @Override
    public void start() {
        this.ticksUntilAttack = 20;
        this.seeTime = 0;
    }

    @Override
    public void stop() {
        this.seeTime = 0;
    }

    @Override
    public void tick() {
        LivingEntity target = this.spitter.getTarget();
        if (target == null) return;

        double distance = this.spitter.squaredDistanceTo(target);
        boolean canSee = this.spitter.canSee(target);

        if (canSee) {
            seeTime++;
        } else {
            seeTime = 0;
        }

        // Keep distance - stay at range
        if (distance < 36.0) { // Too close, back up
            this.spitter.getNavigation().stop();
        } else if (distance > maxRange * maxRange) { // Too far, move closer
            this.spitter.getNavigation().startMovingTo(target, speed);
        }

        // Look at target
        this.spitter.getLookControl().lookAt(target, 30.0f, 30.0f);

        // Attack logic
        if (ticksUntilAttack > 0) {
            ticksUntilAttack--;
        }

        if (seeTime >= 20 && ticksUntilAttack <= 0 && distance <= maxRange * maxRange && this.spitter.canSpit()) {
            this.spitter.spit(target);
            ticksUntilAttack = attackInterval;
        }
    }
}
