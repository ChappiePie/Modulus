package chappie.modulus.mixin.client;

import chappie.modulus.util.render.IRenderStateEntity;
import chappie.modulus.util.render.ModelPoseCache;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements IRenderStateEntity {

    @Unique
    private LivingEntity entity;
    @Unique
    private final ModelPoseCache modulus$poseCache = new ModelPoseCache();

    @Override
    public LivingEntity modulus$entity() {
        return this.entity;
    }

    @Override
    public void modulus$setEntity(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public ModelPoseCache modulus$poseCache() {
        return modulus$poseCache;
    }
}
