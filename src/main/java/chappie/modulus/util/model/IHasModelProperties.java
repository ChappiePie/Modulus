package chappie.modulus.util.model;

import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import java.util.List;

public interface IHasModelProperties {

    void modulus$setup(LivingEntityRenderState state, float partialTicks, List<RenderLayer<?, ?>> layers);
    ModelProperties modulus$modelProperties();
}
