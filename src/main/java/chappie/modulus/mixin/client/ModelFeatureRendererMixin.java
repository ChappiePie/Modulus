package chappie.modulus.mixin.client;

import chappie.modulus.util.events.SetupAnimCallback;
import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.render.IRenderStateEntity;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
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
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;setupAnim(Ljava/lang/Object;)V", shift = At.Shift.AFTER)
    )
    private <S> void modulus$afterSetupAnim(SubmitNodeStorage.ModelSubmit<S> submit,
                                            RenderType renderType,
                                            VertexConsumer consumer,
                                            OutlineBufferSource outlineBufferSource,
                                            MultiBufferSource.BufferSource crumblingBufferSource,
                                            CallbackInfo ci) {
        Model<? super S> model = submit.model();
        if (!(model instanceof HumanoidModel<?> humanoidModel)) {
            return;
        }
        if (!(submit.state() instanceof LivingEntityRenderState livingState)) {
            return;
        }
        if (!(livingState instanceof IRenderStateEntity<?> poseState)) {
            return;
        }

        if (poseState.modulus$poseCache().hasPose()) {
            // Primary renderer already produced a pose this frame, just clone it so armor/feature models sync up.
            poseState.modulus$poseCache().applyPose(humanoidModel);
            return;
        }

        if (!(poseState.modulus$entity() != null
                && model instanceof IHasModelProperties mp)) {
            return;
        }
        SetupAnimCallback.SetupAnimEvent event = new SetupAnimCallback.SetupAnimEvent(
                poseState.modulus$entity(),
                livingState,
                humanoidModel,
                mp.modulus$modelProperties()
        );
        SetupAnimCallback.EVENT.invoker().event(event);
        poseState.modulus$poseCache().storePose(humanoidModel);
    }
}
