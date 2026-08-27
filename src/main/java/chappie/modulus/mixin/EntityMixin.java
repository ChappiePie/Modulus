package chappie.modulus.mixin;

import chappie.modulus.common.ability.DamageImmunityAbility;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.EntityStateOverride;
import com.google.common.collect.Maps;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Entity.class)
public class EntityMixin implements EntityStateOverride {

    @Unique
    private final Map<String, Object> modulus$overrideMap = Maps.newHashMap();
    @Shadow
    public float xRotO;
    @Shadow
    public float yRotO;

    @Override
    public void modulus$setup(Map<String, Object> overrides) {
        this.modulus$overrideMap.putAll(overrides);
    }

    @Override
    public void modulus$reset() {
        this.modulus$overrideMap.clear();
    }

    @Override
    public Map<String, Object> modulus$overrides() {
        return this.modulus$overrideMap;
    }

    // ===== State override injections =====

    @Inject(method = "getXRot*", at = @At("TAIL"), cancellable = true)
    public void modulus$getXRot(CallbackInfoReturnable<Float> cir) {
        if (modulus$overrideMap.containsKey("xRot")) {
            float val = (float) modulus$overrideMap.get("xRot");
            this.xRotO = val;
            cir.setReturnValue(val);
        }
    }

    @Inject(method = "getYRot*", at = @At("TAIL"), cancellable = true)
    public void modulus$getYRot(CallbackInfoReturnable<Float> cir) {
        if (modulus$overrideMap.containsKey("yRot")) {
            float val = (float) modulus$overrideMap.get("yRot");
            this.yRotO = val;
            cir.setReturnValue(val);
        }
    }

    @Inject(method = "getDeltaMovement", at = @At("TAIL"), cancellable = true)
    public void modulus$getDeltaMovement(CallbackInfoReturnable<Vec3> cir) {
        if (modulus$overrideMap.containsKey("deltaMovement")) {
            cir.setReturnValue((Vec3) modulus$overrideMap.get("deltaMovement"));
        }
    }

    @Inject(method = "isInWater", at = @At("TAIL"), cancellable = true)
    public void modulus$isInWater(CallbackInfoReturnable<Boolean> cir) {
        if (modulus$overrideMap.containsKey("isInWater")) {
            cir.setReturnValue((boolean) modulus$overrideMap.get("isInWater"));
        }
    }

    // ===== Existing damage immunity =====

    @Inject(method = "isInvulnerableToBase(Lnet/minecraft/world/damagesource/DamageSource;)Z", at = @At("TAIL"), cancellable = true)
    public void mixin$isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            Entity entity = (Entity) (Object) this;
            for (DamageImmunityAbility a : CommonUtil.getAbilitiesByType(DamageImmunityAbility.class, entity)) {
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
            for (DamageImmunityAbility a : CommonUtil.getAbilitiesByType(DamageImmunityAbility.class, entity)) {
                for (TagKey<DamageType> s : a.damageSourceTags) {
                    if (s.equals(DamageTypeTags.IS_FIRE) && a.isEnabled()) {
                        cir.setReturnValue(true);
                    }
                }
            }
        }
    }
}
