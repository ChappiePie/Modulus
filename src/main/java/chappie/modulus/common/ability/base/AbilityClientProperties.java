package chappie.modulus.common.ability.base;

import chappie.modulus.util.events.RendererChangeCallback;
import chappie.modulus.util.events.SetupAnimCallback;
import chappie.modulus.util.model.ModelProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;

public interface AbilityClientProperties {

    AbilityClientProperties DUMMY = new AbilityClientProperties() {
    };

    /**
     * Render on the entity model (3D world space).
     */
    default void render(LivingEntityRenderer<? extends LivingEntity, ? extends LivingEntityRenderState, ? extends EntityModel<?>> renderer, PoseStack poseStack, SubmitNodeCollector bufferIn, int packedLightIn, LivingEntity entity, ModelProperties modelProperties) {
    }

    /**
     * Custom screen overlay (HUD). Use for effects like speed trails, vignettes, etc.
     * Called every frame when the ability is present (regardless of enabled state).
     */
    default void renderOverlay(GuiGraphicsExtractor graphics, float partialTick, int screenWidth, int screenHeight) {
    }

    default void setupAnim(SetupAnimCallback.SetupAnimEvent event) {
    }

    default boolean rendererChange(RendererChangeCallback.RendererChangeEvent<? extends LivingEntity, ? extends LivingEntityRenderState, ? extends EntityModel<? super LivingEntityRenderState>> event) {
        return false;
    }
}
