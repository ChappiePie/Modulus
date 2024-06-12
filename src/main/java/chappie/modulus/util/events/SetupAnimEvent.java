package chappie.modulus.util.events;

import chappie.modulus.util.model.ModelProperties;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * This runs when Player's rotations are being set.
 * You can change the position and rotation of the limbs and create animation for player.
 */
public class SetupAnimEvent<T extends LivingEntity, M extends HumanoidModel<T>> extends LivingEvent {

    private final M model;
    private final ModelProperties modelProperties;

    public SetupAnimEvent(T entity, M model, ModelProperties modelProperties) {
        super(entity);
        this.model = model;
        this.modelProperties = modelProperties;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getEntity() {
        return (T) super.getEntity();
    }

    public M getModel() {
        return this.model;
    }

    public ModelProperties getModelProperties() {
        return modelProperties;
    }
}
