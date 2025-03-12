package chappie.modulus.util.render;

import net.minecraft.world.entity.LivingEntity;

public interface IRenderStateEntity<T extends LivingEntity> {
    T modulus$entity();

    void modulus$setEntity(T entity);
}
