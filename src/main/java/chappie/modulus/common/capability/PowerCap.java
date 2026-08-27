package chappie.modulus.common.capability;

import chappie.modulus.Modulus;
import chappie.modulus.api.IAbility;
import chappie.modulus.api.IPowerCap;
import chappie.modulus.common.ModConstants;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.events.AbilityEvents;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class PowerCap implements AutoSyncedComponent, CommonTickingComponent, ComponentV3, IPowerCap {

    public static final ComponentKey<PowerCap> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(Modulus.id("powers"), PowerCap.class);

    private static final int MAX_ABILITIES = 100;
    private static final int MAX_NBT_SIZE = 65536; // 64 KB safety cap
    private final LivingEntity livingEntity;
    private final Map<AbilityBuilder, Ability> abilities = Maps.newLinkedHashMap();
    private final Map<String, Ability> abilitiesById = Maps.newHashMap();
    private final Map<Class<?>, Collection<Ability>> abilitiesByType = Maps.newConcurrentMap();
    private Superpower superpower;

    public PowerCap(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
    }

    @Nullable
    public static PowerCap getCap(Object provider) {
        // No powers for spectators to prevent client desyncs and unnecessary data transfer
        if (provider instanceof ServerPlayer player && player.gameMode != null && player.isSpectator()) {
            return null;
        }
        return KEY.maybeGet(provider).orElse(null);
    }

    @Override
    public Superpower getSuperpower() {
        return superpower;
    }

    @Override
    public void setSuperpower(Superpower superpower) {
        Superpower oldSuperpower = this.superpower;
        this.superpower = superpower;
        this.abilities.values().forEach(value -> {
            value.update(this.livingEntity, false);
            value.onRemove(this.livingEntity);
        });

        clearAbilities();

        if (superpower != null) {
            if (superpower.getBuilders().size() > MAX_ABILITIES) {
                Modulus.LOGGER.error("[Modulus] Superpower has {} abilities — exceeds limit of {}. Truncating.",
                        superpower.getBuilders().size(), MAX_ABILITIES);
            }
            int count = 0;
            for (AbilityBuilder builder : superpower.getBuilders()) {
                if (count++ >= MAX_ABILITIES) break;
                Ability ability = builder.build(this.livingEntity);
                addAbility(builder, ability);
            }
        }

        // Fire superpower changed event (server-side only)
        if (!this.livingEntity.level().isClientSide()) {
            AbilityEvents.SUPERPOWER_CHANGED.invoker().onSuperpowerChanged(this.livingEntity, oldSuperpower, superpower);
        }

        this.syncToAll();
    }

    @Override
    public Collection<Ability> getAbilities() {
        return this.abilities.values();
    }

    @Override
    public Ability getAbility(String key) {
        return this.abilitiesById.get(key);
    }

    /**
     * Gets all abilities of a specific type with O(1) cached lookup.
     * This method caches results to avoid repeated filtering operations.
     *
     * @param type The ability class type to filter by
     * @param <T>  The ability type
     * @return A collection of abilities matching the type
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends IAbility> Collection<T> getAbilitiesByType(Class<T> type) {
        return (Collection<T>) abilitiesByType.computeIfAbsent(type, k ->
                abilities.values().stream()
                        .filter(k::isInstance)
                        .collect(java.util.stream.Collectors.toList())
        );
    }

    public void sync() {
        KEY.sync(this.livingEntity);
    }

    public void syncToAll() {
        this.sync();
        for (LivingEntity livingEntity : this.livingEntity.level().players()) {
            if (livingEntity instanceof ServerPlayer player && this.livingEntity != livingEntity) {
                KEY.sync(player);
            }
        }
    }

    @Override
    public void tick() {
        Collection<Ability> abilities = this.getAbilities();

        for (Ability ability : abilities) {
            // New system: tick timers registered via addTimer()/addCooldown()
            ability.tickTimers();

            // Legacy: only tick IHasTimer if ability hasn't migrated to addTimer()
            if (ability.getTimers().isEmpty() && ability instanceof IHasTimer iHasTimer) {
                iHasTimer.timers().forEach(IHasTimer.Timer::update);
            }

            ability.updateTick(this.livingEntity);
        }
    }

    @Override
    // MC-VERSION-SPECIFIC: ValueInput/ValueOutput replaced readFromNbt/writeToNbt in 1.21.4+
    // In older versions: replace ValueInput with NbtCompound and use readFromNbt(NbtCompound tag)
    public void readData(ValueInput valueInput) {
        this.abilities.clear();
        this.abilitiesById.clear();
        // MC-VERSION-SPECIFIC: CompoundTag.CODEC + Optional-returning read() — 1.21.4+ API
        CompoundTag superpowerTag = valueInput.read("Superpower", CompoundTag.CODEC).orElse(null);
        if (superpowerTag == null || !superpowerTag.contains("Id") || superpowerTag.getString("Id").orElse("").isEmpty()) {
            this.superpower = null;
            return;
        }

        // Guard against oversized NBT payloads
        String rawTag = superpowerTag.toString();
        if (rawTag.length() > MAX_NBT_SIZE) {
            Modulus.LOGGER.error("[Modulus] PowerCap NBT payload exceeds {} bytes — discarding data to prevent crash", MAX_NBT_SIZE);
            this.superpower = null;
            return;
        }

        // Check NBT version
        int version = superpowerTag.getInt("Version").orElse(1);
        if (version > ModConstants.CURRENT_NBT_VERSION) {
            Modulus.LOGGER.error("PowerCap NBT version {} is newer than supported version {}. Data may be lost!",
                    version, ModConstants.CURRENT_NBT_VERSION);
            this.superpower = null;
            return;
        }

        // Migrate old NBT versions if needed
        if (version < ModConstants.CURRENT_NBT_VERSION) {
            superpowerTag = migrateNBT(superpowerTag, version);
        }

        Identifier id = Identifier.tryParse(superpowerTag.getString("Id").orElse(""));
        Superpower superpower = id != null ? Superpower.REGISTRY.getValue(id) : null;
        if (superpower == null) {
            this.superpower = null;
            return;
        }

        this.superpower = superpower;
        this.abilitiesByType.clear();
        CompoundTag abilityData = superpowerTag.getCompoundOrEmpty("Abilities");
        for (String key : abilityData.keySet()) {
            abilityData.getCompound(key).ifPresent(nbt -> {
                AbilityBuilder builder = superpower.getBuilderByName(key);
                if (builder == null) {
                    return;
                }

                Ability ability = builder.build(this.livingEntity);
                ability.deserializeNBT(nbt);
                this.abilities.put(builder, ability);
                this.abilitiesById.put(builder.id, ability);
            });
        }
        this.abilitiesByType.clear(); // Invalidate after all abilities loaded
    }

    /**
     * Migrates NBT data from an older version to the current version.
     *
     * <p><b>NBT Format Version 1 (Current):</b>
     * <pre>
     * PowerCap NBT Structure:
     * {
     *   Version: 1 (int),
     *   Id: "namespace:superpower_id" (string),
     *   Abilities: {
     *     "ability_id_1": {
     *       Data: { ... ability-specific data ... },
     *       Conditions: { ... condition state ... }
     *     },
     *     "ability_id_2": { ... },
     *     ...
     *   }
     * }
     *
     * Ability NBT Structure (nested):
     * {
     *   Data: {
     *     "enabled": true/false,
     *     "custom_field": value,
     *     ...
     *   },
     *   Conditions: {
     *     "ACTIVE": [ { condition1 state }, { condition2 state }, ... ],
     *     "USING": [ ... ],
     *     ...
     *   }
     * }
     * </pre>
     *
     * <p><b>Migration Strategy:</b>
     * When incrementing {@link ModConstants#CURRENT_NBT_VERSION}, add migration
     * logic here to transform old format to new format.
     *
     * <p><b>Example Migration (for future version 2):</b>
     * <pre>{@code
     * if (fromVersion == 1) {
     *     // Version 1 → 2: renamed "enabled" to "active"
     *     CompoundTag abilities = tag.getCompound("Abilities").orElse(new CompoundTag());
     *     for (String key : abilities.keySet()) {
     *         CompoundTag abilityTag = abilities.getCompound(key).orElse(new CompoundTag());
     *         CompoundTag data = abilityTag.getCompound("Data").orElse(new CompoundTag());
     *         if (data.contains("enabled")) {
     *             boolean enabled = data.getBoolean("enabled").orElse(false);
     *             data.remove("enabled");
     *             data.putBoolean("active", enabled);
     *         }
     *         abilityTag.put("Data", data);
     *         abilities.put(key, abilityTag);
     *     }
     *     tag.put("Abilities", abilities);
     *     fromVersion = 2;
     * }
     * }</pre>
     *
     * @param tag         The old NBT tag to migrate
     * @param fromVersion The version to migrate from
     * @return The migrated NBT tag with updated version number
     */
    private CompoundTag migrateNBT(CompoundTag tag, int fromVersion) {
        Modulus.LOGGER.info("Migrating PowerCap NBT from version {} to {}", fromVersion, ModConstants.CURRENT_NBT_VERSION);

        // Add migration logic here when NBT format changes in future versions
        // Example:
        // if (fromVersion == 1) {
        //     // Migrate from version 1 to 2
        //     tag = migrateV1ToV2(tag);
        //     fromVersion = 2;
        // }

        tag.putInt("Version", ModConstants.CURRENT_NBT_VERSION);
        return tag;
    }

    @Override
    public void writeData(ValueOutput valueOutput) {
        CompoundTag superpower = new CompoundTag();
        if (this.superpower != null) {
            superpower.putInt("Version", ModConstants.CURRENT_NBT_VERSION);
            superpower.putString("Id", Objects.requireNonNull(Superpower.REGISTRY.getKey(this.superpower)).toString());

            CompoundTag abilities = new CompoundTag();
            this.abilities.forEach((s, a) -> abilities.put(s.id, a.serializeNBT()));
            superpower.put("Abilities", abilities);
        }
        valueOutput.store("Superpower", CompoundTag.CODEC, superpower);
    }

    /**
     * Clears all ability maps to ensure no stale references remain.
     * This is called when the superpower is changed or removed.
     */
    private void clearAbilities() {
        this.abilities.clear();
        this.abilitiesById.clear();
        this.abilitiesByType.clear();
    }

    /**
     * Adds an ability to the internal maps for quick lookup.
     * This method ensures that all relevant maps are updated consistently.
     *
     * @param builder The AbilityBuilder used to construct the ability
     * @param ability The ability instance
     */
    private void addAbility(AbilityBuilder builder, Ability ability) {
        this.abilities.put(builder, ability);
        this.abilitiesById.put(builder.id, ability);
        this.abilitiesByType.clear(); // Invalidate type cache when abilities change
    }
}
