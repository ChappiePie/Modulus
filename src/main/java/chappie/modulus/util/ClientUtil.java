package chappie.modulus.util;

import chappie.modulus.Modulus;
import chappie.modulus.util.model.IChangeableSize;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;

import java.util.function.BiConsumer;

public class ClientUtil {

    private static final ResourceLocation BOLD_FONT_LOCATION = Modulus.id("bold");
    private static final FontDescription BOLD_FONT = new FontDescription.Resource(BOLD_FONT_LOCATION);
    public static final Style BOLD_MINECRAFT = Style.EMPTY.withFont(BOLD_FONT);

    public static float getPartialTick() {
        return Minecraft.getInstance().isPaused() ? 0 : Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation atlasLocation, float x, float y, float uOffset, float vOffset, float uWidth, float vHeight, float width, float height, float textureWidth, float textureHeight, int color) {
        float x1 = x + uWidth;
        float y1 = y + vHeight;
        float u0 = uOffset / textureWidth;
        float u1 = (uOffset + width) / textureWidth;
        float v0 = vOffset / textureHeight;
        float v1 = (vOffset + height) / textureHeight;

        // Submit a custom render state so floats are preserved instead of being rounded by GuiGraphics.blit.
        guiGraphics.guiRenderState.submitGuiElement(
                new FloatBlitRenderState(
                        RenderPipelines.GUI_TEXTURED,
                        TextureSetup.singleTexture(Minecraft.getInstance().getTextureManager().getTexture(atlasLocation).getTextureView()),
                        new Matrix3x2f(guiGraphics.pose()),
                        x,
                        y,
                        x1,
                        y1,
                        u0,
                        u1,
                        v0,
                        v1,
                        color,
                        guiGraphics.scissorStack.peek()
                )
        );
    }

    public static IChangeableSize modified(ModelPart part) {
        return (IChangeableSize) (Object) part;
    }

    public static void modifyAllParts(Model model, BiConsumer<ModelPart, IChangeableSize> consumer) {
        model.allParts().forEach((part) ->
                consumer.accept((ModelPart) part, modified((ModelPart) part)));
    }

    public static void renderFilledBox(Matrix4f pose, VertexConsumer builder, AABB box, float red, float green, float blue, float alpha, int combinedLightIn) {
        builder.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);

        builder.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);

        builder.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);

        builder.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);

        builder.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.maxX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.maxX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);

        builder.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.minX, (float) box.minY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.maxZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
        builder.addVertex(pose, (float) box.minX, (float) box.maxY, (float) box.minZ).setColor(red, green, blue, alpha).setLight(combinedLightIn);
    }

    private record FloatBlitRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            float x0,
            float y0,
            float x1,
            float y1,
            float u0,
            float u1,
            float v0,
            float v1,
            int color,
            @Nullable ScreenRectangle scissorArea,
            @Nullable ScreenRectangle bounds
    ) implements GuiElementRenderState {

        private FloatBlitRenderState(
                RenderPipeline pipeline,
                TextureSetup textureSetup,
                Matrix3x2f pose,
                float x0,
                float y0,
                float x1,
                float y1,
                float u0,
                float u1,
                float v0,
                float v1,
                int color,
                @Nullable ScreenRectangle scissorArea
        ) {
            this(
                    pipeline,
                    textureSetup,
                    pose,
                    x0,
                    y0,
                    x1,
                    y1,
                    u0,
                    u1,
                    v0,
                    v1,
                    color,
                    scissorArea,
                    computeBounds(x0, y0, x1, y1, pose, scissorArea)
            );
        }

        private static ScreenRectangle computeBounds(float x0, float y0, float x1, float y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea) {
            float minX = Math.min(x0, x1);
            float minY = Math.min(y0, y1);
            float maxX = Math.max(x0, x1);
            float maxY = Math.max(y0, y1);
            int left = Mth.floor(minX);
            int top = Mth.floor(minY);
            int width = Mth.ceil(maxX - minX);
            int height = Mth.ceil(maxY - minY);
            ScreenRectangle rectangle = new ScreenRectangle(left, top, width, height).transformMaxBounds(pose);
            return scissorArea != null ? scissorArea.intersection(rectangle) : rectangle;
        }

        @Override
        public void buildVertices(VertexConsumer consumer) {
            consumer.addVertexWith2DPose(this.pose(), this.x0(), this.y0()).setUv(this.u0(), this.v0()).setColor(this.color());
            consumer.addVertexWith2DPose(this.pose(), this.x0(), this.y1()).setUv(this.u0(), this.v1()).setColor(this.color());
            consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1()).setUv(this.u1(), this.v1()).setColor(this.color());
            consumer.addVertexWith2DPose(this.pose(), this.x1(), this.y0()).setUv(this.u1(), this.v0()).setColor(this.color());
        }
    }

    public static final class ModRenderTypes {

        private static final RenderPipeline LASER_PIPELINE = RenderPipelines.register(
                RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                        .withLocation(Modulus.id("pipeline/laser"))
                        .withVertexShader("core/rendertype_lightning")
                        .withFragmentShader("core/rendertype_lightning")
                        .withBlend(BlendFunction.LIGHTNING)
                        .withDepthWrite(false)
                        .withCull(false)
                        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS)
                        .build()
        );
        public static final RenderType LASER = RenderType.create(Modulus.MODID + ":laser", 256, false, true, LASER_PIPELINE, RenderType.CompositeState.builder()
                .setTextureState(RenderStateShard.NO_TEXTURE)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(true));
        private static final RenderPipeline MAIN_LASER_PIPELINE = RenderPipelines.register(
                RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                        .withLocation(Modulus.id("pipeline/main_laser"))
                        .withVertexShader("core/rendertype_lightning")
                        .withFragmentShader("core/rendertype_lightning")
                        .withBlend(BlendFunction.LIGHTNING)
                        .withCull(false)
                        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS)
                        .build()
        );
        public static final RenderType MAIN_LASER = RenderType.create(Modulus.MODID + ":main_laser", 256, false, true, MAIN_LASER_PIPELINE, RenderType.CompositeState.builder()
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(true));

        public static RenderType glow(ResourceLocation texture) {
            return RenderType.create(Modulus.MODID + ":light", 256, false, true, RenderPipelines.ENERGY_SWIRL, RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, false))
                    .setTexturingState(new RenderStateShard.OffsetTexturingStateShard(0, 0))
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .createCompositeState(false));
        }
    }
}
