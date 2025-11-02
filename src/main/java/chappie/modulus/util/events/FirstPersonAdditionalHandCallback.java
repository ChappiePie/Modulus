package chappie.modulus.util.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public interface FirstPersonAdditionalHandCallback {
    Event<FirstPersonAdditionalHandCallback> EVENT = EventFactory.createArrayBacked(FirstPersonAdditionalHandCallback.class,
            (listeners) -> (event) -> {
                boolean canceled = false;
                for (FirstPersonAdditionalHandCallback listener : listeners) {
                    if (listener.event(event)) {
                        canceled = true;
                    }
                }
                return canceled;
            });

    boolean event(FirstPersonAdditionalHandEvent event);


    /**
     * Prerender of arm in first person, here you can modify swing progress, equip progress, and enable/disable rendering arm
     */
    record FirstPersonAdditionalHandEvent(ItemInHandRenderer instance, AtomicBoolean renderArm,
                                          AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch,
                                          InteractionHand pHand, HumanoidArm pArm, AtomicReference<Float> swingProgress,
                                          ItemStack pStack, AtomicReference<Float> equippedProgress,
                                          PoseStack pMatrixStack, SubmitNodeCollector submitNodeCollector,
                                          int pCombinedLight) {
    }
}
