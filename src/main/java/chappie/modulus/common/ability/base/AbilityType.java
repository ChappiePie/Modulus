package chappie.modulus.common.ability.base;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.HelloWorldAbility;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;

public record AbilityType(AbilitySupplier supplier) {
    public static final ResourceKey<Registry<AbilityType>> ABILITY_TYPES = ResourceKey.createRegistryKey(Modulus.id("ability_types"));
    public static final Registry<AbilityType> REGISTRY = FabricRegistryBuilder.createSimple(ABILITY_TYPES).buildAndRegister();
    public static final AbilityType HELLO_WORLD = Registry.register(REGISTRY, Modulus.id("hello_world"), new AbilityType(HelloWorldAbility::new));

    public Ability create(LivingEntity livingEntity, AbilityBuilder builder) {
        Ability ability = this.supplier.create(livingEntity, builder);
        builder.additionalData.forEach(data -> data.accept(ability));
        return ability;
    }

    public static void init() {

    }

    @FunctionalInterface
    public interface AbilitySupplier {
        Ability create(LivingEntity livingEntity, AbilityBuilder builder);
    }
}
