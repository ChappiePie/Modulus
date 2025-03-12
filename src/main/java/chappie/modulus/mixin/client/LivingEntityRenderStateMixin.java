package chappie.modulus.mixin.client;

import chappie.modulus.util.render.IRenderStateEntity;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin<T extends LivingEntity> implements IRenderStateEntity<T> {

    @Unique
    private T entity;

    @Override
    public T modulus$entity() {
        return this.entity;
    }

    @Override
    public void modulus$setEntity(T entity) {
        this.entity = entity;
    }
}