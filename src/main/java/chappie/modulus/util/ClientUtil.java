package chappie.modulus.util;

import chappie.modulus.Modulus;
import chappie.modulus.util.model.IChangeableSize;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
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

    public static void blit(GuiGraphics guiGraphics, ResourceLocation atlasLocation, float x, float y, float uOffset, float vOffset, float uWidth, float vHeight, float width, float height, float textureWidth, float textureHeight, int color) {
        ClientUtil.innerBlit(guiGraphics, atlasLocation, x, x + uWidth, y, y + vHeight, (uOffset + 0.0F) / textureWidth, (uOffset + width) / textureWidth, (vOffset + 0.0F) / textureHeight, (vOffset + height) / textureHeight, color);
    }

    private static void innerBlit(GuiGraphics guiGraphics, ResourceLocation atlasLocation, float x1, float x2, float y1, float y2, float minU, float maxU, float minV, float maxV, int color) {
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
}