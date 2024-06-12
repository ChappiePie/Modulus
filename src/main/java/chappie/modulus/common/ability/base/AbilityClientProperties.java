package chappie.modulus.common.ability.base;

import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.events.RendererChangeEvent;
import chappie.modulus.util.events.SetupAnimEvent;
import chappie.modulus.util.model.ModelProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;

public interface AbilityClientProperties {

    AbilityClientProperties DUMMY = new AbilityClientProperties() {};

    default void render(EntityRendererProvider.Context context, LivingEntityRenderer<? extends LivingEntity, ? extends EntityModel<?>> renderer, PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, LivingEntity entity, ModelProperties modelProperties) {
    }

    default void setupAnim(SetupAnimEvent<? extends LivingEntity, ? extends HumanoidModel<?>> event) {
    }

    default void rendererChange(RendererChangeEvent<? extends LivingEntity, ? extends EntityModel<?>> event) {
    }
}
