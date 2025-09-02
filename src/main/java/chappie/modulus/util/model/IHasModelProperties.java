package chappie.modulus.util.model;

import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public interface IHasModelProperties {

    void setup(RotationProperties rotationProperties, float partialTicks, List<RenderLayer<?, ?>> layers);

    void setEntity(LivingEntity entity);

    ModelProperties modelProperties();
}
