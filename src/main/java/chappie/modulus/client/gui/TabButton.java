package chappie.modulus.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class TabButton extends AbstractWidget {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/gui/tab_button.png");

    public final int tabId;
    private final Supplier<Integer> currentTab;

    public TabButton(int x, int y, int width, int height, Component component, int tabId, Supplier<Integer> currentTab) {
        super(x, y, width, height, component);
        this.tabId = tabId;
        this.currentTab = currentTab;
    }

    @Override
    public void renderWidget(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.setShaderTexture(0, TEXTURE_LOCATION);
        try {
            blitNineSliced(pPoseStack, this.getX(), this.getY(), this.width, this.height, 2, 2, 2, 2, 130, 24, 0, this.getTextureY());

        }catch (Throwable e) {
            e.printStackTrace();
        }
        Font font = Minecraft.getInstance().font;
        int i = this.active ? -1 : -6250336;
        this.renderString(pPoseStack, font, i);
        if (this.isSelected()) {
            this.renderFocusUnderline(pPoseStack, font, i);
        }

    }

    public void renderString(PoseStack p_275321_, Font p_275208_, int p_275293_) {
        int i = this.getX() + 1;
        int j = this.getY() + (this.isSelected() ? 0 : 3);
        int k = this.getX() + this.getWidth() - 1;
        int l = this.getY() + this.getHeight();
        renderScrollingString(p_275321_, p_275208_, this.getMessage(), i, j, k, l, p_275293_);
    }

    private void renderFocusUnderline(PoseStack p_275458_, Font p_275475_, int p_275367_) {
        int i = Math.min(p_275475_.width(this.getMessage()), this.getWidth() - 4);
        int j = this.getX() + (this.getWidth() - i) / 2;
        int k = this.getY() + this.getHeight() - 2;
        fill(p_275458_, j, k, j + i, k + 1, p_275367_);
    }

    protected int getTextureY() {
        int i = 2;
        if (this.isSelected() && this.isHoveredOrFocused()) {
            i = 1;
        } else if (this.isSelected()) {
            i = 0;
        } else if (this.isHoveredOrFocused()) {
            i = 3;
        }

        return i * 24;
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