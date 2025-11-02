package chappie.modulus.mixin;

import chappie.modulus.common.ability.DamageResistanceAbility;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.ModRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "createLivingAttributes()Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;", require = 1, allow = 1, at = @At("RETURN"))
    private static void mixin$addAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue().add(ModRegistries.JUMP_BOOST);
        cir.getReturnValue().add(ModRegistries.FALL_RESISTANCE);
    }

    @Inject(method = "getJumpBoostPower", at = @At("TAIL"), cancellable = true)
    public void jumpBoostPower(CallbackInfoReturnable<Float> cir) {
        AttributeInstance attributeInstance = ((LivingEntity) (Object) this).getAttribute(ModRegistries.JUMP_BOOST);
        if (attributeInstance != null) {
            cir.setReturnValue((float) (cir.getReturnValue() + 0.1F * attributeInstance.getValue()));
        }
    }

    @Inject(method = "calculateFallDamage", at = @At("RETURN"), cancellable = true)
    public void cancelFallDamage(double fallDistance, float damageMultiplier, CallbackInfoReturnable<Integer> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;

        AttributeInstance fallResistance = entity.getAttribute(ModRegistries.FALL_RESISTANCE);
        if (fallResistance != null) {
            fallResistance.setBaseValue(fallDistance);
            if (fallDistance > fallResistance.getValue()) {
                cir.setReturnValue(0);
            }
        }
        AttributeInstance jumpBoost = entity.getAttribute(ModRegistries.JUMP_BOOST);
        if (jumpBoost != null) {
            cir.setReturnValue(cir.getReturnValue() + Mth.ceil(-jumpBoost.getValue() * damageMultiplier));
        }

    }

    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"), cancellable = true)
    public void mixin$getDamageAfterMagicAbsorb(DamageSource damageSource, float damageAmount, CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        for (DamageResistanceAbility a : CommonUtil.listOfType(DamageResistanceAbility.class, CommonUtil.getAbilities(entity))) {
            if (a.isEnabled()) {
                cir.setReturnValue(a.modifiedDamageAmount(damageSource, damageAmount));
            }
        }
    }
}
