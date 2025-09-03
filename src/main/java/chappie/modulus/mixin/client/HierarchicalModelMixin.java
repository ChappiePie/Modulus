package chappie.modulus.mixin.client;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.model.ModelProperties;
import chappie.modulus.util.model.RotationProperties;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(HierarchicalModel.class)
public abstract class HierarchicalModelMixin implements IHasModelProperties {

    @Unique
    private ModelProperties modulus$modelProperties;

    @Shadow
    public abstract ModelPart root();

    @Override
    public void modulus$setup(RotationProperties rotationProperties, float partialTicks, List<RenderLayer<?, ?>> layers) {
        this.modulus$modelProperties = new ModelProperties(this.root(), rotationProperties, partialTicks, layers);
    }

    @Override
    public ModelProperties modulus$modelProperties() {
        if (this.modulus$modelProperties == null) {
            this.modulus$setup(new RotationProperties(null, 0, 0, 0, 0, 0),
                    ClientUtil.getPartialTick(), List.of());
        }
        return this.modulus$modelProperties;
    }
}