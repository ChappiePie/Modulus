package chappie.modulus.mixin.client;

import chappie.modulus.util.render.IHasContext;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin implements IHasContext {

    private EntityRendererProvider.Context context;

    @Inject(method = "onResourceManagerReload(Lnet/minecraft/server/packs/resources/ResourceManager;)V",
            at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void getContext(ResourceManager pResourceManager, CallbackInfo ci, EntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public EntityRendererProvider.Context lastContext() {
        return this.context;
    }
}
