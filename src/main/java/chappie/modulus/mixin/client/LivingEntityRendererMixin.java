package chappie.modulus.mixin.client;

import chappie.modulus.common.capability.anim.PlayerAnimCap;
import chappie.modulus.client.model.anim.PlayerGeoModel;
import chappie.modulus.client.AbilityLayerRenderer;
import chappie.modulus.util.PlayerPart;
import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.events.RendererChangeEvent;
import chappie.modulus.util.events.SetupAnimEvent;
import com.google.common.collect.Iterables;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.keyframe.BoneAnimation;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Shadow protected M model;

    @Shadow public abstract boolean addLayer(RenderLayer<T, M> p_115327_);

    @Shadow @Final protected List<RenderLayer<?, ?>> layers;

    @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Lnet/minecraft/client/model/EntityModel;F)V", at = @At("TAIL"))
    public void mixinInit(EntityRendererProvider.Context context, M model, float shadowSize, CallbackInfo ci) {
        if (model instanceof IHasModelProperties) {
            this.addLayer(new AbilityLayerRenderer<>(context, (LivingEntityRenderer<T, M>) (Object) this));
        }
    }

    @Inject(method = "render*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V"))
    public void setupModelProperties(T entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light, CallbackInfo ci, @Local(ordinal = 8) float f5, @Local(ordinal = 7) float f8, @Local(ordinal = 6) float f7, @Local(ordinal = 2) float f2, @Local(ordinal = 4) float f6) {
        if (this.model instanceof IHasModelProperties iModel) {
            iModel.setup(f5, f8, f7, f2, f6, partialTicks, this.layers);
        }
    }

    @Unique private RendererChangeEvent<T, M> modulus$event;

    @Inject(method = "render*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    public void setup(T entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int light, CallbackInfo ci, @Local(ordinal = 0) RenderType type, @Local(ordinal = 1) int overlay) {
        LivingEntityRenderer<T, M> renderer = (LivingEntityRenderer<T, M>) (Object) this;
        if (this.model instanceof IHasModelProperties iModel) {
            this.modulus$event = new RendererChangeEvent<>(entity, renderer, iModel.modelProperties(), matrixStack, buffer, type, light, overlay);
            if (this.model instanceof HumanoidModel<?> humanoidModel) {
                MinecraftForge.EVENT_BUS.post(new SetupAnimEvent<>(entity, (HumanoidModel<T>) this.model, iModel.modelProperties()));
                if (entity instanceof Player player && humanoidModel instanceof PlayerModel<?> playerModel) {
                    entity.getCapability(PlayerAnimCap.CAPABILITY).ifPresent(cap -> {
                        {
                            cap.getFPAnimatedModel().getBakedModel(cap.getFPAnimatedModel().getModelResource(cap));
                            AnimationState<PlayerAnimCap> animationState = new AnimationState<>(cap, iModel.modelProperties().limbSwing(), iModel.modelProperties().limbSwingAmount(), partialTicks, false);
                            long instanceId = player.getUUID().hashCode() + "first_person".hashCode();

                            animationState.setData(DataTickets.TICK, cap.getTick(player));
                            animationState.setData(DataTickets.ENTITY, player);
                            animationState.setData(PlayerGeoModel.PLAYER_MODEL_DATA, playerModel);
                            cap.getFPAnimatedModel().addAdditionalStateData(cap, instanceId, animationState::setData);
                            cap.getFPAnimatedModel().handleAnimations(cap, instanceId, animationState);
                        }

                        cap.getAnimatedModel().getBakedModel(cap.getAnimatedModel().getModelResource(cap));
                        AnimationState<PlayerAnimCap> animationState = new AnimationState<>(cap, iModel.modelProperties().limbSwing(), iModel.modelProperties().limbSwingAmount(), partialTicks, false);
                        long instanceId = player.getUUID().hashCode();

                        animationState.setData(DataTickets.TICK, cap.getTick(player));
                        animationState.setData(DataTickets.ENTITY, player);
                        animationState.setData(PlayerGeoModel.PLAYER_MODEL_DATA, playerModel);
                        cap.getAnimatedModel().addAdditionalStateData(cap, instanceId, animationState::setData);
                        cap.getAnimatedModel().handleAnimations(cap, instanceId, animationState);

                        for (AnimationController<?> controller : cap.getAnimatableInstanceCache().getManagerForId(instanceId).getAnimationControllers().values()) {
                            if (controller.getName().contains("first_person")) continue;
                            if (controller.getCurrentAnimation() != null && controller.getAnimationState() != AnimationController.State.STOPPED) {
                                for (String s : Iterables.concat(PlayerPart.bodyParts().stream().map(p ->
                                        p.name().toLowerCase()).toList(), Collections.singleton("player"))) {
                                    cap.getAnimatedModel().getBone(s).ifPresent(bone -> {
                                        for (BoneAnimation boneAnimation : controller.getCurrentAnimation().animation().boneAnimations()) {
                                            if (boneAnimation.boneName().equals(s)) {
                                                if (s.equals("player")) {
                                                    RenderUtils.prepMatrixForBone(matrixStack, bone);
                                                    break;
                                                }
                                                PlayerGeoModel.setupPlayerBones(bone, PlayerPart.byName(s).modelPart(playerModel), true);
                                            }
                                        }
                                    });
                                }

                            }
                        }
                    });
                }

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
            MinecraftForge.EVENT_BUS.post(this.modulus$event);
            this.modulus$event.multiBufferSource().getBuffer(this.modulus$event.renderType()); // rollback texture of entity
            return !this.modulus$event.isCanceled();
        }
        return true;
    }
}
