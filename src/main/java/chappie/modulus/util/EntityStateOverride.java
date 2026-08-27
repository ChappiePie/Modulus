package chappie.modulus.util;

import java.util.Map;

/**
 * Interface injected into Entity via mixin to allow temporary state overrides.
 * Abilities can override entity getters (xRot, yRot, deltaMovement, etc.) without
 * permanently changing entity state.
 *
 * <p>Usage:
 * <pre>{@code
 * EntityStateOverride override = EntityStateOverride.of(entity);
 * override.setup(Map.of("xRot", 45.0F, "yRot", 90.0F));
 * // ... render or process with overridden values ...
 * override.reset();
 * }</pre>
 *
 * <p>Supported keys (injected via Core mixins):
 * <ul>
 *     <li>"xRot" (Float) — overrides getXRot()</li>
 *     <li>"yRot" (Float) — overrides getYRot()</li>
 *     <li>"deltaMovement" (Vec3) — overrides getDeltaMovement()</li>
 *     <li>"isInWater" (Boolean) — overrides isInWater()</li>
 *     <li>"isFallFlying" (Boolean) — overrides isFallFlying()</li>
 *     <li>"swimAmount" (Float) — overrides getSwimAmount()</li>
 *     <li>"isVisuallySwimming" (Boolean) — overrides isVisuallySwimming()</li>
 *     <li>"fallFlyingTicks" (Integer) — overrides getFallFlyingTicks()</li>
 * </ul>
 */
public interface EntityStateOverride {

    /**
     * Convenience: cast any entity to EntityStateOverride.
     * Returns null if the entity doesn't have the mixin applied (shouldn't happen in normal play).
     */
    static EntityStateOverride of(Object entity) {
        if (entity instanceof EntityStateOverride override) {
            return override;
        }
        return null;
    }

    /**
     * Sets override values. Getters will return these values until reset() is called.
     */
    void modulus$setup(Map<String, Object> overrides);

    /**
     * Clears all overrides. Getters return normal values again.
     */
    void modulus$reset();

    /**
     * Returns the current override map (read-only view for checking active overrides).
     */
    Map<String, Object> modulus$overrides();
}
