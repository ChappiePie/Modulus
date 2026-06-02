package chappie.modulus.util.render;

import net.minecraft.world.entity.LivingEntity;

public interface IRenderStateEntity {
    LivingEntity modulus$entity();

    void modulus$setEntity(LivingEntity entity);

    ModelPoseCache modulus$poseCache();
}
