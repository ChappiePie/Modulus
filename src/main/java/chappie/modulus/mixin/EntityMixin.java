package chappie.modulus.mixin;

import chappie.modulus.common.ability.DamageImmunityAbility;
import chappie.modulus.util.CommonUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "isInvulnerableToBase(Lnet/minecraft/world/damagesource/DamageSource;)Z", at = @At("TAIL"), cancellable = true)
    public void mixin$isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            Entity entity = (Entity) (Object) this;
            for (DamageImmunityAbility a : CommonUtil.listOfType(DamageImmunityAbility.class, CommonUtil.getAbilities(entity))) {
                for (String s : a.damageSources) {
                    if (s.equals(source.getMsgId()) && a.isEnabled()) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }
}
