package chappie.modulus.mixin.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsAccessor {

    @Accessor("scissorStack")
    GuiGraphics.ScissorStack getScissorStack();

    @Invoker("applyScissor")
    void applyScissor(@Nullable ScreenRectangle rectangle);
}
