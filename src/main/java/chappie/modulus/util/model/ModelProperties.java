package chappie.modulus.util.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

import java.util.List;

public record ModelProperties(ModelPart root, RotationProperties properties, float partialTicks,
                              List<RenderLayer<?, ?>> layers) {
    public ModelProperties(ModelPart root, RotationProperties properties, float partialTicks, List<RenderLayer<?, ?>> layers) {
        this.root = root;
        this.properties = properties;
        this.partialTicks = partialTicks;
        this.layers = layers;
        this.root.getAllParts().forEach(ModelPart::resetPose);
    }
}
