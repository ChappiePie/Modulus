package chappie.modulus.api;

import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.common.capability.PowerCap;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Public API contract for the power capability attached to every {@link LivingEntity}.
 *
 * <p>Obtain an instance via {@link #of(Object)}:
 * <pre>{@code
 * IPowerCap cap = IPowerCap.of(player);
 * if (cap != null) {
 *     Superpower sp = cap.getSuperpower();
 * }
 * }</pre>
 */
public interface IPowerCap {

    /**
     * Returns the {@link IPowerCap} for the given entity or capability provider,
     * or {@code null} if the entity has no power capability.
     */
    @Nullable
    static IPowerCap of(Object provider) {
        return PowerCap.getCap(provider);
    }

    /**
     * Returns the currently active superpower, or {@code null} if none is set.
     */
    @Nullable
    Superpower getSuperpower();

    /**
     * Sets (or removes) the active superpower.
     * Pass {@code null} to strip all abilities from the entity.
     */
    void setSuperpower(@Nullable Superpower superpower);

    /**
     * Returns a live, unmodifiable view of all ability instances.
     */
    Collection<? extends IAbility> getAbilities();

    /**
     * Looks up a specific ability by its string ID.
     *
     * @param id the ability ID as declared in its {@code AbilityBuilder}
     * @return the ability instance, or {@code null} if not found
     */
    @Nullable
    IAbility getAbility(String id);

    /**
     * Returns all abilities of the given concrete type.
     * Useful for cross-ability interactions without hard-coding class casts.
     *
     * <pre>{@code
     * cap.getAbilitiesByType(FlightAbility.class).forEach(f -> f.headTiltContribution += 0.5f);
     * }</pre>
     *
     * @param type the ability class to filter by
     * @param <T>  the ability type
     * @return collection of matching abilities (empty if none)
     */
    <T extends IAbility> Collection<T> getAbilitiesByType(Class<T> type);
}
