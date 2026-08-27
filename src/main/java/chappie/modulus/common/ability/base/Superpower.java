package chappie.modulus.common.ability.base;

import chappie.modulus.Modulus;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.*;

public class Superpower {
    public static final ResourceKey<Registry<Superpower>> SUPERPOWERS = ResourceKey.createRegistryKey(Modulus.id("superpowers"));
    public static final Registry<Superpower> REGISTRY = FabricRegistryBuilder.create(SUPERPOWERS).buildAndRegister();

    private final LinkedList<AbilityBuilder> list;
    private final Set<AbilityBuilder> builderSet;
    private final Map<String, AbilityBuilder> buildersByName;

    // Icon metadata for HUD
    private Identifier iconTexture;
    private int iconU, iconV;

    public Superpower(LinkedList<AbilityBuilder> list) {
        this.list = list;
        this.builderSet = new HashSet<>(list);
        this.buildersByName = new HashMap<>(list.size());
        for (AbilityBuilder builder : list) {
            this.buildersByName.put(builder.id, builder);
        }
    }

    public Superpower(AbilityBuilder... builders) {
        LinkedList<AbilityBuilder> list = new LinkedList<>();
        Collections.addAll(list, builders);
        this.list = list;
        this.builderSet = new HashSet<>(list);
        this.buildersByName = new HashMap<>(list.size());
        for (AbilityBuilder builder : list) {
            this.buildersByName.put(builder.id, builder);
        }
    }

    public static void init() {

    }

    /**
     * Fluent DSL for creating superpowers.
     *
     * <pre>{@code
     * Superpower.builder("homelander")
     *     .icon(MY_TEXTURE, 16, 128)
     *     .add(AbilityBuilder.of("flight", AbilityType.FLIGHT)
     *         .condition(a -> new DoubleKeyCondition(a), "enabling")
     *         .hud(a -> new AbilityHudProperties().texture(TEX).uv(32, 0).autoKey()))
     *     .add(AbilityBuilder.of("lasers", AbilityType.GLOW)
     *         .change(CommonAccessors.COLOR, Color.RED)
     *         .condition(Conditions.key(FIRST, HELD), "enabling"))
     *     .passive("damage_resistance", AbilityType.DAMAGE_RESISTANCE, a -> a
     *         .dataManager.set(DamageResistanceAbility.AMPLIFIER, 8F))
     *     .passive("max_health", AbilityType.ATTRIBUTE_MODIFIER)
     *     .register(MyMod.id("homelander"));
     * }</pre>
     */
    public static SuperpowerBuilder builder() {
        return new SuperpowerBuilder();
    }

    /**
     * O(1) membership check. Use this instead of getBuilders().contains().
     */
    public boolean hasBuilder(AbilityBuilder builder) {
        return builderSet.contains(builder);
    }

    public LinkedList<AbilityBuilder> getBuilders() {
        return list;
    }

    /**
     * O(1) lookup by name. Replaces the old linear scan.
     */
    public AbilityBuilder getBuilderByName(String name) {
        return buildersByName.get(name);
    }

    /**
     * Sets the icon for this superpower (used in ability bar HUD).
     */
    public Superpower icon(Identifier texture, int u, int v) {
        this.iconTexture = texture;
        this.iconU = u;
        this.iconV = v;
        return this;
    }

    @org.jetbrains.annotations.Nullable
    public Identifier iconTexture() {
        return iconTexture;
    }

    public int iconU() {
        return iconU;
    }

    public int iconV() {
        return iconV;
    }

    public static class SuperpowerBuilder {
        private final LinkedList<AbilityBuilder> abilities = new LinkedList<>();
        private Identifier iconTexture;
        private int iconU, iconV;

        /**
         * Sets the superpower icon for the HUD bar.
         */
        public SuperpowerBuilder icon(Identifier texture, int u, int v) {
            this.iconTexture = texture;
            this.iconU = u;
            this.iconV = v;
            return this;
        }

        /**
         * Adds a fully configured AbilityBuilder.
         */
        public SuperpowerBuilder add(AbilityBuilder builder) {
            this.abilities.add(builder);
            return this;
        }

        /**
         * Shorthand: creates and adds a hidden passive ability (no HUD, no conditions).
         */
        public SuperpowerBuilder passive(String id, AbilityType type) {
            this.abilities.add(AbilityBuilder.of(id, type).hide());
            return this;
        }

        /**
         * Shorthand: creates a hidden passive ability with additional configuration.
         */
        public SuperpowerBuilder passive(String id, AbilityType type, java.util.function.Consumer<Ability> config) {
            this.abilities.add(AbilityBuilder.of(id, type).hide().additionalData(config));
            return this;
        }

        /**
         * Builds the Superpower instance (without registering).
         */
        public Superpower build() {
            Superpower sp = new Superpower(abilities);
            if (iconTexture != null) {
                sp.icon(iconTexture, iconU, iconV);
            }
            return sp;
        }

        /**
         * Builds and registers the Superpower in the registry.
         */
        public Superpower register(Identifier id) {
            return Registry.register(Superpower.REGISTRY, id, this.build());
        }
    }

    public Component getDisplayName() {
        return Component.translatable("superpower.%s".formatted(Objects.requireNonNull(REGISTRY.getKey(this)).toString().replace(":", ".")));
    }
}
