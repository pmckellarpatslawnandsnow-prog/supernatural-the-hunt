package com.fanmods.snhunt.client;

import com.fanmods.snhunt.SNHunt;
import com.fanmods.snhunt.entity.HuntMonsterEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.EyesFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.AbstractZombieModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Humanoid renderer shared by all the mod's monsters. Uses the vanilla zombie-shaped model with the mod's own
 * textures, optional "reaching" arms, scale, see-through rendering for ghosts, and glowing eyes.
 */
public class HuntMonsterRenderer<T extends HuntMonsterEntity> extends BipedEntityRenderer<T, BipedEntityModel<T>> {
    private final Identifier texture;
    private final float scale;
    private final boolean ghostly;

    public HuntMonsterRenderer(EntityRendererFactory.Context ctx, String name, boolean reachingArms, float scale,
                               boolean ghostly, @Nullable String eyes) {
        super(ctx, reachingArms
                ? new ReachingModel<T>(ctx.getPart(EntityModelLayers.ZOMBIE))
                : new BipedEntityModel<T>(ctx.getPart(EntityModelLayers.ZOMBIE)), 0.5f * scale);
        this.texture = SNHunt.id("textures/entity/" + name + ".png");
        this.scale = scale;
        this.ghostly = ghostly;
        if (eyes != null) {
            this.addFeature(new GlowEyes<>(this, SNHunt.id("textures/entity/" + eyes + ".png")));
        }
    }

    @Override
    public Identifier getTexture(T entity) {
        return texture;
    }

    @Override
    protected void scale(T entity, MatrixStack matrices, float amount) {
        matrices.scale(scale, scale, scale);
    }

    @Nullable
    @Override
    protected RenderLayer getRenderLayer(T entity, boolean showBody, boolean translucent, boolean showOutline) {
        if (ghostly && showBody) {
            return RenderLayer.getEntityTranslucent(getTexture(entity));
        }
        return super.getRenderLayer(entity, showBody, translucent, showOutline);
    }

    /** Zombie-style arms that reach forward while chasing. */
    static class ReachingModel<T extends HuntMonsterEntity> extends AbstractZombieModel<T> {
        ReachingModel(ModelPart root) {
            super(root);
        }

        @Override
        public boolean isAttacking(T entity) {
            return entity.isAttacking();
        }
    }

    static class GlowEyes<T extends HuntMonsterEntity> extends EyesFeatureRenderer<T, BipedEntityModel<T>> {
        private final RenderLayer layer;

        GlowEyes(FeatureRendererContext<T, BipedEntityModel<T>> context, Identifier texture) {
            super(context);
            this.layer = RenderLayer.getEyes(texture);
        }

        @Override
        public RenderLayer getEyesTexture() {
            return layer;
        }
    }
}
