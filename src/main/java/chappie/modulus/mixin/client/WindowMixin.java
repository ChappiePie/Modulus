package chappie.modulus.mixin.client;

import chappie.modulus.util.IOneScaleScreen;
import chappie.modulus.util.IScreenExtender;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Window.class)
public class WindowMixin {

    @ModifyVariable(method = "calculateScale(IZ)I", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private int injected(int pGuiScale) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen instanceof IOneScaleScreen scl && screen instanceof IScreenExtender ex && !ex.modulus$isRemoved())
            return scl.scale(pGuiScale);
        return pGuiScale;
    }
}
