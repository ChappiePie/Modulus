package chappie.modulus.mixin.client;

import chappie.modulus.client.AbilityLayerRenderer;
import chappie.modulus.util.events.RendererChangeCallback;
import chappie.modulus.util.events.SetupAnimCallback;
import chappie.modulus.util.model.IHasModelProperties;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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
    @Shadow protected M model;

    @Unique
    private RendererChangeCallback.RendererChangeEvent<T, M> modulus$event;

    @Shadow @Final protected List<RenderLayer<?, ?>> layers;

    @Shadow
    protected abstract boolean addLayer(RenderLayer<T, M> p_115327_);

    @Inject(method = "render*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V"))
    public void setupModelProperties(T entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light, CallbackInfo ci, @Local(ordinal = 8) float f5, @Local(ordinal = 7) float f8, @Local(ordinal = 6) float f7, @Local(ordinal = 2) float f2, @Local(ordinal = 4) float f6) {
        if (this.model instanceof IHasModelProperties iModel) {
            iModel.setup(f5, f8, f7, f2, f6, partialTicks, this.layers);
            if (this.layers.stream().noneMatch(p -> p instanceof AbilityLayerRenderer)) {
                this.addLayer(new AbilityLayerRenderer<>((LivingEntityRenderer<T, M>) (Object) this));
            }

        }
    }

    @Shadow
    protected abstract float getWhiteOverlayProgress(T livingEntity, float partialTicks);

    @Inject(method = "render*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getOverlayCoords(Lnet/minecraft/world/entity/LivingEntity;F)I"))
    public void setup(T entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light, CallbackInfo ci, @Local(ordinal = 0) RenderType type) {
        LivingEntityRenderer<T, M> renderer = (LivingEntityRenderer<T, M>) (Object) this;
        if (this.model instanceof IHasModelProperties iModel) {
            this.modulus$event = new RendererChangeCallback.RendererChangeEvent<>(entity, renderer, iModel.modelProperties(), matrixStack, buffer, type, light, LivingEntityRenderer.getOverlayCoords(entity, this.getWhiteOverlayProgress(entity, partialTicks)));
            if (this.model instanceof HumanoidModel<?> humanoidModel) {
                SetupAnimCallback.EVENT.invoker().event(new SetupAnimCallback.SetupAnimEvent(entity, (HumanoidModel<T>) this.model, iModel.modelProperties()));
                // Copy angles to wear
                humanoidModel.hat.copyFrom(humanoidModel.head);
                if (humanoidModel instanceof PlayerModel playerModel) {
                    playerModel.jacket.copyFrom(humanoidModel.body);
                    playerModel.rightSleeve.copyFrom(humanoidModel.rightArm);
                    playerModel.leftSleeve.copyFrom(humanoidModel.leftArm);
                    playerModel.leftPants.copyFrom(humanoidModel.leftLeg);
                    playerModel.rightPants.copyFrom(humanoidModel.rightLeg);
                }
            }
        }
    }

    @WrapWithCondition(
            method = "render*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V")
    )
    private boolean renderIfAllowed(M model, PoseStack pPoseStack, VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, float pRed, float pGreen, float pBlue, float pAlpha) {
        if (this.modulus$event != null) {
            this.modulus$event.setColor(pRed, pGreen, pBlue, pAlpha);
            boolean b = RendererChangeCallback.EVENT.invoker().event(this.modulus$event);
            this.modulus$event.multiBufferSource().getBuffer(this.modulus$event.renderType()); // rollback texture of entity
            return !b;
        }
        return true;
    }
}
