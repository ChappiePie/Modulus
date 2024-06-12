package chappie.modulus.common.ability.base.condition;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.capability.PowerCap;

public class AbilityEnabledCondition extends Condition {

    private String abilityName;

    public AbilityEnabledCondition(Ability ability) {
        super(ability, (c) -> {
            if (c instanceof AbilityEnabledCondition condition) {
                PowerCap cap = PowerCap.getCap(ability.entity);
                if (cap != null) {
                    var conditionAbility = cap.getAbility(condition.abilityName);
                    if (conditionAbility != null) {
                        return conditionAbility.isEnabled();
                    }
                }
            }
            return false;
        });
    }

    public AbilityEnabledCondition abilityName(String abilityName) {
        this.abilityName = abilityName;
        return this;
    }

}
