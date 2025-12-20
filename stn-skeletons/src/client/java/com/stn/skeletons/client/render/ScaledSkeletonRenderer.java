package com.stn.skeletons.client.render;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SkeletonEntityRenderer;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.util.Identifier;

/**
 * A skeleton renderer with a custom texture path.
 */
public class ScaledSkeletonRenderer extends SkeletonEntityRenderer {

    private final Identifier texture;

    public ScaledSkeletonRenderer(EntityRendererFactory.Context context, float scale, String textureName) {
        super(context);
        this.texture = Identifier.of("stn_skeletons", "textures/entity/" + textureName + ".png");
    }

    @Override
    public Identifier getTexture(AbstractSkeletonEntity entity) {
        return texture;
    }
}
