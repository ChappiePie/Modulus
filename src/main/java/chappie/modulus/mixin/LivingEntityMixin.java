package chappie.modulus.mixin;

import chappie.modulus.common.ModConstants;
import chappie.modulus.common.ability.DamageResistanceAbility;
import chappie.modulus.common.ability.FlightAbility;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.ModRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    public abstract float getScale();

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
            cir.setReturnValue((float) (cir.getReturnValue() + ModConstants.JUMP_BOOST_MULTIPLIER * attributeInstance.getValue()));
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
        for (DamageResistanceAbility a : CommonUtil.getAbilitiesByType(DamageResistanceAbility.class, entity)) {
            if (a.isEnabled()) {
                cir.setReturnValue(a.modifiedDamageAmount(damageSource, damageAmount));
            }
        }
    }

    // ===== Flight =====

    @Inject(method = "getDimensions", at = @At("TAIL"), cancellable = true)
    public void modulus$flightDimensions(Pose pPose, CallbackInfoReturnable<EntityDimensions> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.isAlive() && entity instanceof Player) {
            for (FlightAbility ability : CommonUtil.getAbilitiesByType(FlightAbility.class, entity)) {
                if (entity.isSprinting() && ability.isEnabled()) {
                    cir.setReturnValue(ability.getFlightDimensions(this.getScale()));
                }
            }
        }
    }

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    public void modulus$cancelFlightFallDamage(double fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        for (FlightAbility a : CommonUtil.getAbilitiesByType(FlightAbility.class, entity)) {
            if (a.isEnabled()) {
                cir.setReturnValue(true); // Cancel fall damage while flying
            }
        }
    }

    // ===== EntityStateOverride: LivingEntity-specific overrides =====

    @Inject(method = "isFallFlying", at = @At("TAIL"), cancellable = true)
    public void modulus$isFallFlying(CallbackInfoReturnable<Boolean> cir) {
        var map = ((chappie.modulus.util.EntityStateOverride) this).modulus$overrides();
        if (map.containsKey("isFallFlying")) {
            cir.setReturnValue((boolean) map.get("isFallFlying"));
        }
    }

    @Inject(method = "getSwimAmount", at = @At("TAIL"), cancellable = true)
    public void modulus$getSwimAmount(CallbackInfoReturnable<Float> cir) {
        var map = ((chappie.modulus.util.EntityStateOverride) this).modulus$overrides();
        if (map.containsKey("swimAmount")) {
            cir.setReturnValue((float) map.get("swimAmount"));
        }
    }

    @Inject(method = "isVisuallySwimming", at = @At("TAIL"), cancellable = true)
    public void modulus$isVisuallySwimming(CallbackInfoReturnable<Boolean> cir) {
        var map = ((chappie.modulus.util.EntityStateOverride) this).modulus$overrides();
        if (map.containsKey("isVisuallySwimming")) {
            cir.setReturnValue((boolean) map.get("isVisuallySwimming"));
        }
    }

    @Inject(method = "getFallFlyingTicks", at = @At("TAIL"), cancellable = true)
    public void modulus$getFallFlyingTicks(CallbackInfoReturnable<Integer> cir) {
        var map = ((chappie.modulus.util.EntityStateOverride) this).modulus$overrides();
        if (map.containsKey("fallFlyingTicks")) {
            cir.setReturnValue((int) map.get("fallFlyingTicks"));
        }
    }
}
