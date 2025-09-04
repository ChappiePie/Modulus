package chappie.modulus.mixin.client;

import chappie.modulus.util.ClientUtil;
import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.model.ModelProperties;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(QuadrupedModel.class)
public abstract class QuadrupedModelMixin implements IHasModelProperties {
    @Unique private ModelProperties modulus$modelProperties;
    @Unique private ModelPart modulus$root;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void mixin$init(ModelPart pRoot, boolean pScaleHead, float pBabyYHeadOffset, float pBabyZHeadOffset, float pBabyHeadScale, float pBabyBodyScale, int pBodyYOffset, CallbackInfo ci) {
        this.modulus$root = pRoot;
    }

    @Override
    public void modulus$setup(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float partialTicks, List<RenderLayer<?, ?>> layers) {
        this.modulus$modelProperties = new ModelProperties(this.modulus$root, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, layers);
    }

    @Override
    public ModelProperties modulus$modelProperties() {
        if (this.modulus$modelProperties == null) {
            this.modulus$setup(0, 0, 0, 0, 0, ClientUtil.getPartialTick(), List.of());
        }
        return this.modulus$modelProperties;
    }
}