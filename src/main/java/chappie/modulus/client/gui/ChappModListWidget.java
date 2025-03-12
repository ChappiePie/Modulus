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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

// Список модов
public class ChappModListWidget extends ContainerObjectSelectionList<ChappModListWidget.ChappEntry> {

    // Что происходит когда тыкаешь на лого мода в списке
    public static final Map<String, Consumer<ChappEntry>> MOD_CLICKED = Maps.newHashMap();
    // Список с данными о модах
    private static final Supplier<JsonObject> JSON_LIST = CommonUtil.getJsonFromLink("https://raw.githubusercontent.com/ChappiePie/ModulusResources/main/modList.json");

    // Длина списка
    private final int listWidth;
    // Обычное оформление текста модов
    private final TextRenderable DEFAULT_TEXT = (entry, font, x, guiGraphics, entryIdx, top, left, entryWidth, entryHeight, mouseX, mouseY, isHovered, partialTick) -> {
        boolean b = entryIdx % 2 == 0;
        int initX = x.get();
        x.set(initX + (b ? left + 128 : left - 12) + 6);

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        Component modName = Component.literal(entry.modInfo.modInfo() == null ? entry.modInfo.modId : entry.modInfo.modInfo().getName()).withStyle(ClientUtil.BOLD_MINECRAFT);
        float f = 2.5F;
        poseStack.scale(f, f, f);
        poseStack.translate(b ? (x.get() + 5) / f : (entryWidth - 128) / f - font.width(modName), (top + 5) / f, 0);
        guiGraphics.drawString(font, modName, 0, 0, -1, true);
        poseStack.popPose();

        List<Component> list = entry.modInfo.modInfo() == null ? entry.modInfo.text : Component.literal(entry.modInfo.modInfo().getDescription()).toFlatList();
        MultiLineLabel label = MultiLineLabel.create(font, entryWidth - 160, 7, list.toArray(new Component[0]));

        poseStack.pushPose();
        int y = top + 27;
        guiGraphics.fill(x.get() + 4, y, x.get() + 10 + label.getWidth(), y + 2, -1);
        y += 4;
        label.renderLeftAligned(guiGraphics, x.get() + 8, y, font.lineHeight, 0xFFFFFF);
        int newX = b ? x.get() + 4 : x.get() + 10 + label.getWidth();
        int yMax = y + font.lineHeight * label.getLineCount() + 3;
        guiGraphics.fill(newX, y - 4, newX + 2, yMax, -1);
        poseStack.popPose();
        x.set(initX + (b ? left : entryWidth - 128));
    };

    // Создание самого виджета
    // #super(майнкрафт, ширина, высота, позиция по y, высота предмета)
    public ChappModListWidget(int listWidth, int top, int bottom) {
        super(Minecraft.getInstance(), listWidth, bottom - top, top, 110);
        this.listWidth = listWidth;
        this.refreshList(); // Обновляем список для новых данных
    }

    // Убираем рендер фона для списка
    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
    }

    // Позиция списка
    @Override
    protected int scrollBarX() {
        return this.listWidth;
    }

    // Ширина поля для объекта
    @Override
    public int getRowWidth() {
        return this.listWidth - 20;
    }

    // Обновить список, ради новых данных
    public void refreshList() {
        this.clearEntries();
        for (Map.Entry<String, JsonElement> e : JSON_LIST.get().entrySet()) {
            if (e.getValue() instanceof JsonObject json) {
                this.addEntry(new ChappEntry(new ChappModInfo(e.getKey(),
                        json.get("version").getAsString(),
                        json.get("url").getAsString(),
                        CommonUtil.parseDescriptionLines(json.get("description")),
                        json.has("textureId") ? json.get("textureId").getAsString() : e.getKey(),
                        DEFAULT_TEXT)));
            }
        }
    }

    // Рендер предметов в списке
    @Override
    protected void renderItem(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick, int pIndex, int pLeft, int pTop, int pWidth, int pHeight) {
        int colorIn = ARGB.color(150, 0, 0, 0);
        int colorOut = ARGB.color(50, 255, 255, 255);
        this.renderSelection(guiGraphics, pTop, pWidth, pHeight - 8, colorOut, colorIn);
        super.renderItem(guiGraphics, pMouseX, pMouseY, pPartialTick, pIndex, pLeft, pTop, pWidth, pHeight);
    }

    // Пытаться постоянно обновить список, если он пустой или ты не смог синхронизировать его с тем что в интернете.
    public void tick() {
        if (!JSON_LIST.get().isEmpty() && this.children().isEmpty()) {
            this.refreshList();
        }
    }

    // 098765432
    @FunctionalInterface
    public interface TextRenderable {

        void render(ChappEntry entry, Font font, AtomicInteger x, GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean isHovered, float partialTick);
    }

    // Кнопка под иконкой. (Скачать или версия мода)
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
            PoseStack pPoseStack = guiGraphics.pose();

            RenderSystem.enableBlend(); // Добавляем смешивание и глубину ради прозрачности
            RenderSystem.enableDepthTest();

            // Рендер уменьшенной кнопки
            pPoseStack.pushPose();
            float f = 0.75F;
            pPoseStack.scale(f, f, 1.0F);
            f = 1.0F / f;
            pPoseStack.translate(this.getX() * f, this.getY() * f, 0);
            guiGraphics.blitSprite(
                    RenderType::guiTextured,
                    ModulusMainScreen.SPRITES.get(this.active, this.isHoveredOrFocused()),
                    0, 0,
                    (int) this.oldSize.x, (int) this.oldSize.y,
                    ARGB.white(this.alpha)
            );
            pPoseStack.popPose();

            // Рендер уменьшенного текста, который может перелистываться, если слишком длинный.
            pPoseStack.pushPose();

            float f1 = 0.5F;
            pPoseStack.scale(f1, f1, 1.0F);
            f1 = 1.0F / f1;
            pPoseStack.translate(this.getX() * f1, this.getY() * f1, 0);
            guiGraphics.enableScissor(6, 5, (int) (this.oldSize.x * f + 6), (int) (this.oldSize.y + 3));
            renderScrollingString(guiGraphics, Minecraft.getInstance().font, this.getMessage(), -30, -10, (int) (this.oldSize.x * f1), (int) (this.oldSize.y * f1), 10526880 | Mth.ceil(1 * 255.0F) << 24);
            guiGraphics.disableScissor();

            pPoseStack.popPose();

            // Изменяем некоторые данные ради
            this.setWidth((int) ((this.oldSize.x - 5) / f));
            this.setHeight((int) (this.oldSize.y / f));
            this.setX(this.getX() - 2);
        }

        // Отменяем возвращение клика
        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            super.mouseClicked(pMouseX, pMouseY, pButton);
            return false;
        }
    }

    // Информация о моде
    public record ChappModInfo(String modId, String version, String url, List<Component> text, ResourceLocation texture,
                               TextRenderable textRenderable) {

        public ChappModInfo(String modId, String version, String url, List<Component> text, String textureId, TextRenderable textRenderable) {
            this(modId, version, url, text, ModulusMainScreen.getTexByName("mods/%s".formatted(textureId)), textRenderable);
        }

        public ModMetadata modInfo() {
            var v = FabricLoader.getInstance().getModContainer(this.modId);
            return v.map(ModContainer::getMetadata).orElse(null);
        }
    }

    // Предмет в списке
    public static class ChappEntry extends Entry<ChappEntry> implements IHasTimer {

        public final Map<AbstractWidget, BiFunction<Integer, Integer, Vec2>> children;
        public final ChappModInfo modInfo;

        // Анимация приближения
        private final Timer titleTimer = new Timer(() -> 10, () -> false);
        // Анимация подсветки, если мод имеет событие в ChappModListWidget#MOD_CLICKED
        private final Timer highlightTimer = new Timer(() -> 10, () -> false);

        ChappEntry(ChappModInfo info) {
            this.modInfo = info;
            this.children = new HashMap<>();
            var modFile = info.modInfo();
            // Брать версию из файла мода, если он установлен, иначе брать версию с JSON.
            String version = modFile != null ? modFile.getVersion().getFriendlyString() : info.version;
            if (version.equals("${version}")) {
                version = "1.0";
            }
            MyButton versionButton = new MyButton(68, 16, Component.translatable("screen.modulus.modEntry.version", version), (b) -> {
            });
            versionButton.active = false; // Кнопка версии не должна быть активной.
            this.children.put(versionButton, (x, y) -> new Vec2(4 + x, 76 + y));
            // При отсутствии мода, добавлять кнопку скачать.
            if (modFile == null) {
                this.children.put(new MyButton(68, 16, Component.translatable("screen.modulus.modEntry.download"), (b) -> {
                    Util.getPlatform().openUri(info.url);
                }), (x, y) -> new Vec2(74 + x, 76 + y));
            }
        }

        // Рендер
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
                int mainColor = ARGB.color(255, 108, 108, 108);
                int offColor = ARGB.color(255, 56, 56, 56);
                int minX = x.get() - 4, minY = y - 4;
                int maxX = x.get() + 128 + 4, maxY = y + 67 + 7;
                boolean isHoveredMod = mouseX > minX && mouseX < maxX && mouseY > minY && mouseY < maxY;
                guiGraphics.fill( minX, minY, maxX, maxY, mainColor);
                guiGraphics.fill( minX, maxY, maxX, maxY + 15, offColor);

                this.titleTimer.predicate = () -> isHoveredMod;
                this.titleTimer.update();
                float f = this.titleTimer.value(partialTick);
                f *= Mth.sin(entryIdx + (float)(Util.getMillis() % 1000L) / 1000.0F * ((float)Math.PI * 2F)) / 2F;
                this.highlightTimer.predicate = () -> isHoveredMod;
                this.highlightTimer.update();
                float ht = 1 - this.highlightTimer.value(partialTick) * 0.5F;
                if (MOD_CLICKED.containsKey(this.modInfo.modId)) {
                    ht = 1 + this.highlightTimer.value(partialTick);
                }

                ClientUtil.blit(guiGraphics, this.modInfo.texture, x.get() + 6 * f, y + 2 + 3 * f, 0, 0, 128 / (1.0F + f / 10F), 64 / (1.0F + f / 10F), 2048, 1024, 2048, 1024, ARGB.colorFromFloat(1, ht, ht, ht));
            }
            for (Map.Entry<AbstractWidget, BiFunction<Integer, Integer, Vec2>> e : this.children.entrySet()) {
                Vec2 vec2 = e.getValue().apply(x.get(), y);
                e.getKey().setX((int) vec2.x);
                e.getKey().setY((int) vec2.y);
                e.getKey().render(guiGraphics,  mouseX, mouseY, partialTick);
            }

            //GuiComponent.disableScissor();
        }


        // Использовать событие при клике мыши
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
}