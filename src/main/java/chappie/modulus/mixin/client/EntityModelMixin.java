package chappie.modulus.mixin.client;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.model.ModelProperties;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;
import java.util.function.Function;

@Mixin(EntityModel.class)
public abstract class EntityModelMixin extends Model implements IHasModelProperties {

    @Unique
    private ModelProperties modulus$modelProperties;

    public EntityModelMixin(ModelPart root, Function<ResourceLocation, RenderType> renderType) {
        super(root, renderType);
    }

    @Override
    public void setup(LivingEntityRenderState renderstate, float partialTicks, List<RenderLayer<?, ?>> layers) {
        this.modulus$modelProperties = new ModelProperties(this.root(), renderstate, partialTicks, layers);
    }

    @Override
    public ModelProperties modelProperties() {
        if (this.modulus$modelProperties == null) {
            this.setup(new LivingEntityRenderState(), ClientUtil.getPartialTick(), List.of());
        }
        return this.modulus$modelProperties;
    }
}