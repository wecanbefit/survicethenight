package com.stn.mobai.mixin;

import com.stn.core.api.ISoundEmitter;
import com.stn.mobai.entity.ai.sense.SenseManager;
import com.stn.mobai.entity.ai.sense.SoundVolumes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to detect explosions (TNT, creepers, etc.) and register them as very loud sounds.
 */
@Mixin(Explosion.class)
public abstract class ExplosionMixin {

    @Shadow @Final private World world;
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @Final private float power;
    @Shadow @Final private Entity entity;

    @Inject(method = "collectBlocksAndDamageEntities", at = @At("HEAD"))
    private void onExplosion(CallbackInfo ci) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        // Calculate volume based on explosion power
        // TNT = 4.0 power, Creeper = 3.0, Charged Creeper = 6.0
        // Scale so that standard explosions are very loud
        float volume = Math.min(SoundVolumes.EXPLOSION * (power / 3.0f), 2.0f);

        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);

        // Only pass as source if it's a LivingEntity (creeper, etc.)
        // TNT entities are not LivingEntity so pass null
        LivingEntity source = (entity instanceof LivingEntity living) ? living : null;

        SenseManager.getInstance().registerSound(
            serverWorld,
            pos,
            volume,
            source,
            ISoundEmitter.SoundType.EXPLOSION
        );
    }
}
