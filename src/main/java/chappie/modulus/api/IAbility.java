package chappie.modulus.api;

import chappie.modulus.common.ability.base.AbilityBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

/**
 * Public API contract for all abilities.
 *
 * <p>Addon mods should program against this interface rather than the concrete
 * {@code Ability} class. This insulates addons from internal refactors.
 *
 * <h3>Example (addon code):</h3>
 * <pre>{@code
 * IPowerCap cap = IPowerCap.of(player);
 * if (cap != null) {
 *     IAbility flight = cap.getAbility("flight");
 *     if (flight != null && flight.isEnabled()) {
 *         // react to active flight
 *     }
 * }
 * }</pre>
 */
public interface IAbility {

    /**
     * Unique string ID set in {@link AbilityBuilder}.
     */
    String getId();

    /**
     * The entity this ability instance belongs to.
     */
    LivingEntity getEntity();

    /**
     * Returns true if the ability is currently active.
     */
    boolean isEnabled();

    /**
     * Returns true if the ability should not appear in the HUD.
     */
    boolean isHidden();

    /**
     * Ticks elapsed since the ability was last enabled (resets to 0 when disabled).
     */
    int getEnabledTicks();

    /**
     * The builder that configured this ability — safe read-only access to metadata.
     */
    AbilityBuilder getBuilder();

    /**
     * Serializes this ability's state to NBT.
     */
    CompoundTag serializeNBT();

    /**
     * Deserializes this ability's state from NBT.
     */
    void deserializeNBT(CompoundTag tag);
}
