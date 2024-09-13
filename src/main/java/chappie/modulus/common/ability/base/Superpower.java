package chappie.modulus.common.ability.base;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.base.condition.KeyCondition;
import chappie.modulus.util.KeyMap;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;

public class Superpower {
    public static final ResourceKey<Registry<Superpower>> SUPERPOWERS = ResourceKey.createRegistryKey(Modulus.id("superpowers"));
    public static final Registry<Superpower> REGISTRY = FabricRegistryBuilder.createSimple(SUPERPOWERS).buildAndRegister();

    public static final Superpower TEST = Registry.register(REGISTRY, Modulus.id("test"), new Superpower(
            AbilityBuilder.of("bruh", AbilityType.HELLO_WORLD)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST)
                            .action(KeyCondition.Action.HELD), "enabling")
    ));

    private final LinkedList<AbilityBuilder> list;

    public Superpower(LinkedList<AbilityBuilder> list) {
        this.list = list;
    }

    public Superpower(AbilityBuilder... builders) {
        LinkedList<AbilityBuilder> list = new LinkedList<>();
        Collections.addAll(list, builders);
        this.list = list;
    }

    public static void init() {

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
        return Component.translatable("superpower.%s".formatted(Objects.requireNonNull(REGISTRY.getKey(this)).toString().replace(":", ".")));
    }
}
