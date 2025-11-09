package chappie.modulus.mixin.client;

import chappie.modulus.util.events.SetupAnimCallback;
import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.render.IRenderStateEntity;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelFeatureRenderer.class)
public abstract class ModelFeatureRendererMixin {

    @Inject(
            method = "renderModel(Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelSubmit;Lnet/minecraft/client/renderer/RenderType;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/renderer/OutlineBufferSource;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", ordinal = 0)
    )
    private <S> void modulus$populateModelProps(SubmitNodeStorage.ModelSubmit<S> submit,
                                                RenderType renderType,
                                                VertexConsumer consumer,
                                                OutlineBufferSource outlineBufferSource,
                                                MultiBufferSource.BufferSource crumblingBufferSource,
                                                CallbackInfo ci) {
        if (!(submit.model() instanceof HumanoidModel<?> model) || !(submit.state() instanceof LivingEntityRenderState livingState) || !(livingState instanceof IRenderStateEntity<?> stateEntity)) {
            return;
        }

        if (model instanceof IHasModelProperties iModel) {
            SetupAnimCallback.EVENT.invoker().event(
                    new SetupAnimCallback.SetupAnimEvent(
                            stateEntity.modulus$entity(),
                            livingState,
                            model,
                            iModel.modulus$modelProperties()
                    )
            );
        }
    }
}
