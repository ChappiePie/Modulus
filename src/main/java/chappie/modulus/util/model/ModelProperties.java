package chappie.modulus.util.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import java.util.List;

public record ModelProperties(ModelPart root, LivingEntityRenderState renderstate, float partialTicks,
                              List<RenderLayer<?,?>> layers) {
    public ModelProperties(ModelPart root, LivingEntityRenderState renderstate, float partialTicks, List<RenderLayer<?, ?>> layers) {
        this.root = root;
        this.renderstate = renderstate;
        this.partialTicks = partialTicks;
        this.layers = layers;
        this.root.getAllParts().forEach(ModelPart::resetPose);
    }
}
