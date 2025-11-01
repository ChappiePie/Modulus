package chappie.modulus.client;

import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.render.IRenderStateEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;

public class AbilityLayerRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {

    private final LivingEntityRenderer<T, S, M> renderer;

    public AbilityLayerRenderer(LivingEntityRenderer<T, S, M> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int packedLight, S renderState, float yRot, float xRot) {
        if (renderState instanceof IRenderStateEntity s) {
            CommonUtil.getAbilities(s.modulus$entity()).forEach(ability -> ability.clientProperties(c -> c.render(this.renderer, poseStack, nodeCollector, packedLight, ability.entity, ((IHasModelProperties) this.getParentModel()).modulus$modelProperties())));
        }
    }
}