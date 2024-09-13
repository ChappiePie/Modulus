package chappie.modulus.client.gui;

import chappie.modulus.util.ModRegistries;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class ModulusMenuButton extends Button {

    public ModulusMenuButton(int pX, int pY, OnPress pOnPress) {
        super(pX, pY, 20, 20, Component.translatable("narrator.button.modulus"), pOnPress, Supplier::get);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        int bWidth = (int) (this.width * 0.8F), bHeight = (int) (this.height * 0.8F);

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        guiGraphics.blitSprite(SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());

        int addX = (this.width - bWidth) / 2, addY = (this.height - bHeight) / 2;
        guiGraphics.blit(ModulusMainScreen.LOGO, this.getX() + addX, this.getY() + addY, bWidth, bHeight, 0, 0, 16, 16, 16, 16);

        guiGraphics.setColor(1, 1, 1, 1);
    }

    @Override
    public void playDownSound(SoundManager pHandler) {
        pHandler.play(SimpleSoundInstance.forUI(ModRegistries.YA, 1.0F));
    }
}
