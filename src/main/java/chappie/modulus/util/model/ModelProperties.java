package chappie.modulus.util.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

import java.util.List;

public record ModelProperties(ModelPart root, float limbSwing, float limbSwingAmount,
                              float ageInTicks, float netHeadYaw, float headPitch, float partialTicks,
                              List<RenderLayer<?,?>> layers) {
    public ModelProperties(ModelPart root, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float partialTicks, List<RenderLayer<?, ?>> layers) {
        this.root = root;
        this.limbSwing = limbSwing;
        this.limbSwingAmount = limbSwingAmount;
        this.ageInTicks = ageInTicks;
        this.netHeadYaw = netHeadYaw;
        this.headPitch = headPitch;
        this.partialTicks = partialTicks;
        this.layers = layers;
        this.root.getAllParts().forEach(ModelPart::resetPose);
    }
}
