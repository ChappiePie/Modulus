package chappie.modulus.client.gui;

import chappie.modulus.Modulus;
import chappie.modulus.common.ModSounds;
import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.IOneScaleScreen;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModulusMainScreen extends Screen implements IOneScaleScreen {

    public static final ResourceLocation MENU = new ResourceLocation(Modulus.MODID, "textures/gui/menu.png");
    public static final ResourceLocation LOGO = new ResourceLocation(Modulus.MODID, "textures/gui/logo.png");
    public static final ResourceLocation MODULUS_SCREEN = new ResourceLocation(Modulus.MODID, "modulus_screen");
    private static final Supplier<ResourceLocation> CHAPPIE_TEXTURE = ClientUtil.getTextureFromLink(MODULUS_SCREEN, "chappie", "https://raw.githubusercontent.com/ChappiePie/ModulusResources/main/chappie.png");
    private final Screen lastScreen;
    private final List<TabButton> tabs = Lists.newArrayList();
    private final IHasTimer.Timer atChappieTimer = new IHasTimer.Timer(() -> 10, () -> false);
    private final Supplier<List<String>> links = CommonUtil.getTxtFromLink("https://raw.githubusercontent.com/ChappiePie/ModulusResources/main/links.txt");
    public List<GuiEventListener> pageWidgets = new ArrayList<>();
    private int tabId = 0;
    private int canvasMinY, canvasMaxY;
    private ChappModListWidget modList;

    public ModulusMainScreen(Screen lastScreen) {
        super(Component.translatable("gui.modulus.mainScreen"));
        this.lastScreen = lastScreen;
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
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (b) -> {
            this.minecraft.setScreen(this.lastScreen);
        }).pos(halfWidth - 75, this.height - 30).build());


        this.tabs.clear();
        int tabWidth = 100;
        int xPos = halfWidth - tabWidth;
        for (int i = 0; i < 2; i++) {
            MutableComponent component = Component.translatable("modulus.screen.tab.%s".formatted(i == 0 ? "mods" : "about"));
            TabButton tabButton = new TabButton(xPos, 34, tabWidth, 24, component.withStyle(ClientUtil.BOLD_MINECRAFT), i, () -> this.tabId);
            this.tabs.add(tabButton);

            this.addRenderableWidget(tabButton);
            xPos += tabWidth;
        }
        this.setFocused(this.tabs.get(this.tabId));
    }

    public <T extends GuiEventListener & Renderable & NarratableEntry> ImmutableList<T> createSettingsPage() {
        return ImmutableList.of();
    }

    @SuppressWarnings("unchecked")
    public <T extends GuiEventListener & Renderable & NarratableEntry> ImmutableList<T> createModsPage() {
        this.modList = new ChappModListWidget(this, this.width - 18, 64, this.height - 42);
        this.modList.setLeftPos(6);
        this.modList.setRenderTopAndBottom(false);
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
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        this.atChappieTimer.update();
        for (Renderable renderable : this.renderables) {
            if (renderable instanceof IHasTimer timer) {
                for (IHasTimer.Timer t : timer.timers()) {
                    t.update();
                }
            }
        }

        this.renderDirtBackground(pPoseStack);
        // Modulus header
        RenderSystem.setShaderTexture(0, MENU);
        blitNineSliced(pPoseStack, this.width / 2 - 60, 4, 120, 29, 20, 4, 60, 20, 196, 20);

        // Label
        {
            pPoseStack.pushPose();
            pPoseStack.translate(this.width / 4F, 4, 0);
            pPoseStack.scale(0.5F, 0.5F, 1);

            RenderSystem.setShaderTexture(0, MENU);
            int labelXPos = this.width / 2, labelYPos = 10;

            RenderSystem.setShaderColor(0.15F, 0.15F, 0.15F, 1F);
            blit(pPoseStack, labelXPos - 51, labelYPos + 2, 104, 29, 0, 0, 104, 29, 256, 256);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);

            blit(pPoseStack, labelXPos - 52, labelYPos, 104, 29, 0, 0, 104, 29, 256, 256);


            // Line under label
            int lineColor = FastColor.ARGB32.color(255, 46, 51, 53);
            labelYPos += 30;

            RenderSystem.setShaderColor(0.15F, 0.15F, 0.15F, 1F);
            fill(pPoseStack, labelXPos - 51 - 16, labelYPos + 3, labelXPos - 51 + 104 + 16, labelYPos + 5, lineColor);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            fill(pPoseStack, labelXPos - 52 - 16, labelYPos + 2, labelXPos - 52 + 104 + 16, labelYPos + 4, lineColor);

            pPoseStack.popPose();
        }

        RenderSystem.setShaderTexture(0, CreateWorldScreen.HEADER_SEPERATOR);
        blit(pPoseStack, 0, 56, 0.0F, 0.0F, this.width, 2, 32, 2);

        RenderSystem.setShaderTexture(0, CreateWorldScreen.FOOTER_SEPERATOR);
        blit(pPoseStack, 0, Mth.roundToward(this.height - 36, 2), 0.0F, 0.0F, this.width, 2, 32, 2);

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        if (this.tabId == 1) {
            int canvasHeight = this.canvasMaxY - this.canvasMinY;
            this.renderTitle(pPoseStack, Component.translatable("modulus.screen.socials")
                    .withStyle(ClientUtil.BOLD_MINECRAFT), (int) (this.width / 4F), this.canvasMinY + 10);

            // Two sticks like borders
            {
                int c = FastColor.ARGB32.color(20, 255, 255, 255);
                int x = 0;
                int minY = this.canvasMinY + 10;
                int maxY = this.canvasMaxY - 8;

                // right at socials
                fill(pPoseStack, x - 2 + 30, minY, x + 2 + 30, maxY, c);

                fill(pPoseStack, x + 2 + 30, minY, x + 6 + 30, minY + 4, c);

                x = this.width / 2;
                // right at center
                fill(pPoseStack, x - 2 + 30, minY, x + 2 + 30, maxY - 4, c);

                fill(pPoseStack, x + 2 + 30, minY, x + 6 + 30, minY + 4, c);

                // left at center
                fill(pPoseStack, x - 2 - 30, minY, x + 2 - 30, maxY - 4, c);

                fill(pPoseStack, x - 2 - 30, minY, x - 6 - 30, minY + 4, c);

                // left at creator
                x = this.width;
                fill(pPoseStack, x - 2 - 30, minY, x + 2 - 30, maxY, c);

                fill(pPoseStack, x - 2 - 30, minY, x - 6 - 30, minY + 4, c);
                
                fill(pPoseStack, 2 + 30, maxY, x - 2 - 30, maxY - 4, c);
            }

            // Creator
            {
                Component s = Component.translatable("modulus.screen.creator").withStyle(ClientUtil.BOLD_MINECRAFT);
                int x = (int) (this.width * 0.75F), y = this.canvasMinY + 10;

                this.renderTitle(pPoseStack, s, x, y);
                RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
                RenderSystem.setShaderColor(0.125F, 0.125F, 0.125F, 1.0F);
                final int width = (int) (canvasHeight / 1.27);
                final int x1 = x - width / 2, y1 = y + 25;
                GuiComponent.enableScissor(x1, y1, x1 + width, y1 + (int) (canvasHeight / 1.5));
                blit(pPoseStack, x1, y1, 0, 0, width, (int) (canvasHeight / 1.5F), 32, 32);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);


                this.atChappieTimer.predicate = () -> isMouseOverObj(pMouseX, pMouseY, x1, y1, width, canvasHeight / 1.5F);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderTexture(0, CHAPPIE_TEXTURE.get());
                float f = this.atChappieTimer.value(pPartialTick);
                float mouseXAdd = (pMouseX - x1 - width / 2F) / 10F * f;
                float mouseYAdd = Math.max(-40, pMouseY - y1 - (int) (canvasHeight / 1.5F) / 2F) / 10F * f;
                f *= 10F;
                ClientUtil.blit(pPoseStack, x1 + 11.5F - f / 2 + mouseXAdd, y1 - f / 2 + mouseYAdd, canvasHeight / 1.6F + f, canvasHeight / 1.5F + f, 0.0F, 0.0F, 1310, 1440, 1310, 1440);
                GuiComponent.disableScissor();

                Component pTooltip = Component.literal("ChappiePie");
                if (isMouseOverObj(pMouseX, pMouseY, x - 1 - this.minecraft.font.width(s) * 0.75F, y - 1, (this.minecraft.font.width(s) * 0.75F) * 2, this.minecraft.font.lineHeight * 1.5F - 1)) {
                    int i = pMouseX + 2;
                    int j = pMouseY - 10;
                    int k = this.font.width(pTooltip);
                    fillGradient(pPoseStack, i - 3, j - 3, i + k + 3, j + 8 + 3, -1073741824, -1073741824);
                    this.font.drawShadow(pPoseStack, pTooltip, (float) i, (float) j, 16777215);
                }
            }
        }
    }

    private boolean isMouseOverObj(float pMouseX, float pMouseY, float x, float y, float width, float height) {
        return pMouseX >= x && pMouseY >= y && pMouseX <= x + width && pMouseY <= y + height;
    }

    private void renderTitle(PoseStack pPoseStack, Component component, int x, int y) {
        pPoseStack.pushPose();
        pPoseStack.translate(x + 1, y, 0);
        pPoseStack.scale(1.5F, 1.5F, 1F);
        GuiComponent.drawCenteredString(pPoseStack, this.minecraft.font, component, 0, 0, -1);
        pPoseStack.popPose();
        y += 14;
        for (int k = 0; k < 2; k++) {
            if (k == 0) {
                RenderSystem.setShaderColor(0.25F, 0.25F, 0.25F, 1F);
                x += 2;
                y += 2;
            }
            int length = this.minecraft.font.width(component) + 5;
            fill(pPoseStack, x - length, y, x + length, y + 2, -1);

            fill(pPoseStack, x - length + 2, y - 2, x - length + 4, y + 4, -1);
            fill(pPoseStack, x + length - 2, y - 2, x + length - 4, y + 4, -1);

            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            if (k == 0) {
                x -= 2;
                y -= 2;
            }
        }
    }


    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen instanceof PauseScreen ? null : this.lastScreen);
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.NET, 1.0F));
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