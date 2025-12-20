package com.stn.mobai.mixin;

import com.stn.core.api.ISoundEmitter;
import com.stn.mobai.entity.ai.sense.SenseManager;
import com.stn.mobai.entity.ai.sense.SoundVolumes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to track player movement and emit sounds for mob detection.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class PlayerMovementMixin extends PlayerEntity {

    @Unique
    private int stn_movementSoundCooldown = 0;

    @Unique
    private double stn_lastX = 0;

    @Unique
    private double stn_lastZ = 0;

    public PlayerMovementMixin(World world, net.minecraft.util.math.BlockPos pos, float yaw, com.mojang.authlib.GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void stn_trackMovement(CallbackInfo ci) {
        if (stn_movementSoundCooldown > 0) {
            stn_movementSoundCooldown--;
            return;
        }

        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        ServerWorld world = self.getServerWorld();

        // Calculate horizontal movement
        double dx = self.getX() - stn_lastX;
        double dz = self.getZ() - stn_lastZ;
        double horizontalMovement = Math.sqrt(dx * dx + dz * dz);

        stn_lastX = self.getX();
        stn_lastZ = self.getZ();

        // Only emit sound if actually moving horizontally
        if (horizontalMovement < 0.1) {
            return;
        }

        // Sneaking = silent
        if (self.isSneaking()) {
            return;
        }

        float baseVolume;
        int cooldown;

        if (self.isSprinting()) {
            baseVolume = SoundVolumes.PLAYER_SPRINT;
            cooldown = 8; // More frequent sounds when sprinting
        } else {
            baseVolume = SoundVolumes.PLAYER_WALK;
            cooldown = 15; // Walking sounds less frequent
        }

        // Apply armor noise multiplier
        float armorMultiplier = SoundVolumes.getArmorNoiseMultiplier(self);
        float volume = baseVolume * armorMultiplier;

        // Emit movement sound
        SenseManager.getInstance().registerSound(
            world,
            self.getBlockPos(),
            volume,
            self,
            ISoundEmitter.SoundType.MOVEMENT
        );

        stn_movementSoundCooldown = cooldown;
    }
}
