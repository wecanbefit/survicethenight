package com.stn.spiders.entity;

import com.stn.spiders.config.STNSpidersConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Broodmother Spider - Summoner/mini-boss role
 * Periodically spawns spiderlings. High health, slow movement.
 */
public class BroodmotherSpiderEntity extends SpiderEntity {

    private int spawnCooldown = 100; // Start with some delay
    private int spiderlingsSpawned = 0;

    public BroodmotherSpiderEntity(EntityType<? extends SpiderEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createBroodmotherAttributes() {
        return HostileEntity.createHostileAttributes()
            .add(EntityAttributes.GENERIC_MAX_HEALTH, STNSpidersConfig.BROODMOTHER_HEALTH)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, STNSpidersConfig.BROODMOTHER_SPEED)
            .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, STNSpidersConfig.BROODMOTHER_DAMAGE)
            .add(EntityAttributes.GENERIC_ARMOR, STNSpidersConfig.BROODMOTHER_ARMOR)
            .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.5)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();

        if (spawnCooldown > 0) {
            spawnCooldown--;
        }

        if (!this.getWorld().isClient() && this.getTarget() != null) {
            // Spawn spiderlings when in combat
            if (spawnCooldown <= 0 && spiderlingsSpawned < STNSpidersConfig.BROODMOTHER_MAX_SPIDERLINGS) {
                spawnSpiderling();
                spawnCooldown = STNSpidersConfig.BROODMOTHER_SPAWN_COOLDOWN;
            }

            // Building up spawn particles
            if (spawnCooldown > 0 && spawnCooldown <= 40 && this.random.nextInt(3) == 0) {
                this.getWorld().addParticle(
                    ParticleTypes.ITEM_SNOWBALL,
                    this.getX() + this.random.nextGaussian() * 0.5,
                    this.getY() + 0.3,
                    this.getZ() + this.random.nextGaussian() * 0.5,
                    0, 0.05, 0
                );
            }
        }

        // Reset spawned count when out of combat for a while
        if (this.getTarget() == null && spiderlingsSpawned > 0) {
            if (this.random.nextInt(200) == 0) {
                spiderlingsSpawned = Math.max(0, spiderlingsSpawned - 1);
            }
        }
    }

    private void spawnSpiderling() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) return;

        // Spawn a cave spider as spiderling (smaller)
        CaveSpiderEntity spiderling = EntityType.CAVE_SPIDER.create(serverWorld);
        if (spiderling == null) return;

        // Position near the broodmother
        double offsetX = this.random.nextGaussian() * 1.5;
        double offsetZ = this.random.nextGaussian() * 1.5;

        spiderling.setPosition(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
        spiderling.initialize(serverWorld, serverWorld.getLocalDifficulty(this.getBlockPos()),
            SpawnReason.MOB_SUMMONED, null);

        // Copy target
        if (this.getTarget() != null) {
            spiderling.setTarget(this.getTarget());
        }

        serverWorld.spawnEntity(spiderling);
        spiderlingsSpawned++;

        // Spawn effects
        this.playSound(SoundEvents.ENTITY_SPIDER_AMBIENT, 1.0f, 1.5f);

        for (int i = 0; i < 10; i++) {
            this.getWorld().addParticle(
                ParticleTypes.POOF,
                spiderling.getX() + this.random.nextGaussian() * 0.3,
                spiderling.getY() + 0.2,
                spiderling.getZ() + this.random.nextGaussian() * 0.3,
                0, 0.1, 0
            );
        }
    }

    @Override
    public void onDeath(net.minecraft.entity.damage.DamageSource damageSource) {
        super.onDeath(damageSource);

        // Death burst of spiderlings
        if (!this.getWorld().isClient() && this.getWorld() instanceof ServerWorld serverWorld) {
            for (int i = 0; i < 3; i++) {
                CaveSpiderEntity spiderling = EntityType.CAVE_SPIDER.create(serverWorld);
                if (spiderling != null) {
                    double offsetX = this.random.nextGaussian() * 2.0;
                    double offsetZ = this.random.nextGaussian() * 2.0;
                    spiderling.setPosition(this.getX() + offsetX, this.getY(), this.getZ() + offsetZ);
                    spiderling.initialize(serverWorld, serverWorld.getLocalDifficulty(this.getBlockPos()),
                        SpawnReason.MOB_SUMMONED, null);
                    serverWorld.spawnEntity(spiderling);
                }
            }
        }
    }
}
