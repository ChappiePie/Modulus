package chappie.modulus.util.events;

import chappie.modulus.util.model.ModelProperties;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public interface SetupAnimCallback {
    EventInvoker EVENT = new EventInvoker();

    void event(SetupAnimEvent event);

    /**
     * This runs when Player's rotations are being set.
     * You can change the position and rotation of the limbs and create animation for player.
     */
    record SetupAnimEvent(LivingEntity entity, HumanoidModel<? extends LivingEntity> model,
                          ModelProperties modelProperties) {
    }

    class EventInvoker {
        private final List<SetupAnimCallback> listeners = new ArrayList<>();

        public void register(SetupAnimCallback listener) {
            listeners.add(listener);
        }

        public void invoke(SetupAnimEvent event) {
            for (SetupAnimCallback listener : listeners) {
                listener.event(event);
            }
        }
    }
}
