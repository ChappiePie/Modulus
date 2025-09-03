package chappie.modulus.util.model;

import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public interface IHasModelProperties {

    void modulus$setup(LivingEntityRenderState renderstate, float partialTicks, List<RenderLayer<?, ?>> layers);

    void setEntity(LivingEntity entity);

    ModelProperties modulus$modelProperties();
}
