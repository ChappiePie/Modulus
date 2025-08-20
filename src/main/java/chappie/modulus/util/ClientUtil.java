package chappie.modulus.util;

import chappie.modulus.Modulus;
import chappie.modulus.util.model.IChangeableSize;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

import java.util.function.BiConsumer;

public class ClientUtil {

    private static final ResourceLocation BOLD_FONT = ResourceLocation.fromNamespaceAndPath("modulus", "bold");
    public static final Style BOLD_MINECRAFT = Style.EMPTY.withFont(BOLD_FONT);

    public static float getPartialTick() {
        return Minecraft.getInstance().isPaused() ? 0 : Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    public static void renderTextureOverlay(ResourceLocation resourceLocation, int height, int width, float red, float green, float blue, float alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        //RenderSystem.setShaderColor(red, green, blue, alpha);
        RenderSystem.setShaderTexture(0, resourceLocation);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(0.0F, height, -90.0F).setUv(0.0F, 1.0F);
        bufferbuilder.addVertex(width, height, -90.0F).setUv(1.0F, 1.0F);
        bufferbuilder.addVertex(width, 0.0F, -90.0F).setUv(1.0F, 0.0F);
        bufferbuilder.addVertex(0.0F, 0.0F, -90.0F).setUv(0.0F, 0.0F);
        MeshData meshData = bufferbuilder.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void blit(
            GuiGraphics guiGraphics,
            ResourceLocation atlasLocation,
            float x,
            float y,
            float uOffset,
            float vOffset,
            float uWidth,
            float vHeight,
            float width,
            float height,
            float textureWidth,
            float textureHeight
    ) {
        ClientUtil.blit(guiGraphics, atlasLocation, x, y, uOffset, vOffset, uWidth, vHeight, width, height, textureWidth, textureHeight, -1);
    }

    public static void blit(
            GuiGraphics guiGraphics,
            ResourceLocation atlasLocation,
            float x,
            float y,
            float uOffset,
            float vOffset,
            float uWidth,
            float vHeight,
            float width,
            float height,
            float textureWidth,
            float textureHeight,
            int color
    ) {
        ClientUtil.innerBlit(
                guiGraphics,
                atlasLocation,
                x,
                x + uWidth,
                y,
                y + vHeight,
                (uOffset + 0.0F) / textureWidth,
                (uOffset + width) / textureWidth,
                (vOffset + 0.0F) / textureHeight,
                (vOffset + height) / textureHeight,
                color
        );
    }

    private static void innerBlit(
            GuiGraphics guiGraphics,
            ResourceLocation atlasLocation,
            float x1,
            float x2,
            float y1,
            float y2,
            float minU,
            float maxU,
            float minV,
            float maxV,
            int color
    ) {
        RenderType renderType = RenderType.guiTextured(atlasLocation);
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        VertexConsumer vertexConsumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(renderType);
        vertexConsumer.addVertex(matrix4f, x1, y1, 0.0F).setUv(minU, minV).setColor(color);
        vertexConsumer.addVertex(matrix4f, x1, y2, 0.0F).setUv(minU, maxV).setColor(color);
        vertexConsumer.addVertex(matrix4f, x2, y2, 0.0F).setUv(maxU, maxV).setColor(color);
        vertexConsumer.addVertex(matrix4f, x2, y1, 0.0F).setUv(maxU, minV).setColor(color);
    }

    public static IChangeableSize modified(ModelPart part) {
        return (IChangeableSize) (Object) part;
    }

    public static void modifyAllParts(Model model, BiConsumer<ModelPart, IChangeableSize> consumer) {
        model.allParts().forEach((part) ->
                consumer.accept(part, modified(part)));
    }

    public static void renderFilledBox(PoseStack matrixStack, VertexConsumer builder, AABB box, float red, float green, float blue, float alpha, int combinedLightIn) {
        Matrix4f poseStack = matrixStack.last().pose();
        builder.addVertex(poseStack, (float) box.minX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.minX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.maxX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.maxX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);

        builder.addVertex(poseStack, (float) box.minX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.maxX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.maxX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.minX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);

        builder.addVertex(poseStack, (float) box.minX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.minX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.maxX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.maxX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);

        builder.addVertex(poseStack, (float) box.minX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.maxX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.maxX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.minX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);

        builder.addVertex(poseStack, (float) box.maxX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.maxX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.maxX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.maxX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);

        builder.addVertex(poseStack, (float) box.minX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.minX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.minX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(poseStack, (float) box.minX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
    }

    public static class ModRenderTypes extends RenderType {

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

        public ModRenderTypes(String nameIn, VertexFormat formatIn, VertexFormat.Mode drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
            super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
        }

        public static RenderType glow(ResourceLocation texture) {
            return create(Modulus.MODID + ":light", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, CompositeState.builder().setShaderState(RENDERTYPE_ENERGY_SWIRL_SHADER)
                    .setTextureState(new TextureStateShard(texture, TriState.DEFAULT, false))
                    .setTexturingState(new OffsetTexturingStateShard(0, 0))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
                    .createCompositeState(false));
        }
    }

    public static class ARGB32 {
        public static int color(int alpha, int red, int green, int blue) {
            return alpha << 24 | red << 16 | green << 8 | blue;
        }
    }
}