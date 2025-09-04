package chappie.modulus.client.gui;

import chappie.modulus.Modulus;
import chappie.modulus.util.ModRegistries;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class ModulusMenuButton extends Button {

    public static final ResourceLocation LOGO = Modulus.id("textures/gui/logo.png");

    public ModulusMenuButton(int pX, int pY, OnPress pOnPress) {
        super(pX, pY, 20, 20, Component.translatable("narrator.button.modulus"), pOnPress, Supplier::get);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int bWidth = (int) (this.width * 0.8F), bHeight = (int) (this.height * 0.8F);

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        guiGraphics.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());

        int addX = (this.width - bWidth) / 2, addY = (this.height - bHeight) / 2;
        guiGraphics.blit(LOGO, this.getX() + addX, this.getY() + addY, bWidth, bHeight, 0, 0, 16, 16, 16, 16);

        guiGraphics.setColor(1, 1, 1, 1);
    }

    @Override
    public void playDownSound(SoundManager pHandler) {
        pHandler.play(SimpleSoundInstance.forUI(ModRegistries.OPEN_BUTTON, 1.0F));
    }

    private int getTextureY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isHoveredOrFocused()) {
            i = 2;
        }

        return 46 + i * 20;
    }
}
