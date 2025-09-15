package chappie.modulus.mixin.client;

import chappie.modulus.client.ClientEvents;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.common.ability.base.condition.ScrollCondition;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.server.ServerKeyInput;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.KeyMap;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @WrapWithCondition(method = "onScroll(JDD)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedHotbarSlot(I)V"))
    public boolean scroll(Inventory instance, int selectedHotbarSlot, @Local(ordinal = 1, argsOnly = true) double yOffset) {
        if (this.minecraft.player == null) return false;
        for (Ability ability : CommonUtil.getAbilities(this.minecraft.player)) {
            for (Map.Entry<String, List<Condition>> e : ability.conditionManager.methodConditions().entrySet()) {
                for (Condition condition : e.getValue()) {
                    if (condition instanceof ScrollCondition key) {
                        if (yOffset > 0) {
                            if (key.keyType.equals(KeyMap.KeyType.MOUSE_SCROLL_UP)) {
                                ClientEvents.KEYS.setDown(key.keyType, true);
                                if (ability.keys.notEquals(ClientEvents.KEYS)) {
                                    ability.keys.copyFrom(ClientEvents.KEYS);
                                    ability.conditionManager.conditions().forEach(Condition::keyEvent);
                                    ModNetworking.sendToServer(new ServerKeyInput(ability.builder.id, ClientEvents.KEYS));
                                }
                                if (ability.conditionManager.test(e.getKey())) {
                                    return false;
                                }
                            }
                        } else {
                            if (key.keyType.equals(KeyMap.KeyType.MOUSE_SCROLL_DOWN)) {
                                ClientEvents.KEYS.setDown(key.keyType, true);
                                if (ability.keys.notEquals(ClientEvents.KEYS)) {
                                    ability.keys.copyFrom(ClientEvents.KEYS);
                                    ability.conditionManager.conditions().forEach(Condition::keyEvent);
                                    ModNetworking.sendToServer(new ServerKeyInput(ability.builder.id, ClientEvents.KEYS));
                                }
                                if (ability.conditionManager.test(e.getKey())) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
