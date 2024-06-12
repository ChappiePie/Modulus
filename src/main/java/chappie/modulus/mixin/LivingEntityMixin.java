package chappie.modulus.mixin;

import chappie.modulus.util.ModAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
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

    @Inject(method = "getJumpBoostPower", at = @At("TAIL"), cancellable = true)
    public void jumpBoostPower(CallbackInfoReturnable<Double> cir) {
        AttributeInstance attributeInstance = ((LivingEntity) (Object) this).getAttribute(ModAttributes.JUMP_BOOST.get());
        if (attributeInstance != null) {
            cir.setReturnValue(cir.getReturnValue() + 0.1F * attributeInstance.getValue());
        }
    }
}
