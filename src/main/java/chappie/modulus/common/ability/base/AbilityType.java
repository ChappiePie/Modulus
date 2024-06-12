package chappie.modulus.common.ability.base;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.HelloWorldAbility;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public record AbilityType(AbilitySupplier supplier) {
    public static final DeferredRegister<AbilityType> ABILITY_TYPES = DeferredRegister.create(new ResourceLocation(Modulus.MODID, "ability_types"), Modulus.MODID);
    public static final Supplier<IForgeRegistry<AbilityType>> REGISTRY = ABILITY_TYPES.makeRegistry(RegistryBuilder::new);

    public Ability create(LivingEntity livingEntity, AbilityBuilder builder) {
        Ability ability = this.supplier.create(livingEntity, builder);
        builder.additionalData.forEach(data -> data.accept(ability));
        return ability;
    }

    public static final RegistryObject<AbilityType> HELLO_WORLD = ABILITY_TYPES.register("hello_world", () -> new AbilityType(HelloWorldAbility::new));

    @FunctionalInterface
    public interface AbilitySupplier {
        Ability create(LivingEntity livingEntity, AbilityBuilder builder);
    }
}
