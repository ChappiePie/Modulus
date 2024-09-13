package chappie.modulus.util;

import chappie.modulus.Modulus;
import chappie.modulus.client.model.anim.FPPlayerGeoModel;
import chappie.modulus.client.model.anim.PlayerGeoModel;
import chappie.modulus.common.capability.anim.PlayerAnimCap;
import chappie.modulus.util.model.IChangeableSize;
import chappie.modulus.util.model.IHasModelProperties;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.keyframe.BoneAnimation;
import software.bernie.geckolib.util.RenderUtils;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ClientUtil {

    private static final ResourceLocation BOLD_FONT = new ResourceLocation("modulus", "bold");
    public static final Style BOLD_MINECRAFT = Style.EMPTY.withFont(BOLD_FONT);

    public static float getPartialTick() {
        return Minecraft.getInstance().isPaused() ? 0 : Minecraft.getInstance().getFrameTime();
    }

    public static void blit(GuiGraphics guiGraphics, float pX, float pY, float pWidth, float pHeight, float pUOffset, float pVOffset, int pUWidth, int pVHeight, int pTextureWidth, int pTextureHeight) {
        blit(guiGraphics.pose(), pX, pX + pWidth, pY, pY + pHeight, 0, pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight);
    }

    private static void blit(PoseStack p_93188_, float p_93189_, float p_93190_, float p_93191_, float p_93192_, float p_93193_, int p_93194_, int p_93195_, float p_93196_, float p_93197_, int p_93198_, int p_93199_) {
        innerBlit(p_93188_.last().pose(), p_93189_, p_93190_, p_93191_, p_93192_, p_93193_, (p_93196_ + 0.0F) / (float) p_93198_, (p_93196_ + (float) p_93194_) / (float) p_93198_, (p_93197_ + 0.0F) / (float) p_93199_, (p_93197_ + (float) p_93195_) / (float) p_93199_);
    }

    private static void innerBlit(Matrix4f pMatrix, float pX1, float pX2, float pY1, float pY2, float pBlitOffset, float pMinU, float pMaxU, float pMinV, float pMaxV) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(pMatrix, pX1, pY1, pBlitOffset).uv(pMinU, pMinV).endVertex();
        bufferbuilder.vertex(pMatrix, pX1, pY2, pBlitOffset).uv(pMinU, pMaxV).endVertex();
        bufferbuilder.vertex(pMatrix, pX2, pY2, pBlitOffset).uv(pMaxU, pMaxV).endVertex();
        bufferbuilder.vertex(pMatrix, pX2, pY1, pBlitOffset).uv(pMaxU, pMinV).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }

    public static void modifyAllParts(Model model, BiConsumer<ModelPart, IChangeableSize> consumer) {
        if (model instanceof IHasModelProperties modelProperties) {
            modelProperties.allParts().forEach((part) ->
                    consumer.accept(part, modified(part)));
        }
    }

    public static IChangeableSize modified(ModelPart part) {
        return (IChangeableSize) (Object) part;
    }

    public static void renderFilledBox(PoseStack matrixStack, VertexConsumer builder, AABB box, float red, float green, float blue, float alpha, int combinedLightIn) {
        Matrix4f poseStack = matrixStack.last().pose();
        builder.vertex(poseStack, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();

        builder.vertex(poseStack, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();

        builder.vertex(poseStack, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();

        builder.vertex(poseStack, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();

        builder.vertex(poseStack, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();

        builder.vertex(poseStack, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
        builder.vertex(poseStack, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).uv2(combinedLightIn).endVertex();
    }

    public static Supplier<ResourceLocation> getTextureFromLink(ResourceLocation location, String name, String url) {
        ResourceLocation resourcelocation = new ResourceLocation(location.getNamespace(), location.getPath() + "/" + name);
        AbstractTexture abstracttexture = Minecraft.getInstance().getTextureManager().getTexture(resourcelocation, MissingTextureAtlasSprite.getTexture());
        if (abstracttexture == MissingTextureAtlasSprite.getTexture()) {
            File file = new File("config/modulus/data", name);
            HttpTexture httptexture = new HttpTexture(file, url, Modulus.id("textures/gui/white.png"), false, () -> {
            });
            Minecraft.getInstance().getTextureManager().register(resourcelocation, httptexture);
        }
        return () -> resourcelocation;
    }

    public static boolean rotationFromAnimation(PlayerAnimCap cap, ItemInHandRenderer instance, AbstractClientPlayer player, float pPartialTicks, float pPitch, InteractionHand pHand, AtomicReference<Float> pSwingProgress, ItemStack pStack, AtomicReference<Float> pEquippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight) {
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

    public static class ModRenderTypes extends RenderType {

        public ModRenderTypes(String nameIn, VertexFormat formatIn, VertexFormat.Mode drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
            super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
        }

        public static final RenderType LASER = create(Modulus.MODID + ":laser", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true, CompositeState.builder()
                .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                .setTextureState(NO_TEXTURE)
                .setCullState(NO_CULL)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .setLightmapState(LIGHTMAP)
                .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(true));

        public static final RenderType MAIN_LASER = create(Modulus.MODID + ":main_laser", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true, CompositeState.builder()
                .setShaderState(RENDERTYPE_LIGHTNING_SHADER)
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(true));

        public static RenderType glow(ResourceLocation texture) {
            return create(Modulus.MODID + ":light", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, CompositeState.builder().setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setTexturingState(new OffsetTexturingStateShard(0, 0))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
                    .createCompositeState(false));
        }
    }
}