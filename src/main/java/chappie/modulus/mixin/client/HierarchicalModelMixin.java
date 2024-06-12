package chappie.modulus.mixin.client;

import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.model.ModelProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(HierarchicalModel.class)
public abstract class HierarchicalModelMixin<T extends Entity> implements IHasModelProperties {
    @Shadow public abstract ModelPart root();

    @Unique private ModelProperties modulus$modelProperties;

    @Override
    public void setup(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float partialTicks, List<RenderLayer<?, ?>> layers) {
        this.modulus$modelProperties = new ModelProperties(this.root(), limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, layers);
    }

    @Override
    public ModelProperties modelProperties() {
        if (this.modulus$modelProperties == null) {
            this.setup(0, 0, 0, 0, 0, Minecraft.getInstance().getPartialTick(), List.of());
        }
        return this.modulus$modelProperties;
    }
}