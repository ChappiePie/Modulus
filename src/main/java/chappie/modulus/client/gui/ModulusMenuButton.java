package chappie.modulus.client.gui;

import chappie.modulus.common.ModSounds;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class ModulusMenuButton extends ImageButton {


    public ModulusMenuButton(int pX, int pY, OnPress pOnPress) {
        super(pX, pY, 20, 20, 0, 0, 0, ModulusMainScreen.LOGO,
                16, 16, pOnPress, Component.translatable("narrator.button.modulus"));
    }

    @Override
    public void renderWidget(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        int bWidth = (int) (this.width * 0.8F), bHeight = (int) (this.height * 0.8F);
        int i = 66, yDiff = this.yTexStart;
        if (!this.isActive() || this.isHoveredOrFocused()) {
            i += 20;
            yDiff += this.yDiffTex;
        }
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        blitNineSliced(pPoseStack, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, i);

        int addX = (this.width - bWidth) / 2, addY = (this.height - bHeight) / 2;
        RenderSystem.setShaderTexture(0, this.resourceLocation);
        blit(pPoseStack, this.getX() + addX, this.getY() + addY, bWidth, bHeight, this.xTexStart, yDiff, this.textureWidth, this.textureHeight, this.textureWidth, this.textureHeight);
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    @Override
    public void playDownSound(SoundManager pHandler) {
        pHandler.play(SimpleSoundInstance.forUI(ModSounds.YA, 1.0F));
    }
}
