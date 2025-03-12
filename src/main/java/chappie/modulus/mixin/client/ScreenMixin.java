package chappie.modulus.mixin.client;

import chappie.modulus.util.IOneScaleScreen;
import chappie.modulus.util.IScreenExtender;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin implements IScreenExtender {

    @Unique
    private boolean modulus$removed = false;

    @Shadow public int width;

    @Shadow public int height;

    @Inject(method = "added", at = @At("TAIL"))
    private void mixin$added(CallbackInfo ci) {
        if (this instanceof IOneScaleScreen) {
            Minecraft mc = Minecraft.getInstance();
            Window window = mc.getWindow();
            int i = Minecraft.getInstance().getWindow().calculateScale(mc.options.guiScale().get(), mc.isEnforceUnicode());
            window.setGuiScale(i);
            this.width = window.getGuiScaledWidth();
            this.height = window.getGuiScaledHeight();

            mc.getMainRenderTarget().resize(window.getWidth(), window.getHeight());
            mc.gameRenderer.resize(window.getWidth(), window.getHeight());
            mc.mouseHandler.setIgnoreFirstMove();
        }
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void mixin$removed(CallbackInfo ci) {
        if (this instanceof IOneScaleScreen) {
            this.modulus$removed = true;
            Minecraft mc = Minecraft.getInstance();
            Window window = mc.getWindow();
            int i = Minecraft.getInstance().getWindow().calculateScale(mc.options.guiScale().get(), mc.isEnforceUnicode());
            window.setGuiScale(i);
            this.width = window.getGuiScaledWidth();
            this.height = window.getGuiScaledHeight();

            mc.getMainRenderTarget().resize(window.getWidth(), window.getHeight());
            mc.gameRenderer.resize(window.getWidth(), window.getHeight());
            mc.mouseHandler.setIgnoreFirstMove();
        }
    }

    @Override
    public boolean modulus$isRemoved() {
        return this.modulus$removed;
    }
}
