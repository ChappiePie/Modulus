package chappie.modulus.util.events;

import chappie.modulus.util.model.ModelProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

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
    class RendererChangeEvent<T extends LivingEntity, M extends EntityModel<T>> {

        private final LivingEntityRenderer<T, M> renderer;
        private final ModelProperties modelProperties;
        private final PoseStack poseStack;
        private final MultiBufferSource multiBufferSource;
        private final RenderType renderType;
        private final int packedLight, packedOverlay;
        private final T entity;
        private float red, green, blue, alpha;

        public RendererChangeEvent(T entity, LivingEntityRenderer<T, M> renderer, ModelProperties modelProperties, PoseStack poseStack, MultiBufferSource multiBufferSource, RenderType renderType, int packedLight, int packedOverlay) {
            this.entity = entity;
            this.renderer = renderer;
            this.modelProperties = modelProperties;
            this.poseStack = poseStack;
            this.multiBufferSource = multiBufferSource;
            this.renderType = renderType;
            this.packedLight = packedLight;
            this.packedOverlay = packedOverlay;
        }

        public void setColor(float red, float green, float blue, float alpha) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }

        @SuppressWarnings("unchecked")
        public T getEntity() {
            return (T) this.entity;
        }

        public LivingEntityRenderer<T, M> renderer() {
            return renderer;
        }

        public PoseStack poseStack() {
            return poseStack;
        }

        public MultiBufferSource multiBufferSource() {
            return multiBufferSource;
        }

        public RenderType renderType() {
            return renderType;
        }

        public int packedLight() {
            return packedLight;
        }

        public int packedOverlay() {
            return packedOverlay;
        }

        public ModelProperties modelProperties() {
            return modelProperties;
        }

        public float red() {
            return red;
        }

        public float green() {
            return green;
        }

        public float blue() {
            return blue;
        }

        public float alpha() {
            return alpha;
        }
    }
}
