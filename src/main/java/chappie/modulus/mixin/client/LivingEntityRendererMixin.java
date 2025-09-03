package chappie.modulus.mixin.client;

import chappie.modulus.client.AbilityLayerRenderer;
import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.events.RendererChangeCallback;
import chappie.modulus.util.events.SetupAnimCallback;
import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.model.RotationProperties;
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
import net.minecraft.util.FastColor;
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
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Shadow
    protected M model;
    @Shadow
    @Final
    protected List<RenderLayer<?, ?>> layers;
    @Unique
    private RendererChangeCallback.RendererChangeEvent<T, M> modulus$event;

    @Shadow
    protected abstract boolean addLayer(RenderLayer<T, M> p_115327_);

    @Shadow
    protected abstract float getWhiteOverlayProgress(T livingEntity, float partialTicks);

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V"))
    public void setupModelProperties2(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci, @Local(ordinal = 8) float f5, @Local(ordinal = 7) float f8, @Local(ordinal = 6) float f7, @Local(ordinal = 2) float f2, @Local(ordinal = 4) float f6) {
        if (this.model instanceof IHasModelProperties iModel) {
            iModel.modulus$setup(new RotationProperties(entity, f5, f8, f7, f2, f6), ClientUtil.getPartialTick(), this.layers);
            if (this.layers.stream().noneMatch(p -> p instanceof AbilityLayerRenderer)) {
                this.addLayer(new AbilityLayerRenderer<>((LivingEntityRenderer<T, M>) (Object) this));
            }
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getWhiteOverlayProgress(Lnet/minecraft/world/entity/LivingEntity;F)F"))
    public void setup(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci, @Local(ordinal = 0) RenderType type) {
        LivingEntityRenderer<T, M> renderer = (LivingEntityRenderer<T, M>) (Object) this;
        if (this.model instanceof IHasModelProperties iModel) {
            this.modulus$event = new RendererChangeCallback.RendererChangeEvent<>(entity, renderer, iModel.modulus$modelProperties(), poseStack, buffer, type, packedLight, LivingEntityRenderer.getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTicks)));
            if (this.model instanceof HumanoidModel<?>) {
                SetupAnimCallback.EVENT.invoker().event(new SetupAnimCallback.SetupAnimEvent(entity, (HumanoidModel<T>) this.model, iModel.modulus$modelProperties()));
            }
        }
    }

    @WrapWithCondition(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V")
    )
    private boolean renderIfAllowed(EntityModel instance, PoseStack poseStack, VertexConsumer vertexConsumer, int pPackedLight, int pPackedOverlay, int pColor) {
        if (this.modulus$event != null) {
            this.modulus$event.setColor(FastColor.ARGB32.red(pColor), FastColor.ARGB32.green(pColor), FastColor.ARGB32.blue(pColor), FastColor.ARGB32.alpha(pColor));
            boolean b = RendererChangeCallback.EVENT.invoker().event(this.modulus$event);
            this.modulus$event.multiBufferSource().getBuffer(this.modulus$event.renderType()); // rollback texture of entity
            return !b;
        }
        return true;
    }
}
