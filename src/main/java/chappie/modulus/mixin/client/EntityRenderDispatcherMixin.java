package chappie.modulus.mixin.client;

import chappie.modulus.util.render.IHasContext;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin implements IHasContext {

    @Unique
    private EntityRendererProvider.Context context;

    @Inject(method = "onResourceManagerReload(Lnet/minecraft/server/packs/resources/ResourceManager;)V",
            at = @At("TAIL"))
    private void getContext(ResourceManager pResourceManager, CallbackInfo ci, @Local EntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public EntityRendererProvider.Context modulus$lastContext() {
        return this.context;
    }
}
