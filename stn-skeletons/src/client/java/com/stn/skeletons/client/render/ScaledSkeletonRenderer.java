package com.stn.skeletons.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.client.render.entity.state.SkeletonEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.util.Identifier;

/**
 * A skeleton renderer with a custom texture path.
 */
public class ScaledSkeletonRenderer extends SkeletonEntityRenderer {

    private final float scale;
    private final Identifier texture;

    public ScaledSkeletonRenderer(EntityRendererFactory.Context context, float scale, String textureName) {
        super(context);
        this.scale = scale;
        this.texture = Identifier.of("stn_skeletons", "textures/entity/" + textureName + ".png");
    }

    @Override
    protected void scale(SkeletonEntityRenderState state, MatrixStack matrices) {
        matrices.scale(scale, scale, scale);
        super.scale(state, matrices);
    }
}
