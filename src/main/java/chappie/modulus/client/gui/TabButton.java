package chappie.modulus.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class TabButton extends AbstractWidget {
    private static final WidgetSprites SPRITES = new WidgetSprites(Identifier.withDefaultNamespace("widget/tab_selected"), Identifier.withDefaultNamespace("widget/tab"), Identifier.withDefaultNamespace("widget/tab_selected_highlighted"), Identifier.withDefaultNamespace("widget/tab_highlighted"));

    public final int tabId;
    private final Supplier<Integer> currentTab;

    public TabButton(int x, int y, int width, int height, Component component, int tabId, Supplier<Integer> currentTab) {
        super(x, y, width, height, component);
        this.tabId = tabId;
        this.currentTab = currentTab;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        try {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(this.isSelected(), this.isHovered()), this.getX(), this.getY(), this.width, this.height);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        Font font = Minecraft.getInstance().font;
        int i = this.active ? -1 : -6250336;
        this.doRenderString(guiGraphics, font, i);
        if (this.isSelected()) {
            this.doRenderFocusUnderline(guiGraphics, font, i);
        }

    }

    private void doRenderString(GuiGraphicsExtractor guiGraphics, Font font, int color) {
        int i = this.getX() + 1;
        int j = this.getY() + (this.isSelected() ? 0 : 3);
        int k = this.getX() + this.getWidth() - 1;
        int l = this.getY() + this.getHeight();
        guiGraphics.centeredText(font, this.getMessage(), (i + k) / 2, (j + l - font.lineHeight) / 2, color);
    }

    private void doRenderFocusUnderline(GuiGraphicsExtractor guiGraphics, Font font, int color) {
        int i = Math.min(font.width(this.getMessage()), this.getWidth() - 4);
        int j = this.getX() + (this.getWidth() - i) / 2;
        int k = this.getY() + this.getHeight() - 2;
        guiGraphics.fill(j, k, j + i, k + 1, color);
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