package com.stn.spiders.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SpiderEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.util.Identifier;

/**
 * A spider renderer with custom scale and texture.
 */
public class ScaledSpiderRenderer<T extends SpiderEntity> extends SpiderEntityRenderer<T> {

    private final float scale;
    private final Identifier texture;

    public ScaledSpiderRenderer(EntityRendererFactory.Context context, float scale, String textureName) {
        super(context);
        this.scale = scale;
        this.texture = Identifier.of("stn_spiders", "textures/entity/" + textureName + ".png");
    }

    @Override
    protected void scale(LivingEntityRenderState state, MatrixStack matrices) {
        matrices.scale(scale, scale, scale);
        super.scale(state, matrices);
    }
}
