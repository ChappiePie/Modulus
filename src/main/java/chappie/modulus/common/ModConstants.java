package chappie.modulus.common;

/**
 * Central location for all magic numbers and constants used throughout the mod.
 */
public final class ModConstants {

    /**
     * Multiplier for jump boost attribute when calculating jump power.
     * Each point of jump boost adds 0.1F to the jump height.
     */
    public static final float JUMP_BOOST_MULTIPLIER = 0.1F;

    /**
     * Interval in ticks for mob ability toggling (600 ticks = 30 seconds).
     * Non-player entities will toggle their abilities every 30 seconds.
     */
    public static final int MOB_ABILITY_TOGGLE_INTERVAL = 600;

    /**
     * Minimum throttle interval in ticks for ability synchronization packets.
     * Prevents excessive network traffic from rapid ability state changes.
     */
    public static final int ABILITY_SYNC_THROTTLE_TICKS = 5;

    /**
     * Maximum distance in blocks for particle effects to be visible.
     */
    public static final double PARTICLE_RENDER_DISTANCE = 20.0;

    /**
     * Current NBT format version for PowerCap data serialization.
     * Increment this when changing the NBT structure.
     */
    public static final int CURRENT_NBT_VERSION = 1;

    private ModConstants() {
        throw new AssertionError("ModConstants should not be instantiated");
    }
}
