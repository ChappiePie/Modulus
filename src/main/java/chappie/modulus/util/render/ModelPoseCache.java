package chappie.modulus.util.render;

import chappie.modulus.mixin.client.ModelPartAccessor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

import java.util.HashMap;
import java.util.Map;

public final class ModelPoseCache {

    private final Map<String, PoseData> poses = new HashMap<>();

    public void clear() {
        this.poses.clear();
    }

    public void storePose(HumanoidModel<?> model) {
        this.poses.clear();
        this.capture("root", model.root());
    }

    public void applyPose(HumanoidModel<?> model) {
        if (!this.hasPose()) {
            return;
        }
        this.applyPose("root", model.root());
    }

    public boolean hasPose() {
        return !this.poses.isEmpty();
    }

    private void capture(String path, ModelPart part) {
        this.poses.put(path, PoseData.from(part));
        ((ModelPartAccessor) (Object) part).modulus$getChildren()
                .forEach((name, child) -> this.capture("%s/%s".formatted(path, name), child));
    }

    private void applyPose(String path, ModelPart part) {
        PoseData pose = this.poses.get(path);
        if (pose != null) {
            pose.apply(part);
        }
        ((ModelPartAccessor) (Object) part).modulus$getChildren()
                .forEach((name, child) -> this.applyPose("%s/%s".formatted(path, name), child));
    }

    // из за того что оно изменяет рендер стейт, моделька костюма тоже ресетается (прозрачность), но повороты работают нормально...
    private record PoseData(float x, float y, float z,
                            float xRot, float yRot, float zRot,
                            boolean visible, boolean skipDraw) {

        static PoseData from(ModelPart part) {
            return new PoseData(part.x, part.y, part.z,
                    part.xRot, part.yRot, part.zRot,
                    part.visible, part.skipDraw);
        }

        void apply(ModelPart part) {
            part.x = this.x;
            part.y = this.y;
            part.z = this.z;
            part.xRot = this.xRot;
            part.yRot = this.yRot;
            part.zRot = this.zRot;
            part.visible = this.visible;
            part.skipDraw = this.skipDraw;
        }
    }
}
