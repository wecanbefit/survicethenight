package com.stn.zombies.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.client.render.entity.state.ZombieEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;

/**
 * A zombie renderer with custom scale and texture.
 */
public class ScaledZombieRenderer extends ZombieEntityRenderer {

    private final float scale;
    private final Identifier texture;

    public ScaledZombieRenderer(EntityRendererFactory.Context context, float scale, String textureName) {
        super(context);
        this.scale = scale;
        this.texture = Identifier.of("stn_zombies", "textures/entity/" + textureName + ".png");
    }

    @Override
    protected void scale(ZombieEntityRenderState state, MatrixStack matrices) {
        matrices.scale(scale, scale, scale);
        super.scale(state, matrices);
    }
}
