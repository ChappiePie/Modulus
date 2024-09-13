package chappie.modulus.util.events;

import chappie.modulus.util.model.ModelProperties;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;

public interface SetupAnimCallback {
    Event<SetupAnimCallback> EVENT = EventFactory.createArrayBacked(SetupAnimCallback.class,
            (listeners) -> (event) -> {
                for (SetupAnimCallback listener : listeners) {
                    listener.event(event);
                }
            });

    void event(SetupAnimEvent event);

    /**
     * This runs when Player's rotations are being set.
     * You can change the position and rotation of the limbs and create animation for player.
     */
    record SetupAnimEvent(LivingEntity entity, HumanoidModel<? extends LivingEntity> model,
                          ModelProperties modelProperties) {
    }
}
