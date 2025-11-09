package chappie.modulus.mixin.client;

import chappie.modulus.client.AbilityLayerRenderer;
import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.events.RendererChangeCallback;
import chappie.modulus.util.events.SetupAnimCallback;
import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.render.IRenderStateEntity;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@SuppressWarnings("unchecked")
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Shadow
    protected M model;
    @Shadow
    @Final
    protected List<RenderLayer<?, ?>> layers;

    @Shadow
    protected abstract boolean addLayer(RenderLayer<S, M> p_115327_);

    @Shadow
    protected abstract boolean shouldRenderLayers(S renderState);

    @SuppressWarnings("rawtypes")
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    public void setupRenderStateEntity(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci) {
        if (livingEntityRenderState instanceof IRenderStateEntity s) {
            s.modulus$setEntity(livingEntity);
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"))
    public void setupModelProperties(S livingEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (this.shouldRenderLayers(livingEntityRenderState) && !this.layers.isEmpty()) {
            if (this.model instanceof IHasModelProperties iModel) {
                iModel.modulus$setup(livingEntityRenderState, ClientUtil.getPartialTick(), this.layers);
                if (this.layers.stream().noneMatch(p -> p instanceof AbilityLayerRenderer)) {
                    this.addLayer(new AbilityLayerRenderer<>((LivingEntityRenderer<T, S, M>) (Object) this));
                }
            }
        }
    }

    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Ljava/lang/Object;)V", shift = At.Shift.AFTER)
    )
    private void modulus$afterSetupAnim(S livingEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (!(this.model instanceof HumanoidModel<?>)
                || !(this.model instanceof IHasModelProperties iModel)
                || !(livingEntityRenderState instanceof IRenderStateEntity<?> e && e.modulus$entity() != null)) {
            return;
        }

        SetupAnimCallback.EVENT.invoker().event(new SetupAnimCallback.SetupAnimEvent(e.modulus$entity(), livingEntityRenderState, (HumanoidModel<? super S>) this.model, iModel.modulus$modelProperties()));
    }

    @WrapWithCondition(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V")
    )
    private boolean renderIfAllowed(SubmitNodeCollector instance, Model<? super S> model,
                                    Object renderState,
                                    PoseStack poseStack,
                                    RenderType renderType,
                                    int packedLight,
                                    int packedOverlay,
                                    int tintColor,
                                    @Nullable TextureAtlasSprite sprite,
                                    int outlineColor,
                                    @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if (!(renderState instanceof LivingEntityRenderState livingState) || !(livingState instanceof IRenderStateEntity<?> entityState)) {
            return true;
        }

        LivingEntity entity = entityState.modulus$entity();
        if (entity == null || !(model instanceof IHasModelProperties iModel)) {
            return true;
        }

        LivingEntityRenderer<T, S, M> renderer = (LivingEntityRenderer<T, S, M>) (Object) this;
        RendererChangeCallback.RendererChangeEvent<T, S, M> event = new RendererChangeCallback.RendererChangeEvent<>(
                (T) entity,
                (S) livingState,
                renderer,
                iModel.modulus$modelProperties(),
                poseStack,
                instance,
                model,
                renderType,
                packedLight,
                packedOverlay,
                tintColor,
                sprite,
                outlineColor,
                crumblingOverlay
        );
        return !RendererChangeCallback.EVENT.invoker().event(event);
    }
}
