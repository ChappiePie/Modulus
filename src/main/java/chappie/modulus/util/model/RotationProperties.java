package chappie.modulus.util.model;

import net.minecraft.world.entity.LivingEntity;

public record RotationProperties(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                                 float netHeadYaw, float headPitch) {
}
