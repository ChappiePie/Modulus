package chappie.playeranim;

import chappie.modulus.util.PlayerPart;
import chappie.modulus.util.model.IHasModelProperties;
import chappie.playeranim.capability.PlayerAnimCap;
import chappie.playeranim.model.FPPlayerGeoModel;
import chappie.playeranim.model.PlayerGeoModel;
import chappie.playeranim.networking.ClientTriggerPlayerAnim;
import com.google.common.collect.Iterables;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.keyframe.BoneAnimation;
import software.bernie.geckolib.util.RenderUtils;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerAnimationUtil {
    protected static final boolean INITIALIZE = false;

    public static boolean initialized() {
        return INITIALIZE;
    }

    public static boolean rotationInFirst(ItemInHandRenderer instance, AbstractClientPlayer player, float pPartialTicks, float pPitch, InteractionHand pHand, AtomicReference<Float> pSwingProgress, ItemStack pStack, AtomicReference<Float> pEquippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight) {
        PlayerAnimCap cap = PlayerAnimCap.getCap(player);
        FPPlayerGeoModel model = cap.getFPAnimatedModel();
        var m = model.getBakedModel(model.getModelResource(cap));
        PlayerModel<?> playerModel = ((PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player)).getModel();
        if (!(playerModel instanceof IHasModelProperties iModel)) return false;
        // third person
        long instanceId = player.getUUID().hashCode();
        {
            cap.getAnimatedModel().getBakedModel(cap.getAnimatedModel().getModelResource(cap));
            AnimationState<PlayerAnimCap> animationState = new AnimationState<>(cap, iModel.modelProperties().limbSwing(), iModel.modelProperties().limbSwingAmount(), pPartialTicks, false);

            animationState.setData(DataTickets.TICK, cap.getTick(player));
            animationState.setData(DataTickets.ENTITY, player);
            animationState.setData(PlayerGeoModel.PLAYER_MODEL_DATA, playerModel);
            cap.getAnimatedModel().addAdditionalStateData(cap, instanceId, animationState::setData);
            cap.getAnimatedModel().handleAnimations(cap, instanceId, animationState);
        }


        AnimationState<PlayerAnimCap> animationState = new AnimationState<>(cap, iModel.modelProperties().limbSwing(), iModel.modelProperties().limbSwingAmount(), pPartialTicks, false);
        instanceId += "first_person".hashCode();

        animationState.setData(DataTickets.TICK, cap.getTick(player));
        animationState.setData(DataTickets.ENTITY, player);
        animationState.setData(PlayerGeoModel.PLAYER_MODEL_DATA, playerModel);

        boolean renderArm = false;
        boolean flag = pHand == InteractionHand.MAIN_HAND;
        HumanoidArm handside = flag ? player.getMainArm() : player.getMainArm().getOpposite();
        boolean mirror = player.getMainArm() == HumanoidArm.LEFT;

        if (flag) {
            model.addAdditionalStateData(cap, instanceId, animationState::setData);
            model.handleAnimations(cap, instanceId, animationState);

            pMatrixStack.translate(0, -2, -1);
            for (GeoBone registeredBone : m.topLevelBones()) {
                rotateBoneRecursively(registeredBone, mirror, pMatrixStack);
            }
            pMatrixStack.translate(0, 2, 1);
        }
        int i = handside == HumanoidArm.RIGHT ? -1 : 1;
        if (mirror)
            handside = handside.getOpposite();
        String sideName = handside == HumanoidArm.RIGHT ? "right" : "left";

        for (AnimationController<?> controller : cap.getAnimatableInstanceCache().getManagerForId(instanceId).getAnimationControllers().values()) {
            if (!controller.getName().contains("first_person")) continue;
            if (controller.getCurrentAnimation() != null && controller.getAnimationState() != AnimationController.State.STOPPED) {

                for (BoneAnimation boneAnimation : controller.getCurrentAnimation().animation().boneAnimations()) {
                    if (boneAnimation.boneName().equals("fixed_pitch_" + sideName)) {
                        float fixedPitch = model.getBone("fixed_pitch_" + sideName).orElseThrow().getPosX();
                        pMatrixStack.mulPose(Axis.XP.rotationDegrees(pPitch * fixedPitch));
                    } else if (boneAnimation.boneName().equals("swing_progress_" + sideName)) {
                        float swingProgress = model.getBone("swing_progress_" + sideName).orElseThrow().getPosX();
                        pSwingProgress.set(swingProgress);
                    } else if (boneAnimation.boneName().equals("equipped_progress_" + sideName)) {
                        float equippedProgress = model.getBone("equipped_progress_" + sideName).orElseThrow().getPosX();
                        pEquippedProgress.set(equippedProgress);
                    } else if (boneAnimation.boneName().equals("render_arm_" + sideName)) {
                        renderArm = model.getBone("render_arm_" + sideName).orElseThrow().getPosX() == 1;
                    }
                }
            }
        }
        GeoBone bone = model.getBone(sideName + "_arm").orElseThrow();
        pMatrixStack.last().normal().mul(bone.getWorldSpaceNormal());
        pMatrixStack.last().pose().mul(bone.getWorldSpaceMatrix());
        pMatrixStack.translate(i * 0.547, 0.7655, 0.625);
        return renderArm;
    }

    public static void rotateBoneRecursively(GeoBone bone, boolean mirror, PoseStack poseStack) {
        poseStack.pushPose();
        if (mirror) {
            poseStack.translate(bone.getPosX() / 16.0F, bone.getPosY() / 16.0F, (double) (bone.getPosZ() / 16.0F));
            poseStack.translate(-bone.getPivotX() / 16.0F, bone.getPivotY() / 16.0F, (double) (bone.getPivotZ() / 16.0F));
            if (bone.getRotZ() != 0.0F) {
                poseStack.mulPose(Axis.ZP.rotation(-bone.getRotZ()));
            }
            if (bone.getRotY() != 0.0F) {
                poseStack.mulPose(Axis.YP.rotation(-bone.getRotY()));
            }
            if (bone.getRotX() != 0.0F) {
                poseStack.mulPose(Axis.XP.rotation(bone.getRotX()));
            }
            RenderUtils.scaleMatrixForBone(poseStack, bone);
        } else {
            RenderUtils.translateMatrixToBone(poseStack, bone);
            RenderUtils.translateToPivotPoint(poseStack, bone);
            RenderUtils.rotateMatrixAroundBone(poseStack, bone);
            RenderUtils.scaleMatrixForBone(poseStack, bone);
        }

        poseStack.pushPose();
        PoseStack.Pose entry = poseStack.last();
        bone.setWorldSpaceNormal(new Matrix3f(entry.normal()));
        bone.setWorldSpaceMatrix(new Matrix4f(entry.pose()));
        poseStack.popPose();

        if (mirror) {
            poseStack.translate(bone.getPivotX() / 16.0F, -bone.getPivotY() / 16.0F, (double) (-bone.getPivotZ() / 16.0F));
        } else {
            RenderUtils.translateAwayFromPivotPoint(poseStack, bone);
        }
        if (!bone.isHidingChildren()) {
            for (GeoBone childBone : bone.getChildBones()) {
                rotateBoneRecursively(childBone, mirror, poseStack);
            }
        }
        poseStack.popPose();
    }

    public static void rotationInThird(float partialTicks, PoseStack matrixStack, IHasModelProperties iModel, Player player, PlayerModel<?> playerModel) {
        PlayerAnimCap cap = PlayerAnimCap.getCap(player);
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
    }

    public static void registerClientMessages() {
        ClientPlayNetworking.registerGlobalReceiver(ClientTriggerPlayerAnim.PACKET, ClientTriggerPlayerAnim::handle);
    }
}
