package chappie.modulus.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.util.data.DataAccessor;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public class DamageResistanceAbility extends Ability {
    public static final DataAccessor<Float> AMPLIFIER = new DataAccessor<>("amplifier", DataAccessor.DataSerializer.FLOAT);

    public DamageResistanceAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(AMPLIFIER, 1.0F);
    }

    public float modifiedDamageAmount(DamageSource damageSource, float damageAmount) {
        return damageAmount * (1.0F / this.dataManager.get(DamageResistanceAbility.AMPLIFIER));
    }
}
