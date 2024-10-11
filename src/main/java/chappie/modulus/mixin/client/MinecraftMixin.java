package chappie.modulus.mixin.client;

import chappie.modulus.client.ClientEvents;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.common.ability.base.condition.KeyCondition;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.server.ServerKeyInput;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.KeyMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(method = "startAttack()Z", at = @At("HEAD"), cancellable = true)
    public void cancelAttack(CallbackInfoReturnable<Boolean> cir) {
        if (this.player == null) return;
        for (Ability ability : CommonUtil.getAbilities(this.player)) {
            for (Map.Entry<String, List<Condition>> e : ability.conditionManager.methodConditions().entrySet()) {
                for (Condition condition : e.getValue()) {
                    if (condition instanceof KeyCondition key && key.keyType.equals(KeyMap.KeyType.MOUSE_LEFT)) {
                        ClientEvents.KEYS.setDown(key.keyType, true);
                        if (ability.keys.notEquals(ClientEvents.KEYS)) {
                            ability.keys.copyFrom(ClientEvents.KEYS);
                            ability.conditionManager.conditions().forEach(Condition::keyEvent);
                            ModNetworking.sendToServer(new ServerKeyInput(ability.builder.id, ClientEvents.KEYS));
                        }
                        if (ability.conditionManager.test(e.getKey())) {
                            cir.setReturnValue(false);
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "startUseItem()V", at = @At("HEAD"), cancellable = true)
    public void cancelUseItem(CallbackInfo ci) {
        if (this.player == null) return;
        for (Ability ability : CommonUtil.getAbilities(this.player)) {
            for (Map.Entry<String, List<Condition>> e : ability.conditionManager.methodConditions().entrySet()) {
                for (Condition condition : e.getValue()) {
                    if (condition instanceof KeyCondition key && key.keyType.equals(KeyMap.KeyType.MOUSE_RIGHT)) {
                        ClientEvents.KEYS.setDown(key.keyType, true);
                        if (ability.keys.notEquals(ClientEvents.KEYS)) {
                            ability.keys.copyFrom(ClientEvents.KEYS);
                            ability.conditionManager.conditions().forEach(Condition::keyEvent);
                            ModNetworking.sendToServer(new ServerKeyInput(ability.builder.id, ClientEvents.KEYS));
                        }
                        if (ability.conditionManager.test(e.getKey())) {
                            ci.cancel();
                        }
                    }
                }
            }
        }
    }
}
