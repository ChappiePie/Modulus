package chappie.modulus.util.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import java.util.List;

public record ModelProperties(ModelPart root, LivingEntityRenderState state, float partialTicks,
                              List<RenderLayer<?, ?>> layers) {
}
