package chappie.modulus.common.ability.base;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.AttributeModifierAbility;
import chappie.modulus.common.ability.DamageImmunityAbility;
import chappie.modulus.common.ability.DamageResistanceAbility;
import chappie.modulus.common.ability.HelloWorldAbility;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;

import java.util.Objects;

public record AbilityType(AbilitySupplier supplier) {
    public static final ResourceKey<Registry<AbilityType>> ABILITY_TYPES = ResourceKey.createRegistryKey(Modulus.id("ability_types"));
    public static final Registry<AbilityType> REGISTRY = FabricRegistryBuilder.createSimple(ABILITY_TYPES).buildAndRegister();

    public static final AbilityType HELLO_WORLD = AbilityType.register("hello_world", HelloWorldAbility::new);
    public static final AbilityType ATTRIBUTE_MODIFIER = AbilityType.register("attribute_modifier", AttributeModifierAbility::new);
    public static final AbilityType DAMAGE_IMMUNITY = AbilityType.register("damage_immunity", DamageImmunityAbility::new);
    public static final AbilityType DAMAGE_RESISTANCE = AbilityType.register("damage_resistance", DamageResistanceAbility::new);

    public Ability create(LivingEntity livingEntity, AbilityBuilder builder) {
        Ability ability = this.supplier.create(livingEntity, builder);
        builder.additionalData.forEach(data -> data.accept(ability));
        return ability;
    }

    public Component displayName() {
        return Component.translatable("abilities.%s".formatted(Objects.requireNonNull(REGISTRY.getKey(this)).toString().replace(":", ".")));
    }

    private static <T extends AbilitySupplier> AbilityType register(String id, T ability) {
        return Registry.register(AbilityType.REGISTRY, Modulus.id(id), new AbilityType(ability));
    }

    public static void init() {

    }

    @FunctionalInterface
    public interface AbilitySupplier {
        Ability create(LivingEntity livingEntity, AbilityBuilder builder);
    }
}
