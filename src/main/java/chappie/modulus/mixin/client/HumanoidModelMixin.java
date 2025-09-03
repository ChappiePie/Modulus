package chappie.modulus.mixin.client;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.model.ModelProperties;
import chappie.modulus.util.model.RotationProperties;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Function;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin implements IHasModelProperties {

    @Unique
    private ModelProperties modulus$modelProperties;
    @Unique
    private ModelPart modulus$root;

    @Inject(method = "<init>(Lnet/minecraft/client/model/geom/ModelPart;Ljava/util/function/Function;)V", at = @At(value = "TAIL"))
    private void mixin$init(ModelPart root, Function renderType, CallbackInfo ci) {
        this.modulus$root = root;
    }

    @Override
    public void modulus$setup(RotationProperties rotationProperties, float partialTicks, List<RenderLayer<?, ?>> layers) {
        this.modulus$modelProperties = new ModelProperties(this.modulus$root, rotationProperties, partialTicks, layers);
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