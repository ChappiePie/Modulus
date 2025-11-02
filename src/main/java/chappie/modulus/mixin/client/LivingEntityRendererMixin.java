package chappie.modulus.mixin.client;

import chappie.modulus.client.AbilityLayerRenderer;
import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.events.RendererChangeCallback;
import chappie.modulus.util.events.SetupAnimCallback;
import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.render.IRenderStateEntity;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
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
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
    @Unique
    private RendererChangeCallback.RendererChangeEvent<T, S, M> modulus$event;

    @Shadow
    protected abstract boolean addLayer(RenderLayer<S, M> p_115327_);

    @Shadow
    protected abstract float getWhiteOverlayProgress(S renderState);

    @Shadow
    protected abstract boolean shouldRenderLayers(S renderState);

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    public void setupModelProperties(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci) {
        if (livingEntityRenderState instanceof IRenderStateEntity s) {
            s.modulus$setEntity(livingEntity);
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("HEAD"))
    public void setupModelProperties2(S livingEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (this.shouldRenderLayers(livingEntityRenderState) && !this.layers.isEmpty()) {
            if (this.model instanceof IHasModelProperties iModel) {
                iModel.modulus$setup(livingEntityRenderState, ClientUtil.getPartialTick(), this.layers);
                if (this.layers.stream().noneMatch(p -> p instanceof AbilityLayerRenderer)) {
                    this.addLayer(new AbilityLayerRenderer<>((LivingEntityRenderer<T, S, M>) (Object) this));
                }
            }
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getWhiteOverlayProgress(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)F"))
    public void setup(S livingEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci, @Local(ordinal = 0) RenderType type) {
        LivingEntityRenderer<T, S, M> renderer = (LivingEntityRenderer<T, S, M>) (Object) this;
        if (this.model instanceof IHasModelProperties iModel && livingEntityRenderState instanceof IRenderStateEntity e) {
            T entity = (T) e.modulus$entity();
            int packedOverlay = LivingEntityRenderer.getOverlayCoords(livingEntityRenderState, this.getWhiteOverlayProgress(livingEntityRenderState));
            this.modulus$event = new RendererChangeCallback.RendererChangeEvent<>(entity, renderer, iModel.modulus$modelProperties(), poseStack, submitNodeCollector, type, livingEntityRenderState.lightCoords, packedOverlay);
            if (this.model instanceof HumanoidModel<?>) {
                SetupAnimCallback.EVENT.invoker().event(new SetupAnimCallback.SetupAnimEvent(entity, livingEntityRenderState, (HumanoidModel<? super S>) this.model, iModel.modulus$modelProperties()));
            }
        }
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
        if (this.modulus$event != null) {
            int color = tintColor;
            if (color == -1) {
                color = 0xFFFFFFFF;
            }
            this.modulus$event.setColor(ARGB.red(color), ARGB.green(color), ARGB.blue(color), ARGB.alpha(color));
            boolean b = RendererChangeCallback.EVENT.invoker().event(this.modulus$event);
            this.modulus$event = null;
            return !b;
        }
        return true;
    }
}
