package chappie.modulus.mixin.client;

import chappie.modulus.util.model.IChangeableSize;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin implements IChangeableSize {
    @Mutable
    @Shadow
    @Final
    private List<ModelPart.Cube> cubes;

    @Unique
    private Vector3f modulus$size = new Vector3f();

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setSize(Vector3f size) {
        if (!this.modulus$size.equals(size)) {
            this.modulus$size = size;
        }
    }

    @Override
    public Vector3f size() {
        return modulus$size;
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"))
    private void setSize(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
        for (ModelPart.Cube cube : this.cubes) {
            ((IChangeableSize) cube).setSize(this.modulus$size);
        }
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("TAIL"))
    private void resetSize(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
        this.setSize(new Vector3f());
    }
}
