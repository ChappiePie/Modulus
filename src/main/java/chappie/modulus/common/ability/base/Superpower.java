package chappie.modulus.common.ability.base;

import chappie.modulus.Modulus;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.Collections;
import java.util.LinkedList;
import java.util.function.Supplier;

public class Superpower {
    public static final DeferredRegister<Superpower> SUPERPOWERS = DeferredRegister.create(new ResourceLocation(Modulus.MODID, "superpowers"), Modulus.MODID);
    public static final Supplier<IForgeRegistry<Superpower>> REGISTRY = SUPERPOWERS.makeRegistry(RegistryBuilder::new);

    private final LinkedList<AbilityBuilder> list;

    public Superpower(LinkedList<AbilityBuilder> list) {
        this.list = list;
    }

    public Superpower(AbilityBuilder... builders) {
        LinkedList<AbilityBuilder> list = new LinkedList<>();
        Collections.addAll(list, builders);
        this.list = list;
    }

    public LinkedList<AbilityBuilder> getBuilders() {
        return list;
    }

    public AbilityBuilder getBuilderByName(String name) {
        for (AbilityBuilder abilityBuilder : this.list) {
            if (abilityBuilder.id.equals(name)) {
                return abilityBuilder;
            }
        }
        return null;
    }

    public Component getDisplayName() {
        return Component.translatable("superpower.%s".formatted(REGISTRY.get().getKey(this)));
    }
}
