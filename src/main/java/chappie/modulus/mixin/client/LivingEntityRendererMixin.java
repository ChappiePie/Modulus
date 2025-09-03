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
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
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

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    public void setupModelProperties(T livingEntity, S livingEntityRenderState, float f, CallbackInfo ci) {
        if (livingEntityRenderState instanceof IRenderStateEntity s) {
            s.modulus$setEntity(livingEntity);
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)V"))
    public void setupModelProperties2(S livingEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (this.model instanceof IHasModelProperties iModel) {
            iModel.modulus$setup(livingEntityRenderState, ClientUtil.getPartialTick(), this.layers);
            if (this.layers.stream().noneMatch(p -> p instanceof AbilityLayerRenderer)) {
                this.addLayer(new AbilityLayerRenderer<>((LivingEntityRenderer<T, S, M>) (Object) this));
            }
        }
    }

    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getWhiteOverlayProgress(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)F"))
    public void setup(S livingEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo info, @Local(ordinal = 0) RenderType type) {
        LivingEntityRenderer<T, S, M> renderer = (LivingEntityRenderer<T, S, M>) (Object) this;
        if (this.model instanceof IHasModelProperties iModel && livingEntityRenderState instanceof IRenderStateEntity e) {
            T entity = (T) e.modulus$entity();
            this.modulus$event = new RendererChangeCallback.RendererChangeEvent<>(entity, renderer, iModel.modulus$modelProperties(), poseStack, multiBufferSource, type, i, LivingEntityRenderer.getOverlayCoords(livingEntityRenderState, this.getWhiteOverlayProgress(livingEntityRenderState)));
            if (this.model instanceof HumanoidModel<?>) {
                SetupAnimCallback.EVENT.invoker().event(new SetupAnimCallback.SetupAnimEvent(entity, livingEntityRenderState, (HumanoidModel<? super S>) this.model, iModel.modulus$modelProperties()));
            }
        }
    }

    @WrapWithCondition(
            method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V")
    )
    private boolean renderIfAllowed(EntityModel instance, PoseStack poseStack, VertexConsumer vertexConsumer, int pPackedLight, int pPackedOverlay, int pColor) {
        if (this.modulus$event != null) {
            this.modulus$event.setColor(ARGB.red(pColor), ARGB.green(pColor), ARGB.blue(pColor), ARGB.alpha(pColor));
            boolean b = RendererChangeCallback.EVENT.invoker().event(this.modulus$event);
            this.modulus$event.multiBufferSource().getBuffer(this.modulus$event.renderType()); // rollback texture of entity
            return !b;
        }
        return true;
    }
}
