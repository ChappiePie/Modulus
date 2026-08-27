package chappie.modulus.client.gui;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.IHasTimer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ModulusAboutButton extends Button implements IHasTimer {
    protected final Identifier Identifier;
    protected final int xTexStart;
    protected final int yTexStart;
    protected final int textureWidth;
    protected final int textureHeight;
    protected final int index;
    private final Timer timer = new Timer(() -> 10, this::isHoveredOrFocused);

    public ModulusAboutButton(int pX, int pY, int pScale, int pXTexStart, Identifier pResourceLocation, Supplier<List<String>> links, int index) {
        this(pX, pY, pScale, pScale, pXTexStart, 0, pResourceLocation, (b) -> {
            if (!links.get().isEmpty()) {
                Util.getPlatform().openUri(links.get().get(index));
            }
        }, index);
    }

    public ModulusAboutButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, Identifier pResourceLocation, OnPress pOnPress, int index) {
        this(pX, pY, pWidth, pHeight, pXTexStart, pYTexStart, pResourceLocation, 256, 256, pOnPress, CommonComponents.EMPTY, index);
    }

    public ModulusAboutButton(int pX, int pY, int pWidth, int pHeight, int pXTexStart, int pYTexStart, Identifier pResourceLocation, int pTextureWidth, int pTextureHeight, OnPress pOnPress, Component pMessage, int index) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, DEFAULT_NARRATION);
        this.textureWidth = pTextureWidth;
        this.textureHeight = pTextureHeight;
        this.xTexStart = pXTexStart;
        this.yTexStart = pYTexStart;
        this.Identifier = pResourceLocation;
        this.index = index;
    }

    @Override
    public void extractContents(GuiGraphicsExtractor GuiGraphicsExtractor, int pMouseX, int pMouseY, float pPartialTick) {
        float f = this.height / 8F * this.timer.value(pPartialTick);
        //super.renderWidget(GuiGraphicsExtractor, pMouseX, pMouseY, pPartialTick);

        float x = this.index == 0 || this.index == 2 ? -f : 0;
        float y = this.index == 0 || this.index == 1 ? -f : 0;

        ClientUtil.blit(GuiGraphicsExtractor, this.Identifier, this.getX() + x, this.getY() + y, this.xTexStart, this.yTexStart, this.width + f, this.height + f, 16, 16, this.textureWidth, this.textureHeight, -1);
        //this.renderTexture(GuiGraphicsExtractor, this.Identifier, this.getX(), this.getY(), this.xTexStart, this.yTexStart, this.yDiffTex, this.width, this.height, this.textureWidth, this.textureHeight);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        super.mouseClicked(event, isDoubleClick);
        if (event.button() == 0) {
            boolean flag = event.x() >= (double) this.getX() && event.x() < (double) (this.getX() + this.width) && event.y() >= (double) this.getY() && event.y() < (double) (this.getY() + this.height);
            if (flag) {
                this.setFocused(false);
                return false;
            }
        }
        return false;
    }

    @Override
    public Iterable<Timer> timers() {
        return Collections.singleton(this.timer);
    }
}