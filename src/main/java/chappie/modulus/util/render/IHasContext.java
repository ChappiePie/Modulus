package chappie.modulus.util.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public interface IHasContext {
    static EntityRendererProvider.Context getContext() {
        return ((IHasContext) Minecraft.getInstance().getEntityRenderDispatcher()).modulus$lastContext();
    }

    EntityRendererProvider.Context modulus$lastContext();
}
