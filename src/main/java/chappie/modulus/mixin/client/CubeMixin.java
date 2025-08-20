package chappie.modulus.mixin.client;

import chappie.modulus.util.model.IChangeableSize;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ModelPart.Cube.class)
public class CubeMixin implements IChangeableSize {

    @Mutable
    @Shadow
    @Final
    public float minX;
    @Mutable
    @Shadow
    @Final
    public float minY;
    @Mutable
    @Shadow
    @Final
    public float minZ;
    @Unique
    public int p_273701_, p_273034_;
    @Unique
    public float p_273591_, p_273313_;
    @Unique
    public float p_273722_, p_273763_, p_272823_;
    @Unique
    public float modulus$growX, modulus$growY, modulus$growZ;
    @Unique
    public boolean p_273589_;
    @Unique
    public Set<Direction> p_273291_;
    @Mutable
    @Shadow
    @Final
    private ModelPart.Polygon[] polygons;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void mixinInit(int u, int v, float minX, float minY, float minZ, float dimensionX, float dimensionY, float dimensionZ, float growX, float growY, float growZ, boolean mirror, float texWidth, float texHeight, Set<Direction> directions, CallbackInfo ci) {
        this.p_273701_ = u;
        this.p_273034_ = v;
        this.p_273722_ = dimensionX;
        this.p_273763_ = dimensionY;
        this.p_272823_ = dimensionZ;
        this.modulus$growX = growX; // p_272945_
        this.modulus$growY = growY; // p_272790_
        this.modulus$growZ = growZ; // p_272870_
        this.p_273589_ = mirror;
        this.p_273591_ = texWidth;
        this.p_273313_ = texHeight;
        this.p_273291_ = directions;
    }

    @Override
    public void setSize(Vector3f size) {
        float x = modulus$growX + size.x;
        float y = modulus$growY + size.y;
        float z = modulus$growZ + size.z;

        float p_272824_ = this.minX;
        float p_273777_ = this.minY;
        float p_273748_ = this.minZ;
        this.polygons = new ModelPart.Polygon[p_273291_.size()];
        float f = p_272824_ + p_273722_;
        float f1 = p_273777_ + p_273763_;
        float f2 = p_273748_ + p_272823_;
        p_272824_ -= x;
        p_273777_ -= y;
        p_273748_ -= z;
        f += x;
        f1 += y;
        f2 += z;
        if (p_273589_) {
            float f3 = f;
            f = p_272824_;
            p_272824_ = f3;
        }

        ModelPart.Vertex modelpart$vertex7 = new ModelPart.Vertex(p_272824_, p_273777_, p_273748_, 0.0F, 0.0F);
        ModelPart.Vertex modelpart$vertex = new ModelPart.Vertex(f, p_273777_, p_273748_, 0.0F, 8.0F);
        ModelPart.Vertex modelpart$vertex1 = new ModelPart.Vertex(f, f1, p_273748_, 8.0F, 8.0F);
        ModelPart.Vertex modelpart$vertex2 = new ModelPart.Vertex(p_272824_, f1, p_273748_, 8.0F, 0.0F);
        ModelPart.Vertex modelpart$vertex3 = new ModelPart.Vertex(p_272824_, p_273777_, f2, 0.0F, 0.0F);
        ModelPart.Vertex modelpart$vertex4 = new ModelPart.Vertex(f, p_273777_, f2, 0.0F, 8.0F);
        ModelPart.Vertex modelpart$vertex5 = new ModelPart.Vertex(f, f1, f2, 8.0F, 8.0F);
        ModelPart.Vertex modelpart$vertex6 = new ModelPart.Vertex(p_272824_, f1, f2, 8.0F, 0.0F);
        float f4 = (float) p_273701_;
        float f5 = (float) p_273701_ + p_272823_;
        float f6 = (float) p_273701_ + p_272823_ + p_273722_;
        float f7 = (float) p_273701_ + p_272823_ + p_273722_ + p_273722_;
        float f8 = (float) p_273701_ + p_272823_ + p_273722_ + p_272823_;
        float f9 = (float) p_273701_ + p_272823_ + p_273722_ + p_272823_ + p_273722_;
        float f10 = (float) p_273034_;
        float f11 = (float) p_273034_ + p_272823_;
        float f12 = (float) p_273034_ + p_272823_ + p_273763_;
        int i = 0;
        if (p_273291_.contains(Direction.DOWN)) {
            this.polygons[i++] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart$vertex4, modelpart$vertex3, modelpart$vertex7, modelpart$vertex}, f5, f10, f6, f11, p_273591_, p_273313_, p_273589_, Direction.DOWN);
        }

        if (p_273291_.contains(Direction.UP)) {
            this.polygons[i++] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart$vertex1, modelpart$vertex2, modelpart$vertex6, modelpart$vertex5}, f6, f11, f7, f10, p_273591_, p_273313_, p_273589_, Direction.UP);
        }

        if (p_273291_.contains(Direction.WEST)) {
            this.polygons[i++] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart$vertex7, modelpart$vertex3, modelpart$vertex6, modelpart$vertex2}, f4, f11, f5, f12, p_273591_, p_273313_, p_273589_, Direction.WEST);
        }

        if (p_273291_.contains(Direction.NORTH)) {
            this.polygons[i++] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart$vertex, modelpart$vertex7, modelpart$vertex2, modelpart$vertex1}, f5, f11, f6, f12, p_273591_, p_273313_, p_273589_, Direction.NORTH);
        }

        if (p_273291_.contains(Direction.EAST)) {
            this.polygons[i++] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart$vertex4, modelpart$vertex, modelpart$vertex1, modelpart$vertex5}, f6, f11, f8, f12, p_273591_, p_273313_, p_273589_, Direction.EAST);
        }

        if (p_273291_.contains(Direction.SOUTH)) {
            this.polygons[i] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart$vertex3, modelpart$vertex4, modelpart$vertex5, modelpart$vertex6}, f8, f11, f9, f12, p_273591_, p_273313_, p_273589_, Direction.SOUTH);
        }

    }

    @Override
    public Vector3f size() {
        return null;
    }
}