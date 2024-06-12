package chappie.modulus.client.gui;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class ModulusAboutButton extends Button {
    private final IHasTimer.Timer timer = new IHasTimer.Timer(() -> 10, this::isHoveredOrFocused);
    protected final ResourceLocation resourceLocation;
    protected final int xTexStart;
    protected final int yTexStart;
    protected final int textureWidth;
    protected final int textureHeight;
    protected final int index;

    public ModulusAboutButton(int pX, int pY, int pScale, int pXTexStart, ResourceLocation pResourceLocation, Supplier<List<String>> links, int index) {
        this(pX, pY, pScale, pScale, pXTexStart, 0, pResourceLocation, (b) -> {
            if (links.get().size() > 0) {
                Util.getPlatform().openUri(links.get().get(index));
            }
        }, index);
    }

    public ModulusAboutButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation pResourceLocation, OnPress pOnPress, int index) {
        this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pResourceLocation, 256, 256, pOnPress, CommonComponents.EMPTY, index);
    }

    public ModulusAboutButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, ResourceLocation pResourceLocation, int pTextureWidth, int pTextureHeight, OnPress pOnPress, Component pMessage, int index) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, DEFAULT_NARRATION);
        this.textureWidth = pTextureWidth;
        this.textureHeight = pTextureHeight;
        this.xTexStart = pXTexStart;
        this.yTexStart = pYTexStart;
        this.resourceLocation = pResourceLocation;
        this.index = index;
    }

    @Override
    public void renderWidget(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        float f = this.height / 8F * this.timer.value(pPartialTick);
        this.timer.update();
        //super.renderWidget(pPoseStack, pMouseX, pMouseY, pPartialTick);
        RenderSystem.setShaderTexture(0, this.resourceLocation);

        float x = this.index == 0 || this.index == 2 ? -f : 0;
        float y = this.index == 0 || this.index == 1 ? -f : 0;

        ClientUtil.blit(pPoseStack, this.getX() + x, this.getY() + y, this.width + f, this.height + f, this.xTexStart, this.yTexStart, 16, 16, this.textureWidth, this.textureHeight);
        //this.renderTexture(pPoseStack, this.resourceLocation, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        super.mouseClicked(pMouseX, pMouseY, pButton);
        if (pButton == 0) {
            boolean flag = pMouseX >= (double) this.getX() && pMouseX < (double) (this.getX() + this.width) && pMouseY >= (double) this.getY() && pMouseY < (double) (this.getY() + this.height);
            if (flag) {
                this.setFocused(false);
                return false;
            }
        }
        return false;
    }

}