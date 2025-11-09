package chappie.modulus.util.events;

import chappie.modulus.util.model.ModelProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface RendererChangeCallback {
    Event<RendererChangeCallback> EVENT = EventFactory.createArrayBacked(RendererChangeCallback.class,
            (listeners) -> (event) -> {
                boolean canceled = false;
                for (RendererChangeCallback listener : listeners) {
                    if (listener.event(event)) {
                        canceled = true;
                    }
                }
                return canceled;
            });

    boolean event(RendererChangeEvent event);

    /**
     * Fired before the entity model is rendered.
     * Cancelling this event will prevent the entity model from being rendered.
     * <p>
     * This event is suitable for any additional renders you want to apply to the entity,
     * or to render a model other than the entity.
     */
    record RendererChangeEvent<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>(
            T entity, S renderState, LivingEntityRenderer<T, S, M> renderer, ModelProperties modelProperties,
            PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Model<? super S> model, RenderType renderType,
            int packedLight, int packedOverlay, int tintColor, @Nullable TextureAtlasSprite sprite, int outlineColor,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {

        public void submitDefaultModel() {
            this.submitModel(this.model, this.renderType, this.packedLight, this.packedOverlay, this.tintColor, this.sprite, this.outlineColor, this.crumblingOverlay);
        }

        public void submitTranslucent(int alpha) {
            int packedTint = (Math.max(0, Math.min(255, alpha)) & 0xFF) << 24 | 0x00FFFFFF;
            RenderType translucent = RenderType.itemEntityTranslucentCull(this.renderer.getTextureLocation(this.renderState));
            this.submitModel(this.model, translucent, this.packedLight, this.packedOverlay, packedTint, this.sprite, this.outlineColor, this.crumblingOverlay);
        }

        public void submitModel(
                Model<? super S> model,
                RenderType renderType,
                int packedLight,
                int packedOverlay,
                int tintColor,
                @Nullable TextureAtlasSprite sprite,
                int outlineColor,
                @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
        ) {
            this.submitNodeCollector.submitModel(model, this.renderState, this.poseStack, renderType, packedLight, packedOverlay, tintColor, sprite, outlineColor, crumblingOverlay);
        }

    }
}
