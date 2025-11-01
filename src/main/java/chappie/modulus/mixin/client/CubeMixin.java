package chappie.modulus.mixin.client;

import chappie.modulus.util.model.IChangeableSize;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
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
    public int u, v;
    @Unique
    public float texWidth, texHeight;
    @Unique
    public float dimensionX, dimensionY, dimensionZ;
    @Unique
    public float growX, growY, growZ;
    @Unique
    public boolean mirror;
    @Unique
    public Set<Direction> directions;
    @Mutable
    @Shadow
    @Final
    public ModelPart.Polygon[] polygons;

    @Unique
    public ModelPart.Polygon[] original$polygons;

    @Unique
    private Vector3f size;

    @Unique
    private Vector3f pos;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void mixinInit(int u, int v, float minX, float minY, float minZ, float dimensionX, float dimensionY, float dimensionZ, float growX, float growY, float growZ, boolean mirror, float texWidth, float texHeight, Set<Direction> directions, CallbackInfo ci) {
        this.u = u;
        this.v = v;
        this.dimensionX = dimensionX;
        this.dimensionY = dimensionY;
        this.dimensionZ = dimensionZ;
        this.growX = growX; // p_272945_
        this.growY = growY; // p_272790_
        this.growZ = growZ; // p_272870_
        this.mirror = mirror;
        this.texWidth = texWidth;
        this.texHeight = texHeight;
        this.directions = directions;
        this.original$polygons = polygons;
    }

    @Inject(method = "compile", at = @At("HEAD"), cancellable = true)
    private void onCompile(PoseStack.Pose pose, VertexConsumer buffer, int packedLight,
                           int packedOverlay, int color, CallbackInfo ci) {
        if (Objects.equals(this.size, new Vector3f())) return;
        Matrix4f matrix = pose.pose();
        Vector3f tempVector = new Vector3f();
        Vector3f tempNormal = new Vector3f();

        for (ModelPart.Polygon polygon : this.polygons) {
            Vector3f normal = pose.transformNormal(polygon.normal(), tempNormal);
            ModelPart.Vertex[] vertices = polygon.vertices();

            for (int i = 0; i < vertices.length; i += 4) {
                int batchEnd = Math.min(i + 4, vertices.length);
                for (int j = i; j < batchEnd; j++) {
                    ModelPart.Vertex vertex = vertices[j];
                    Vector3f pos = new Vector3f(vertex.worldX(), vertex.worldY(), vertex.worldZ());

                    Vector3f tempVector2 = matrix.transformPosition(pos.x() / 16.0F, pos.y() / 16.0F, pos.z() / 16.0F, tempVector);

                    buffer.addVertex(tempVector2.x(), tempVector2.y(), tempVector2.z(), color,
                            vertex.u(), vertex.v(), packedOverlay, packedLight,
                            normal.x(), normal.y(), normal.z());
                }
            }
        }
        ci.cancel();
    }

    @Override
    public void modulus$setSizeAndPos(Vector3f size, Vector3f pos) {
        this.size = size;
        this.pos = pos;
        if (this.size.x() == 0 && this.size.y() == 0 && this.size.z() == 0 && this.pos.x() == 0 && this.pos.y() == 0 && this.pos.z() == 0) {
            if (this.polygons != this.original$polygons) {
                this.polygons = this.original$polygons;
            }
            return;
        }

        float x = growX + size.x;
        float y = growY + size.y;
        float z = growZ + size.z;

        float originX = this.minX;
        float originY = this.minY;
        float originZ = this.minZ;
        this.polygons = new ModelPart.Polygon[directions.size()];
        float f = originX + dimensionX;
        float f1 = originY + dimensionY;
        float f2 = originZ + dimensionZ;
        originX -= x;
        originY -= y;
        originZ -= z;
        f += x;
        f1 += y;
        f2 += z;

        originX += pos.x;
        originY += pos.y;
        originZ += pos.z;

        f += pos.x;
        f1 += pos.y;
        f2 += pos.z;

        if (mirror) {
            float f3 = f;
            f = originX;
            originX = f3;
        }

        ModelPart.Vertex modelpart$vertex7 = new ModelPart.Vertex(originX, originY, originZ, 0.0F, 0.0F);
        ModelPart.Vertex modelpart$vertex = new ModelPart.Vertex(f, originY, originZ, 0.0F, 8.0F);
        ModelPart.Vertex modelpart$vertex1 = new ModelPart.Vertex(f, f1, originZ, 8.0F, 8.0F);
        ModelPart.Vertex modelpart$vertex2 = new ModelPart.Vertex(originX, f1, originZ, 8.0F, 0.0F);
        ModelPart.Vertex modelpart$vertex3 = new ModelPart.Vertex(originX, originY, f2, 0.0F, 0.0F);
        ModelPart.Vertex modelpart$vertex4 = new ModelPart.Vertex(f, originY, f2, 0.0F, 8.0F);
        ModelPart.Vertex modelpart$vertex5 = new ModelPart.Vertex(f, f1, f2, 8.0F, 8.0F);
        ModelPart.Vertex modelpart$vertex6 = new ModelPart.Vertex(originX, f1, f2, 8.0F, 0.0F);
        float f4 = (float) u;
        float f5 = (float) u + dimensionZ;
        float f6 = (float) u + dimensionZ + dimensionX;
        float f7 = (float) u + dimensionZ + dimensionX + dimensionX;
        float f8 = (float) u + dimensionZ + dimensionX + dimensionZ;
        float f9 = (float) u + dimensionZ + dimensionX + dimensionZ + dimensionX;
        float f10 = (float) v;
        float f11 = (float) v + dimensionZ;
        float f12 = (float) v + dimensionZ + dimensionY;
        int i = 0;
        if (directions.contains(Direction.DOWN)) {
            this.polygons[i++] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart$vertex4, modelpart$vertex3, modelpart$vertex7, modelpart$vertex}, f5, f10, f6, f11, texWidth, texHeight, mirror, Direction.DOWN);
        }

        if (directions.contains(Direction.UP)) {
            this.polygons[i++] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart$vertex1, modelpart$vertex2, modelpart$vertex6, modelpart$vertex5}, f6, f11, f7, f10, texWidth, texHeight, mirror, Direction.UP);
        }

        if (directions.contains(Direction.WEST)) {
            this.polygons[i++] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart$vertex7, modelpart$vertex3, modelpart$vertex6, modelpart$vertex2}, f4, f11, f5, f12, texWidth, texHeight, mirror, Direction.WEST);
        }

        if (directions.contains(Direction.NORTH)) {
            this.polygons[i++] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart$vertex, modelpart$vertex7, modelpart$vertex2, modelpart$vertex1}, f5, f11, f6, f12, texWidth, texHeight, mirror, Direction.NORTH);
        }

        if (directions.contains(Direction.EAST)) {
            this.polygons[i++] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart$vertex4, modelpart$vertex, modelpart$vertex1, modelpart$vertex5}, f6, f11, f8, f12, texWidth, texHeight, mirror, Direction.EAST);
        }

        if (directions.contains(Direction.SOUTH)) {
            this.polygons[i] = new ModelPart.Polygon(new ModelPart.Vertex[]{modelpart$vertex3, modelpart$vertex4, modelpart$vertex5, modelpart$vertex6}, f8, f11, f9, f12, texWidth, texHeight, mirror, Direction.SOUTH);
        }

    }

    @Override
    public Vector3f modulus$size() {
        return this.size;
    }

    @Override
    public Vector3f modulus$pos() {
        return this.pos;
    }
}