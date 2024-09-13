package chappie.modulus.mixin;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.util.CommonUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(at = @At("HEAD"), method = "setOldPosAndRot")
    public void abilityTick(CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof LivingEntity e) {
            for (Ability ability : CommonUtil.getAbilities(e)) {
                ability.updateTick(e);
            }
        }
    }
}
