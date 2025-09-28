package chappie.modulus.mixin.client;

import chappie.modulus.util.ClientUtil;
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
import java.util.Map;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin implements IChangeableSize {
    @Mutable
    @Shadow
    @Final
    private List<ModelPart.Cube> cubes;

    @Shadow
    @Final
    private Map<String, ModelPart> children;
    @Unique
    private Vector3f modulus$size = new Vector3f();
    @Unique
    private Vector3f modulus$pos = new Vector3f();

    @SuppressWarnings("ConstantConditions")
    @Override
    public void modulus$setSizeAndPos(Vector3f size, Vector3f pos) {
        if (!this.modulus$size.equals(size)) {
            this.modulus$size = size;
        }
        if (!this.modulus$pos.equals(pos)) {
            this.modulus$pos = pos;
        }
        for (ModelPart value : this.children.values()) {
            ClientUtil.modified(value).modulus$setSizeAndPos(size, pos);
        }
    }

    @Override
    public Vector3f modulus$size() {
        return modulus$size;
    }

    @Override
    public Vector3f modulus$pos() {
        return this.modulus$pos;
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("HEAD"))
    private void setSize(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
        for (ModelPart.Cube cube : this.cubes) {
            if (cube instanceof IChangeableSize s) {
                s.modulus$setSizeAndPos(this.modulus$size, this.modulus$pos);
            }
        }
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V", at = @At("TAIL"))
    private void resetSize(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color, CallbackInfo ci) {
        this.modulus$setSizeAndPos(new Vector3f(), new Vector3f());
    }
}
