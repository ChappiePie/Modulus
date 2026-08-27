package chappie.modulus.util.data;

import java.awt.*;

/**
 * Common DataAccessor presets that any superhero mod is likely to need.
 * Use these instead of defining your own to avoid duplication.
 *
 * <pre>{@code
 * this.dataManager.define(CommonAccessors.COLOR, Color.RED);
 * this.dataManager.define(CommonAccessors.DISTANCE, 20);
 * this.dataManager.define(CommonAccessors.SPEED, 1.0F);
 * }</pre>
 */
public final class CommonAccessors {

    public static final DataAccessor<Color> COLOR = new DataAccessor<>("color", DataAccessor.DataSerializer.COLOR);
    public static final DataAccessor<Integer> DISTANCE = new DataAccessor<>("distance", DataAccessor.DataSerializer.INT);
    public static final DataAccessor<Float> SPEED = new DataAccessor<>("speed", DataAccessor.DataSerializer.FLOAT);
    public static final DataAccessor<Float> STRENGTH = new DataAccessor<>("strength", DataAccessor.DataSerializer.FLOAT);

    private CommonAccessors() {
    }
}
