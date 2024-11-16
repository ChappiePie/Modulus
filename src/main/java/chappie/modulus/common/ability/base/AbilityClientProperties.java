package chappie.modulus.common.ability.base;

import chappie.modulus.util.events.RendererChangeCallback;
import chappie.modulus.util.events.SetupAnimCallback;
import chappie.modulus.util.model.ModelProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

public interface AbilityClientProperties {

    AbilityClientProperties DUMMY = new AbilityClientProperties() {};

    default void render(LivingEntityRenderer<? extends LivingEntity, ? extends EntityModel<?>> renderer, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, LivingEntity entity, ModelProperties modelProperties) {
    }

    default void setupAnim(SetupAnimCallback.SetupAnimEvent event) {
    }

    default boolean rendererChange(RendererChangeCallback.RendererChangeEvent<? extends LivingEntity, ? extends EntityModel<?>> event) {
        return false;
    }
}
