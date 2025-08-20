package chappie.modulus.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class TabButton extends AbstractWidget {
    private static final WidgetSprites SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("widget/tab_selected"), ResourceLocation.withDefaultNamespace("widget/tab"), ResourceLocation.withDefaultNamespace("widget/tab_selected_highlighted"), ResourceLocation.withDefaultNamespace("widget/tab_highlighted"));

    public final int tabId;
    private final Supplier<Integer> currentTab;

    public TabButton(int x, int y, int width, int height, Component component, int tabId, Supplier<Integer> currentTab) {
        super(x, y, width, height, component);
        this.tabId = tabId;
        this.currentTab = currentTab;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        try {
            guiGraphics.blitSprite(RenderType::guiTextured, SPRITES.get(this.isSelected(), this.isHovered()), this.getX(), this.getY(), this.width, this.height);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        Font font = Minecraft.getInstance().font;
        int i = this.active ? -1 : -6250336;
        this.renderString(guiGraphics, font, i);
        if (this.isSelected()) {
            this.renderFocusUnderline(guiGraphics, font, i);
        }

    }

    public void renderString(GuiGraphics guiGraphics, Font p_275208_, int p_275293_) {
        int i = this.getX() + 1;
        int j = this.getY() + (this.isSelected() ? 0 : 3);
        int k = this.getX() + this.getWidth() - 1;
        int l = this.getY() + this.getHeight();
        renderScrollingString(guiGraphics, p_275208_, this.getMessage(), i, j, k, l, p_275293_);
    }

    private void renderFocusUnderline(GuiGraphics guiGraphics, Font p_275475_, int p_275367_) {
        int i = Math.min(p_275475_.width(this.getMessage()), this.getWidth() - 4);
        int j = this.getX() + (this.getWidth() - i) / 2;
        int k = this.getY() + this.getHeight() - 2;
        guiGraphics.fill(j, k, j + i, k + 1, p_275367_);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationText) {
        this.defaultButtonNarrationText(narrationText);
    }

    @Override
    public void playDownSound(SoundManager p_276302_) {
    }

    @Override
    public void setFocused(boolean pFocused) {
        super.setFocused(pFocused);

    }

    public boolean isSelected() {
        return this.currentTab.get() == this.tabId;
    }
}