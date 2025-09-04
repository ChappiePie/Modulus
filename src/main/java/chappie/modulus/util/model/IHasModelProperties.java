package chappie.modulus.util.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

import java.util.List;
import java.util.stream.Stream;

public interface IHasModelProperties {

    void modulus$setup(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float partialTicks, List<RenderLayer<?, ?>> layers);

    ModelProperties modulus$modelProperties();

    default Stream<ModelPart> allParts() {
        return modulus$modelProperties().root().getAllParts();
    }
}
