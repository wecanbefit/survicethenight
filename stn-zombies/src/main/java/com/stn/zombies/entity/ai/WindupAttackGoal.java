package com.stn.zombies.entity.ai;

import com.stn.zombies.entity.BruiserZombieEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

/**
 * Attack goal with a windup animation before dealing damage.
 * Used by Bruiser Zombie for telegraphed heavy attacks.
 */
public class WindupAttackGoal extends MeleeAttackGoal {

    private final int windupTicks;
    private int currentWindup = 0;
    private boolean isWindingUp = false;

    public WindupAttackGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle, int windupTicks) {
        super(mob, speed, pauseWhenMobIdle);
        this.windupTicks = windupTicks;
    }

    @Override
    protected void attack(LivingEntity target) {
        if (this.canAttack(target)) {
            if (!isWindingUp) {
                // Start windup
                isWindingUp = true;
                currentWindup = windupTicks;

                if (this.mob instanceof BruiserZombieEntity bruiser) {
                    bruiser.setWindingUp(true);
                }

                // Visual/audio cue for windup
                if (this.mob.getWorld() instanceof ServerWorld sw) {
                    sw.spawnParticles(
                        ParticleTypes.ANGRY_VILLAGER,
                        this.mob.getX(),
                        this.mob.getY() + 2.0,
                        this.mob.getZ(),
                        1, 0, 0, 0, 0
                    );
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (isWindingUp) {
            currentWindup--;

            // Shake/telegraph during windup
            if (currentWindup % 4 == 0 && this.mob.getWorld() instanceof ServerWorld sw) {
                sw.spawnParticles(
                    ParticleTypes.CRIT,
                    this.mob.getX(),
                    this.mob.getY() + 1.5,
                    this.mob.getZ(),
                    1, 0.3, 0.3, 0.3, 0
                );
            }

            if (currentWindup <= 0) {
                // Execute attack
                isWindingUp = false;

                if (this.mob instanceof BruiserZombieEntity bruiser) {
                    bruiser.setWindingUp(false);
                }

                LivingEntity target = this.mob.getTarget();
                if (target != null && this.mob.squaredDistanceTo(target) < 4.0 && this.mob.getWorld() instanceof ServerWorld serverWorld) {
                    // Heavy attack with extra knockback
                    this.mob.tryAttack(serverWorld, target);
                    this.mob.swingHand(this.mob.getActiveHand());

                    // Impact effect
                    this.mob.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.8f);

                    if (this.mob.getWorld() instanceof ServerWorld sw) {
                        sw.spawnParticles(
                            ParticleTypes.CRIT,
                            target.getX(),
                            target.getY() + 1.0,
                            target.getZ(),
                            10, 0.5, 0.5, 0.5, 0.1
                        );
                    }
                }

                this.resetCooldown();
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        isWindingUp = false;
        currentWindup = 0;

        if (this.mob instanceof BruiserZombieEntity bruiser) {
            bruiser.setWindingUp(false);
        }
    }
}
