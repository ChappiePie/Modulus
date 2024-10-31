package chappie.modulus.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityType;
import chappie.modulus.util.CommonUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;
import java.util.function.Consumer;

public class AttributeModifierAbility extends Ability {
    public final AttributeBuilder attributeBuilder = new AttributeBuilder();

    public AttributeModifierAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    public static AbilityBuilder of(String id, Consumer<AttributeBuilder> consumer) {
        return of(id, consumer, true);
    }

    public static AbilityBuilder of(String id, Consumer<AttributeBuilder> consumer, boolean hidden) {
        AbilityBuilder abilityBuilder = AbilityBuilder.of(id, AbilityType.ATTRIBUTE_MODIFIER).additionalData(a -> {
            if (a instanceof AttributeModifierAbility ability) {
                consumer.accept(ability.attributeBuilder);
            }
        });
        if (hidden) {
            abilityBuilder.hide();
        }
        return abilityBuilder;
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        CommonUtil.setAttribute(entity, this.builder.id, this.attributeBuilder.attribute,
                this.attributeBuilder.uuid, enabled ? this.attributeBuilder.amount : 0, this.attributeBuilder.operation);
        entity.setHealth(entity.getHealth());
    }

    public static class AttributeBuilder {
        protected final UUID uuid = UUID.randomUUID();
        protected Attribute attribute;
        protected double amount;
        protected AttributeModifier.Operation operation;

        public AttributeBuilder attribute(Attribute attribute) {
            this.attribute = attribute;
            return this;
        }

        public AttributeBuilder amount(double amount) {
            this.amount = amount;
            return this;
        }

        public AttributeBuilder operation(AttributeModifier.Operation operation) {
            this.operation = operation;
            return this;
        }
    }
}