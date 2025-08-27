package chappie.modulus.mixin;

import chappie.modulus.common.ability.DamageImmunityAbility;
import chappie.modulus.util.CommonUtil;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
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
                for (TagKey<DamageType> tag : a.damageSourceTags) {
                    if (source.is(tag) && a.isEnabled()) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }

    @Inject(method = "fireImmune", at = @At("TAIL"), cancellable = true)
    public void mixin$displayFireAnimation(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (!cir.getReturnValue() && entity.level() != null) {
            for (DamageImmunityAbility a : CommonUtil.listOfType(DamageImmunityAbility.class, CommonUtil.getAbilities(entity))) {
                for (TagKey<DamageType> s : a.damageSourceTags) {
                    if (a.isEnabled() && s.equals(DamageTypeTags.IS_FIRE)) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }
}
