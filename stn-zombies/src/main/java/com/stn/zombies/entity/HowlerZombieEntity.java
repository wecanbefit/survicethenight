package com.stn.zombies.entity;

import com.stn.core.api.ISoundEmitter;
import com.stn.mobai.entity.BlockBreakAnimatable;
import com.stn.mobai.entity.ai.sense.SenseManager;
import com.stn.zombies.config.STNZombiesConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.List;

/**
 * Howler Zombie - Support/threat escalator role
 * Howls to attract nearby zombies and buff them.
 */
public class HowlerZombieEntity extends ZombieEntity implements BlockBreakAnimatable {

    private boolean isBreakingBlock = false;
    private int howlCooldown = 0;
    private boolean isHowling = false;
    private int howlWindup = 0;

    public HowlerZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createHowlerAttributes() {
        return ZombieEntity.createZombieAttributes()
            .add(EntityAttributes.MAX_HEALTH, STNZombiesConfig.HOWLER_HEALTH)
            .add(EntityAttributes.MOVEMENT_SPEED, STNZombiesConfig.HOWLER_SPEED)
            .add(EntityAttributes.ATTACK_DAMAGE, STNZombiesConfig.HOWLER_DAMAGE)
            .add(EntityAttributes.FOLLOW_RANGE, 48.0)
            .add(EntityAttributes.ARMOR, 0.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (howlCooldown > 0) {
            howlCooldown--;
        }

        if (isHowling && howlWindup > 0) {
            howlWindup--;
            spawnHowlParticles();

            if (howlWindup <= 0) {
                completeHowl();
            }
        }

        // Try to howl when spotting player
        if (!this.getWorld().isClient() && this.getTarget() != null && canHowl()) {
            if (this.random.nextInt(100) == 0) {
                startHowl();
            }
        }

        // Ambient eerie sounds
        if (!this.getWorld().isClient() && this.random.nextInt(300) == 0 && !isHowling) {
            this.getWorld().playSound(
                null,
                this.getBlockPos(),
                SoundEvents.ENTITY_GHAST_AMBIENT,
                SoundCategory.HOSTILE,
                0.3f,
                0.6f
            );
        }
    }

    public void startHowl() {
        if (howlCooldown > 0 || isHowling) return;

        isHowling = true;
        howlWindup = 40; // 2 second windup

        this.getWorld().playSound(
            null,
            this.getBlockPos(),
            SoundEvents.ENTITY_RAVAGER_ROAR,
            SoundCategory.HOSTILE,
            1.5f,
            0.5f
        );
    }

    private void completeHowl() {
        isHowling = false;
        howlCooldown = STNZombiesConfig.HOWLER_HOWL_COOLDOWN;

        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        // Loud howl sound
        this.getWorld().playSound(
            null,
            this.getBlockPos(),
            SoundEvents.ENTITY_RAVAGER_ROAR,
            SoundCategory.HOSTILE,
            3.0f,
            0.4f
        );

        // Register as very loud sound for mob AI
        SenseManager.getInstance().registerSound(
            serverWorld,
            this.getBlockPos(),
            2.0f, // Very loud
            this,
            ISoundEmitter.SoundType.GENERIC
        );

        // Buff nearby zombies
        double range = STNZombiesConfig.HOWLER_HOWL_RANGE;
        Box searchBox = new Box(this.getBlockPos()).expand(range);

        List<MobEntity> nearbyMobs = serverWorld.getEntitiesByClass(
            MobEntity.class,
            searchBox,
            mob -> mob != this && mob instanceof ZombieEntity
        );

        for (MobEntity mob : nearbyMobs) {
            // Apply speed and strength buff
            if (mob instanceof LivingEntity living) {
                living.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.SPEED,
                    STNZombiesConfig.HOWLER_BUFF_DURATION,
                    0,
                    false,
                    true
                ));
                living.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.STRENGTH,
                    STNZombiesConfig.HOWLER_BUFF_DURATION,
                    0,
                    false,
                    true
                ));

                // Alert them to our target
                if (this.getTarget() != null) {
                    mob.setTarget(this.getTarget());
                }
            }
        }

        // Big particle burst
        serverWorld.spawnParticles(
            ParticleTypes.NOTE,
            this.getX(),
            this.getY() + 1.5,
            this.getZ(),
            30, 2.0, 1.0, 2.0, 0.1
        );
    }

    private void spawnHowlParticles() {
        if (this.getWorld() instanceof ServerWorld sw) {
            sw.spawnParticles(
                ParticleTypes.NOTE,
                this.getX(),
                this.getY() + 2.0,
                this.getZ(),
                2, 0.3, 0, 0.3, 0.05
            );
        }
    }

    public boolean canHowl() {
        return howlCooldown <= 0 && !isHowling;
    }

    public boolean isHowling() {
        return isHowling;
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

    @Override
    protected boolean burnsInDaylight() {
        return true;
    }
}
