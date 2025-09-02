package chappie.modulus.client;

import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.model.IHasModelProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

public class AbilityLayerRenderer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    private final LivingEntityRenderer<T, M> renderer;

    public AbilityLayerRenderer(LivingEntityRenderer<T, M> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        CommonUtil.getAbilities(livingEntity).forEach(ability -> ability.clientProperties(c -> c.render(this.renderer, poseStack, bufferSource, packedLight, ability.entity, ((IHasModelProperties) this.getParentModel()).modelProperties())));
    }
}