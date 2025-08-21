package chappie.modulus.client.gui;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ChappModListWidget extends ContainerObjectSelectionList<ChappModListWidget.ChappEntry> {

    public static final Map<String, Consumer<ChappEntry>> MOD_CLICKED = Maps.newHashMap();
    private static final Supplier<JsonObject> JSON_LIST = CommonUtil.getJsonFromLink("https://raw.githubusercontent.com/ChappiePie/ModulusResources/main/modList.json");

    private final int listWidth;

    private final ModulusMainScreen parent;

    public ChappModListWidget(ModulusMainScreen parent, int listWidth, int top, int bottom) {
        super(Minecraft.getInstance(), listWidth, bottom - top, top, bottom, 110);
        this.parent = parent;
        this.listWidth = listWidth;
        this.refreshList();
        this.setRenderBackground(false);
        this.setRenderTopAndBottom(false);
    }

    @Override
    protected int getScrollbarPosition() {
        return this.listWidth;
    }

    @Override
    public int getRowWidth() {
        return this.listWidth - 20;
    }

    public void refreshList() {
        this.clearEntries();
        for (Map.Entry<String, JsonElement> e : JSON_LIST.get().entrySet()) {
            if (e.getValue() instanceof JsonObject json) {
                this.addEntry(new ChappEntry(new ChappModInfo(e.getKey(),
                        json.get("version").getAsString(),
                        json.get("url").getAsString(),
                        CommonUtil.parseDescriptionLines(json.get("description")),
                        json.has("textureId") ? json.get("textureId").getAsString() : e.getKey(),
                        DEFAULT_TEXT), this.parent));
            }
        }
    }

    @Override
    protected void renderItem(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick, int pIndex, int pLeft, int pTop, int pWidth, int pHeight) {
        int colorIn = FastColor.ARGB32.color(150, 0, 0, 0);
        int colorOut = FastColor.ARGB32.color(50, 255, 255, 255);
        this.renderSelection(guiGraphics, pTop, pWidth, pHeight - 8, colorOut, colorIn);
        super.renderItem(guiGraphics, pMouseX, pMouseY, pPartialTick, pIndex, pLeft, pTop, pWidth, pHeight);
    }

    public void tick() {
        if (JSON_LIST.get().size() != 0 && this.children().isEmpty()) {
            this.refreshList();
        }
    }

    private final TextRenderable DEFAULT_TEXT = (entry, font, x, guiGraphics,  entryIdx, top, left, entryWidth, entryHeight, mouseX, mouseY, isHovered, partialTick) -> {
        boolean b = entryIdx % 2 == 0;
        int initX = x.get();
        x.set(initX + (b ? left + 128 : left - 12) + 6);

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        Component modName = Component.literal(entry.modInfo.modInfo() == null ? entry.modInfo.modId : entry.modInfo.modInfo().getName()).withStyle(ClientUtil.BOLD_MINECRAFT);
        float f = 2.5F;
        poseStack.scale(f, f, f);
        poseStack.translate(b ? (x.get() + 5) / f : (entryWidth - 128) / f - font.width(modName), (top + 5) / f, 0);
        guiGraphics.drawString(font,  modName, 0, 0, -1, true);
        poseStack.popPose();

        MultiLineLabel label = MultiLineLabel.create(font, entry.modInfo.modInfo() == null ? FormattedText.composite(entry.modInfo.text) : Component.literal(entry.modInfo.modInfo().getDescription()), entryWidth - 160, 7);

        poseStack.pushPose();
        int y = top + 27;
        guiGraphics.fill( x.get() + 4, y, x.get() + 10 + label.getWidth(), y + 2, -1);
        y += 4;
        label.renderLeftAligned(guiGraphics,  x.get() + 8, y, font.lineHeight, 0xFFFFFF);
        int newX = b ? x.get() + 4 : x.get() + 10 + label.getWidth();
        int yMax = y + font.lineHeight * label.getLineCount() + 3;
        guiGraphics.fill( newX, y - 4, newX + 2, yMax, -1);
        poseStack.popPose();
        x.set(initX + (b ? left : entryWidth - 128));
    };

    public static class MyButton extends Button {

        private final Vec2 oldSize;

        public MyButton(int pWidth, int pHeight, Component pMessage, OnPress pOnPress) {
            this(4, 0, pWidth, pHeight, pMessage, pOnPress);
        }

        public MyButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress) {
            super(pX, pY, pWidth, pHeight, pMessage, pOnPress, Button.DEFAULT_NARRATION);
            this.oldSize = new Vec2(this.getWidth(), this.getHeight());
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
            //super.renderWidget(guiGraphics, pMouseX, pMouseY, pPartialTick);
            PoseStack pPoseStack = guiGraphics.pose();

            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

            pPoseStack.pushPose();
            float f = 0.75F;
            pPoseStack.scale(f, f, 1.0F);
            f = 1.0F / f;
            pPoseStack.translate(this.getX() * f, this.getY() * f, 0);
            guiGraphics.blit(ModulusMainScreen.MENU, 0, 0, (int) this.oldSize.x, (int) this.oldSize.y, 196, this.getTextureY(), 60, 20, 256, 256);

            this.setWidth((int) ((this.oldSize.x - 5) / f));
            this.height = (int) (this.oldSize.y / f);
            pPoseStack.popPose();
            //guiGraphics.fill(this.getX() + 2, this.getY(), this.getX() + 2 + this.width, this.getY() + this.height, -1);

            pPoseStack.pushPose();
            f = 0.6F;
            pPoseStack.scale(f, f, 1.0F);
            f = 1.0F / f;
            pPoseStack.translate(this.getX() * f, this.getY() * f, 0);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

            int i = Minecraft.getInstance().font.width(this.getMessage());
            float j = 0;
            if (i > this.getWidth() * f) {
                j = Mth.sin((float)(Util.getMillis() % 10000L) / 10000.0F * ((float)Math.PI * 2F)) / 2F;
                guiGraphics.enableScissor(this.getX() + 2, this.getY(), this.getX() + this.getWidth() + 2, this.getY() + this.getHeight());
            }
            guiGraphics.drawCenteredString(Minecraft.getInstance().font, this.getMessage(), (int) (43 + j * i * 0.7), 6, 10526880 | Mth.ceil(1 * 255.0F) << 24);
            if (i > this.getWidth() * f) {
                guiGraphics.disableScissor();
            }
            pPoseStack.popPose();
            this.setX(this.getX() - 2);
        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            super.mouseClicked(pMouseX, pMouseY, pButton);
            return false;
        }

        private int getTextureY() {
            int i = 1;
            if (!this.active) {
                i = 0;
            } else if (this.isHoveredOrFocused()) {
                i = 2;
            }

            return i * 20;
        }
    }

    public record ChappModInfo(String modId, String version, String url, List<Component> text, Supplier<ResourceLocation> texture, TextRenderable textRenderable) {

        public ChappModInfo(String modId, String version, String url, List<Component> text, String textureId, TextRenderable textRenderable) {
            this(modId, version, url, text,
                    ClientUtil.getTextureFromLink(ModulusMainScreen.MODULUS_SCREEN, "mods/%s".formatted(textureId),
                            "https://raw.githubusercontent.com/ChappiePie/ModulusResources/main/mods/%s.png"
                                    .formatted(textureId)), textRenderable);
        }

        public ModMetadata modInfo() {
            var v = FabricLoader.getInstance().getModContainer(this.modId);
            return v.map(ModContainer::getMetadata).orElse(null);
        }
    }

    public class ChappEntry extends Entry<ChappEntry> implements IHasTimer {

        public final Map<AbstractWidget, BiFunction<Integer, Integer, Vec2>> children;
        public final ChappModInfo modInfo;
        public final ModulusMainScreen parent;
        private final IHasTimer.Timer titleTimer = new IHasTimer.Timer(() -> 10, () -> false);
        private final IHasTimer.Timer highlightTimer = new IHasTimer.Timer(() -> 10, () -> false);

        ChappEntry(ChappModInfo info, ModulusMainScreen parent) {
            this.modInfo = info;
            this.parent = parent;
            this.children = new HashMap<>();
            var modFile = info.modInfo();
            String version = modFile != null ? modFile.getVersion().toString() : info.version;
            MyButton versionButton = new MyButton(68, 16, Component.translatable("modulus.screen.modEntry.version", version), (b) -> {
            });
            versionButton.active = false;
            this.children.put(versionButton, (x, y) -> new Vec2(4 + x, 76 + y));
            if (modFile == null) {
                this.children.put(new MyButton(68, 16, Component.translatable("modulus.screen.modEntry.download"), (b) -> {
                    Util.getPlatform().openUri(info.url);
                }), (x, y) -> new Vec2(74 + x, 76 + y));
            }
        }

        @Override
        public void render(GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            //GuiComponent.enableScissor(left, top, left + entryWidth, top + entryHeight - 10);
            //guiGraphics.fill( left, top, left + entryWidth, top + entryHeight, entryIdx % 2 == 0 ? -16777216 : -1);
            Font font = Minecraft.getInstance().font;
            AtomicInteger x = new AtomicInteger(6);
            int y = top;

            this.modInfo.textRenderable.render(this, font, x, guiGraphics,  entryIdx, y, left, entryWidth, entryHeight, mouseX, mouseY, isHovered, partialTick);
            y += 6;
            {
                int mainColor = FastColor.ARGB32.color(255, 108, 108, 108);
                int offColor = FastColor.ARGB32.color(255, 56, 56, 56);
                int minX = x.get() - 4, minY = y - 4;
                int maxX = x.get() + 128 + 4, maxY = y + 67 + 7;
                boolean isHoveredMod = mouseX > minX && mouseX < maxX && mouseY > minY && mouseY < maxY;
                guiGraphics.fill( minX, minY, maxX, maxY, mainColor);
                guiGraphics.fill( minX, maxY, maxX, maxY + 15, offColor);

                this.titleTimer.predicate = () -> isHoveredMod;
                this.titleTimer.update();
                RenderSystem.setShaderTexture(0, this.modInfo.texture.get());
                float f = this.titleTimer.value(partialTick);
                f *= Mth.sin(entryIdx + (float)(Util.getMillis() % 1000L) / 1000.0F * ((float)Math.PI * 2F)) / 2F;
                this.highlightTimer.predicate = () -> isHoveredMod;
                this.highlightTimer.update();
                float ht = 1 - this.highlightTimer.value(partialTick) * 0.5F;
                if (MOD_CLICKED.containsKey(this.modInfo.modId)) {
                    ht = 1 + this.highlightTimer.value(partialTick);
                }
                guiGraphics.setColor(ht, ht, ht, 1);
                ClientUtil.blit(guiGraphics, x.get() + 6 * f, y + 2 + 3 * f, 128 / (1.0F + f / 10F), 64 / (1.0F + f / 10F), 0, 0, 2048, 1024, 2048, 1024);
                guiGraphics.setColor(1, 1, 1, 1);
            }
            for (Map.Entry<AbstractWidget, BiFunction<Integer, Integer, Vec2>> e : this.children.entrySet()) {
                Vec2 vec2 = e.getValue().apply(x.get(), y);
                e.getKey().setX((int) vec2.x);
                e.getKey().setY((int) vec2.y);
                e.getKey().render(guiGraphics,  mouseX, mouseY, partialTick);
            }

            //GuiComponent.disableScissor();
        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            if (this.titleTimer.predicate.get() && pButton == 0) {
                if (MOD_CLICKED.containsKey(this.modInfo.modId)) {
                    MOD_CLICKED.get(this.modInfo.modId).accept(this);
                }
            }
            return super.mouseClicked(pMouseX, pMouseY, pButton);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return new ArrayList<>(this.children.keySet());
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return new ArrayList<>(this.children.keySet());
        }

        public ChappModInfo getInfo() {
            return modInfo;
        }

        @Override
        public Iterable<Timer> timers() {
            return Collections.singleton(this.titleTimer);
        }
    }

    @FunctionalInterface
    public interface TextRenderable {

        void render(ChappEntry entry, Font font, AtomicInteger x, GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float partialTick);
    }
}