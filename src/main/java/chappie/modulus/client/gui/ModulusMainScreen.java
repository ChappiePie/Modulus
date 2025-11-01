package chappie.modulus.client.gui;

import chappie.modulus.Modulus;
import chappie.modulus.util.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModulusMainScreen extends Screen implements IOneScaleScreen {

    public static final ResourceLocation MENU = Modulus.id("textures/gui/sprites/menu.png");
    public static final WidgetSprites SPRITES = new WidgetSprites(
            Modulus.id("widget/button"),
            Modulus.id("widget/button_disabled"),
            Modulus.id("widget/button_highlighted")
    );
    private static final ResourceLocation CHAPPIE_TEXTURE = getTexByName("chappie");
    private final Screen lastScreen;
    private final List<TabButton> tabs = Lists.newArrayList();
    private final IHasTimer.Timer atChappieTimer = new IHasTimer.Timer(() -> 10, () -> false);
    private final Supplier<List<String>> links = CommonUtil.getTxtFromLink("https://raw.githubusercontent.com/ChappiePie/ModulusResources/main/links.txt");
    public List<GuiEventListener> pageWidgets = new ArrayList<>();
    private int tabId = 0;
    private int canvasMinY, canvasMaxY;
    private ChappModListWidget modList;
    private long utilMillis;

    public ModulusMainScreen(Screen lastScreen) {
        super(Component.translatable("gui.modulus.mainScreen"));
        this.lastScreen = lastScreen;
    }

    public static ResourceLocation getTexByName(String name) {
        ResourceLocation resourcelocation = Modulus.id("modulus_screen" + "/" + name);
        File file = new File("config/modulus/data", name);
        return HttpTexture.byUrl(file, "https://raw.githubusercontent.com/ChappiePie/ModulusResources/main/%s.png".formatted(name), resourcelocation, Modulus.id("textures/gui/mods_author/%s.png".formatted(name)));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.modList != null) {
            this.modList.tick();
        }
    }

    @Override
    protected void init() {
        super.init();
        this.canvasMinY = 58;
        this.canvasMaxY = this.height - 36;

        int halfWidth = this.width / 2;
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (b) ->
                this.minecraft.setScreen(this.lastScreen)).pos(halfWidth - 75, this.height - 30).build());

        this.tabs.clear();
        int tabWidth = 100;
        int xPos = halfWidth - tabWidth;
        for (int i = 0; i < 2; i++) {
            MutableComponent component = Component.translatable("screen.modulus.tab.%s".formatted(i == 0 ? "mods" : "about"));
            TabButton tabButton = new TabButton(xPos, 40, tabWidth, 24, component.withStyle(ClientUtil.BOLD_MINECRAFT), i, () -> this.tabId);
            this.tabs.add(tabButton);

            this.addRenderableWidget(tabButton);
            xPos += tabWidth;
        }
        this.setFocused(this.tabs.get(this.tabId));
    }

    @SuppressWarnings("unchecked")
    public <T extends GuiEventListener & Renderable & NarratableEntry> ImmutableList<T> createModsPage() {
        this.modList = new ChappModListWidget(this, this.width - 18, 64, this.height - 42);
        this.modList.setX(6);
        return (ImmutableList<T>) ImmutableList.of(this.modList);
    }

    @SuppressWarnings("unchecked")
    public <T extends GuiEventListener & Renderable & NarratableEntry> ImmutableList<T> createAboutPage() {
        List<T> buttons = new ArrayList<>();
        int canvasHeight = this.canvasMaxY - this.canvasMinY;
        for (int i = 0; i < 4; i++) {
            int size = canvasHeight / 3;
            int height = this.canvasMinY + 7 + 20 + 2 + size / 8;
            int j = i == 0 || i == 2 ? 0 : size;
            int k = i > 1 ? size : 0;
            buttons.add((T) new ModulusAboutButton(this.width / 4 - size + j, height + k, size, 105 + i * 16, MENU, this.links, i));
        }
        return buttons.stream().collect(ImmutableList.toImmutableList());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {

        long l = Util.getMillis();
        if (l - this.utilMillis > 10L) {
            this.utilMillis = l;
            this.atChappieTimer.update();
            for (GuiEventListener renderable : this.children()) {
                if (renderable instanceof IHasTimer timer) {
                    for (IHasTimer.Timer t : timer.timers()) {
                        t.update();
                    }
                }
            }
        }
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

        // Modulus header
        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                SPRITES.get(true, false),
                this.width / 2 - 60, 3,
                120, 32,
                ARGB.white(1.0F)
        );

        Matrix3x2fStack pPoseStack = guiGraphics.pose();
        // Label
        {
            pPoseStack.pushMatrix();
            pPoseStack.translate(this.width / 4F, 4);
            pPoseStack.scale(0.5F, 0.5F);

            int labelXPos = this.width / 2, labelYPos = 10;

            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MENU, labelXPos - 52, labelYPos, 0, 0, 104, 29, 104, 29, 256, 256);


            // Line under label
            int lineColor = ARGB.color(255, 46, 51, 53);
            labelYPos += 30;

            //RenderSystem.setShaderColor(0.15F, 0.15F, 0.15F, 1F);
            guiGraphics.fill(labelXPos - 51 - 16, labelYPos + 3, labelXPos - 51 + 104 + 16, labelYPos + 5, lineColor);
            //RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            guiGraphics.fill(labelXPos - 52 - 16, labelYPos + 2, labelXPos - 52 + 104 + 16, labelYPos + 4, lineColor);

            pPoseStack.popMatrix();
        }
        //guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Screen.HEADER_SEPARATOR, 0, 56, 0.0F, 0.0F, this.width, 2, 32, 2);
        //guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Screen.FOOTER_SEPARATOR, 0, Mth.roundToward(this.height - 36, 2), 0.0F, 0.0F, this.width, 2, 32, 2);

        if (this.tabId == 1) {
            int canvasHeight = this.canvasMaxY - this.canvasMinY;
            this.renderTitle(guiGraphics, Component.translatable("screen.modulus.socials")
                    .withStyle(ClientUtil.BOLD_MINECRAFT), (int) (this.width / 4F), this.canvasMinY + 10);

            // Two sticks like borders
            {
                int c = ARGB.color(20, 255, 255, 255);
                int x = 0;
                int minY = this.canvasMinY + 10;
                int maxY = this.canvasMaxY - 8;

                // right at socials
                guiGraphics.fill(x - 2 + 30, minY, x + 2 + 30, maxY, c);

                guiGraphics.fill(x + 2 + 30, minY, x + 6 + 30, minY + 4, c);

                x = this.width / 2;
                // right at center
                guiGraphics.fill(x - 2 + 30, minY, x + 2 + 30, maxY - 4, c);

                guiGraphics.fill(x + 2 + 30, minY, x + 6 + 30, minY + 4, c);

                // left at center
                guiGraphics.fill(x - 2 - 30, minY, x + 2 - 30, maxY - 4, c);

                guiGraphics.fill(x - 2 - 30, minY, x - 6 - 30, minY + 4, c);

                // left at creator
                x = this.width;
                guiGraphics.fill(x - 2 - 30, minY, x + 2 - 30, maxY, c);

                guiGraphics.fill(x - 2 - 30, minY, x - 6 - 30, minY + 4, c);

                guiGraphics.fill(2 + 30, maxY, x - 2 - 30, maxY - 4, c);
            }

            // Creator
            {
                Component s = Component.translatable("screen.modulus.creator").withStyle(ClientUtil.BOLD_MINECRAFT);
                int x = (int) (this.width * 0.75F), y = this.canvasMinY + 10;

                this.renderTitle(guiGraphics, s, x, y);
                //RenderSystem.setShaderColor(0.125F, 0.125F, 0.125F, 1.0F);
                final int width = (int) (canvasHeight / 1.27);
                final int x1 = x - width / 2, y1 = y + 25;
                guiGraphics.enableScissor(x1, y1, x1 + width, y1 + (int) (canvasHeight / 1.5));
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Screen.MENU_BACKGROUND, x1, y1, 0, 0, width, (int) (canvasHeight / 1.5F), 32, 32);
                //RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);


                this.atChappieTimer.predicate = () -> isMouseOverObj(pMouseX, pMouseY, x1, y1, width, canvasHeight / 1.5F);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                float f = this.atChappieTimer.value(ClientUtil.getPartialTick());
                float mouseXAdd = (pMouseX - x1 - width / 2F) / 10F * f;
                float mouseYAdd = Math.max(-40, pMouseY - y1 - (int) (canvasHeight / 1.5F) / 2F) / 10F * f;
                f *= 10F;
                ClientUtil.blit(guiGraphics, CHAPPIE_TEXTURE, x1 + 11.5F - f / 2 + mouseXAdd, y1 - f / 2 + mouseYAdd, 0.0F, 0.0F, canvasHeight / 1.6F + f, canvasHeight / 1.5F + f, 1310, 1440, 1310, 1440, -1);
                guiGraphics.disableScissor();

                Component pTooltip = Component.literal("ChappiePie");
                if (isMouseOverObj(pMouseX, pMouseY, x - 1 - this.minecraft.font.width(s) * 0.75F, y - 1, (this.minecraft.font.width(s) * 0.75F) * 2, this.minecraft.font.lineHeight * 1.5F - 1)) {
                    pPoseStack.pushMatrix();
                    pPoseStack.translate(0, 0, 50);
                    int i = pMouseX + 2;
                    int j = pMouseY - 10;
                    int k = this.font.width(pTooltip);
                    guiGraphics.fillGradient(i - 3, j - 3, i + k + 3, j + 8 + 3, -1073741824, -1073741824);
                    guiGraphics.drawString(this.font, pTooltip, i, j, 16777215, true);
                    pPoseStack.popMatrix();
                }
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    private boolean isMouseOverObj(float pMouseX, float pMouseY, float x, float y, float width, float height) {
        return pMouseX >= x && pMouseY >= y && pMouseX <= x + width && pMouseY <= y + height;
    }

    private void renderTitle(GuiGraphics guiGraphics, Component component, int x, int y) {
        PoseStack pPoseStack = guiGraphics.pose();
        pPoseStack.pushPose();
        pPoseStack.translate(x + 1, y, 0);
        pPoseStack.scale(1.5F, 1.5F, 1F);
        guiGraphics.drawCenteredString(this.minecraft.font, component, 0, 0, -1);
        pPoseStack.popPose();
        y += 14;
        int color = -1;
        for (int k = 0; k < 2; k++) {
            if (k == 0) {
                color = ARGB.colorFromFloat(1F, 0.25F, 0.25F, 0.25F);
                x += 2;
                y += 2;
            }
            int length = this.minecraft.font.width(component) + 5;
            guiGraphics.fill(x - length, y, x + length, y + 2, color);

            guiGraphics.fill(x - length + 2, y - 2, x - length + 4, y + 4, color);
            guiGraphics.fill(x + length - 2, y - 2, x + length - 4, y + 4, color);

            //RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            if (k == 0) {
                x -= 2;
                y -= 2;
                color = -1;
            }
        }
    }


    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen instanceof PauseScreen ? null : this.lastScreen);
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModRegistries.CLOSE_BUTTON, 1.0F));
    }

    @Override
    public int scale(int scale) {
        return 3;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener pListener) {
        if (pListener instanceof TabButton b && this.getFocused() != b) {
            this.tabId = b.tabId;
            for (GuiEventListener pageWidget : this.pageWidgets) {
                this.removeWidget(pageWidget);
            }
            this.pageWidgets.clear();
            for (var listener : switch (this.tabId) {
                //case 0 -> this.createSettingsPage();
                case 0 -> this.createModsPage();
                default -> this.createAboutPage();
            }) {
                this.addRenderableWidget(listener);
                this.pageWidgets.add(listener);
            }

        }
        super.setFocused(pListener);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (Screen.hasControlDown()) {
            int i = this.getNextTabIndex(pKeyCode);
            if (i != -1) {
                this.setFocused(this.tabs.get(Mth.clamp(i, 0, this.tabs.size() - 1)));
                return true;
            }
        } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        return false;
    }

    private int getNextTabIndex(int pKeyCode) {
        if (pKeyCode >= 49 && pKeyCode <= 57) {
            return pKeyCode - 49;
        } else {
            if (pKeyCode == 258) {
                int i = this.tabId;
                if (i != -1) {
                    int j = Screen.hasShiftDown() ? i - 1 : i + 1;
                    return Math.floorMod(j, this.tabs.size());
                }
            }

            return -1;
        }
    }
}