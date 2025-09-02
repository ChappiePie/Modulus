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
import net.minecraft.util.Mth;
import net.minecraft.util.TriState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

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
        public static int alpha(int color) {
            return color >>> 24;
        }

        public static int red(int color) {
            return color >> 16 & 0xFF;
        }

        public static int green(int color) {
            return color >> 8 & 0xFF;
        }

        public static int blue(int color) {
            return color & 0xFF;
        }

        public static int color(int alpha, int red, int green, int blue) {
            return alpha << 24 | red << 16 | green << 8 | blue;
        }

        public static int color(int red, int green, int blue) {
            return color(255, red, green, blue);
        }

        public static int color(Vec3 color) {
            return color(as8BitChannel((float) color.x()), as8BitChannel((float) color.y()), as8BitChannel((float) color.z()));
        }

        public static int multiply(int color1, int color2) {
            if (color1 == -1) {
                return color2;
            } else {
                return color2 == -1
                        ? color1
                        : color(alpha(color1) * alpha(color2) / 255, red(color1) * red(color2) / 255, green(color1) * green(color2) / 255, blue(color1) * blue(color2) / 255);
            }
        }

        public static int scaleRGB(int color, float scale) {
            return scaleRGB(color, scale, scale, scale);
        }

        public static int scaleRGB(int color, float redScale, float greenScale, float blueScale) {
            return color(
                    alpha(color),
                    Math.clamp((int) ((float) red(color) * redScale), 0, 255),
                    Math.clamp((int) ((float) green(color) * greenScale), 0, 255),
                    Math.clamp((int) ((float) blue(color) * blueScale), 0, 255)
            );
        }

        public static int scaleRGB(int color, int scale) {
            return color(
                    alpha(color),
                    Math.clamp((long) red(color) * (long) scale / 255L, 0, 255),
                    Math.clamp((long) green(color) * (long) scale / 255L, 0, 255),
                    Math.clamp((long) blue(color) * (long) scale / 255L, 0, 255)
            );
        }

        public static int greyscale(int color) {
            int i = (int) ((float) red(color) * 0.3F + (float) green(color) * 0.59F + (float) blue(color) * 0.11F);
            return color(i, i, i);
        }

        public static int lerp(float delta, int color1, int color2) {
            int i = Mth.lerpInt(delta, alpha(color1), alpha(color2));
            int j = Mth.lerpInt(delta, red(color1), red(color2));
            int k = Mth.lerpInt(delta, green(color1), green(color2));
            int l = Mth.lerpInt(delta, blue(color1), blue(color2));
            return color(i, j, k, l);
        }

        public static int opaque(int color) {
            return color | 0xFF000000;
        }

        public static int transparent(int color) {
            return color & 16777215;
        }

        public static int color(int alpha, int color) {
            return alpha << 24 | color & 16777215;
        }

        public static int white(float alpha) {
            return as8BitChannel(alpha) << 24 | 16777215;
        }

        public static int colorFromFloat(float alpha, float red, float green, float blue) {
            return color(as8BitChannel(alpha), as8BitChannel(red), as8BitChannel(green), as8BitChannel(blue));
        }

        public static Vector3f vector3fFromRGB24(int color) {
            float f = (float) red(color) / 255.0F;
            float g = (float) green(color) / 255.0F;
            float h = (float) blue(color) / 255.0F;
            return new Vector3f(f, g, h);
        }

        public static int average(int color1, int color2) {
            return color((alpha(color1) + alpha(color2)) / 2, (red(color1) + red(color2)) / 2, (green(color1) + green(color2)) / 2, (blue(color1) + blue(color2)) / 2);
        }

        public static int as8BitChannel(float value) {
            return Mth.floor(value * 255.0F);
        }

        public static float alphaFloat(int color) {
            return from8BitChannel(alpha(color));
        }

        public static float redFloat(int color) {
            return from8BitChannel(red(color));
        }

        public static float greenFloat(int color) {
            return from8BitChannel(green(color));
        }

        public static float blueFloat(int color) {
            return from8BitChannel(blue(color));
        }

        private static float from8BitChannel(int value) {
            return (float) value / 255.0F;
        }

        public static int toABGR(int color) {
            return color & -16711936 | (color & 0xFF0000) >> 16 | (color & 0xFF) << 16;
        }

        public static int fromABGR(int color) {
            return toABGR(color);
        }
    }
}