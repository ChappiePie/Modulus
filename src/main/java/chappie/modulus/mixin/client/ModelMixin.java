package chappie.modulus.mixin.client;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.model.ModelProperties;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(Model.class)
public abstract class ModelMixin<S> implements IHasModelProperties {

    @Shadow
    @Final
    protected ModelPart root;

    @Unique
    private ModelProperties modulus$modelProperties;

    @Override
    public void modulus$setup(LivingEntityRenderState state, float partialTicks, List<RenderLayer<?, ?>> layers) {
        this.modulus$modelProperties = new ModelProperties(this.root, state, partialTicks, layers);
    }

    @Override
    public ModelProperties modulus$modelProperties() {
        if (this.modulus$modelProperties == null) {
            this.modulus$setup(new LivingEntityRenderState(), ClientUtil.getPartialTick(), List.of());
        }
        return this.modulus$modelProperties;
    }
}