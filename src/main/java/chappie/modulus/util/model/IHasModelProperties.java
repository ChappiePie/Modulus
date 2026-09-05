package chappie.modulus.util.model;

import chappie.modulus.util.render.ModelPoseCache;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

import java.util.List;

public interface IHasModelProperties {

    void modulus$setup(RotationProperties rotationProperties, float partialTicks, List<RenderLayer<?, ?>> layers);

    ModelProperties modulus$modelProperties();

    ModelPoseCache modulus$poseCache();
}
