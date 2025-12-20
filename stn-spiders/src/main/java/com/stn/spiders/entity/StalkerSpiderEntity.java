package com.stn.spiders.entity;

import com.stn.spiders.config.STNSpidersConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

/**
 * Stalker Spider - Ambush role
 * Semi-invisible while stationary in darkness.
 * First hit deals bonus damage.
 */
public class StalkerSpiderEntity extends SpiderEntity {

    private boolean hasAmbushed = false;
    private int stationaryTicks = 0;

    public StalkerSpiderEntity(EntityType<? extends SpiderEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createStalkerAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, STNSpidersConfig.STALKER_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, STNSpidersConfig.STALKER_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, STNSpidersConfig.STALKER_DAMAGE)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        // Track stationary time
        if (this.getVelocity().horizontalLengthSquared() < 0.001) {
            stationaryTicks++;
        } else {
            stationaryTicks = 0;
        }

        // Reset ambush flag when target is lost
        if (this.getTarget() == null) {
            hasAmbushed = false;
        }
    }

    @Override
    public boolean tryAttack(Entity target) {
        boolean hit = super.tryAttack(target);

        if (hit && target instanceof LivingEntity living && !this.getWorld().isClient()) {
            // Ambush bonus damage on first hit
            if (!hasAmbushed) {
                hasAmbushed = true;
                living.damage(this.getDamageSources().mobAttack(this),
                    (float) STNSpidersConfig.STALKER_AMBUSH_BONUS_DAMAGE);

                // Ambush effect
                this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.8f);

                for (int i = 0; i < 10; i++) {
                    target.getWorld().addParticle(
                        ParticleTypes.CRIT,
                        target.getX() + this.random.nextGaussian() * 0.5,
                        target.getY() + 0.5,
                        target.getZ() + this.random.nextGaussian() * 0.5,
                        0, 0.1, 0
                    );
                }
            }
        }

        return hit;
    }

    /**
     * Check if this spider should be semi-invisible.
     * Used by renderer for transparency effect.
     */
    public boolean isCamouflaged() {
        if (stationaryTicks < 20) return false; // Need to be still for 1 second

        BlockPos pos = this.getBlockPos();
        int lightLevel = this.getWorld().getLightLevel(LightType.BLOCK, pos);
        int skyLight = this.getWorld().getLightLevel(LightType.SKY, pos);
        int totalLight = Math.max(lightLevel, skyLight - this.getWorld().getAmbientDarkness());

        return totalLight <= STNSpidersConfig.STALKER_INVISIBILITY_LIGHT_LEVEL;
    }

    /**
     * Get the transparency level for rendering.
     * 0.0 = fully invisible, 1.0 = fully visible
     */
    public float getVisibility() {
        if (!isCamouflaged()) return 1.0f;

        // Fade to 20% visibility when camouflaged
        return 0.2f;
    }
}
