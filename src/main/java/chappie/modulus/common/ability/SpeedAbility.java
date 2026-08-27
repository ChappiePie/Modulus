package chappie.modulus.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.data.CommonAccessors;
import chappie.modulus.util.data.DataAccessor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.awt.*;

/**
 * Generic speed ability with progressive level scaling.
 * Speed level ramps up while the entity moves and drops when it stops.
 *
 * <p>Configurable via DataAccessors:
 * <ul>
 *     <li>{@link #SPEED_LVL} — current speed level (1..MAX)</li>
 *     <li>{@link #MAX_SPEED_LVL} — maximum speed level (default 10)</li>
 * </ul>
 *
 * <p>Subclass to add: trail visuals, collision damage, custom effects.
 *
 * <pre>{@code
 * // Basic usage:
 * AbilityBuilder.of("speed", AbilityType.SPEED)
 *     .change(SpeedAbility.MAX_SPEED_LVL, 15)
 * }</pre>
 */
public class SpeedAbility extends Ability {

    public static final DataAccessor<Integer> SPEED_LVL = new DataAccessor<>("speed_lvl", DataAccessor.DataSerializer.INT);
    public static final DataAccessor<Integer> MAX_SPEED_LVL = new DataAccessor<>("max_speed_lvl", DataAccessor.DataSerializer.INT);

    private final IHasTimer.Cooldown upgradeCooldown = addCooldown();
    private final IHasTimer.Cooldown downgradeCooldown = addCooldown();

    private double prevX, prevZ;
    private boolean wasMoving;

    public SpeedAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(CommonAccessors.COLOR, Color.BLUE);
        this.dataManager.define(SPEED_LVL, 1);
        this.dataManager.define(MAX_SPEED_LVL, 10);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (entity.level().isClientSide()) return;

        if (enabled && !entity.isSwimming() && !entity.isFallFlying()) {
            int speedLevel = this.dataManager.get(SPEED_LVL);
            boolean isMoving = this.checkMoving(entity);

            this.applySpeedAttribute(entity, speedLevel);

            if (isMoving && !entity.isPassenger()) {
                this.onMoving(entity, speedLevel);

                if (this.upgradeCooldown.end() && speedLevel < this.getMaxSpeedLevel()) {
                    this.dataManager.set(SPEED_LVL, speedLevel + 1);
                    this.upgradeCooldown.start(speedLevel * 10);
                }
            } else {
                if (this.downgradeCooldown.end() && speedLevel > 1) {
                    this.dataManager.set(SPEED_LVL, speedLevel - 1);
                    this.downgradeCooldown.start(speedLevel);
                }
            }

            if (entity.isSprinting() && speedLevel > 5 && isMoving) {
                this.onSprintCollision(entity, speedLevel);
            }
        } else {
            this.resetSpeed(entity);
        }
    }

    /**
     * Checks if entity has moved since last tick.
     */
    protected boolean checkMoving(LivingEntity entity) {
        double dx = entity.getX() - this.prevX;
        double dz = entity.getZ() - this.prevZ;
        this.prevX = entity.getX();
        this.prevZ = entity.getZ();
        this.wasMoving = (dx * dx + dz * dz) > 1.0e-6;
        return this.wasMoving;
    }

    /**
     * Returns true if entity was moving last tick.
     */
    public boolean isMoving() {
        return this.wasMoving;
    }

    /**
     * Applies MOVEMENT_SPEED attribute based on current speed level.
     * Override to add additional attributes (e.g. ATTACK_SPEED).
     */
    protected void applySpeedAttribute(LivingEntity entity, int speedLevel) {
        CommonUtil.setAttribute(entity, this.getAttributeId(), Attributes.MOVEMENT_SPEED,
                speedLevel, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    /**
     * Called when speed is reset (ability disabled or entity swimming/flying).
     */
    protected void resetSpeed(LivingEntity entity) {
        this.dataManager.set(SPEED_LVL, 1);
        CommonUtil.setAttribute(entity, this.getAttributeId(), Attributes.MOVEMENT_SPEED,
                0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.downgradeCooldown.timer = this.upgradeCooldown.timer = 0;
    }

    /**
     * Called every tick while moving. Override to add trail effects.
     */
    protected void onMoving(LivingEntity entity, int speedLevel) {
    }

    /**
     * Called when sprinting at high speed and moving. Override to add collision damage.
     */
    protected void onSprintCollision(LivingEntity entity, int speedLevel) {
    }

    /**
     * Returns max speed level. Override to add dynamic bonuses.
     */
    public int getMaxSpeedLevel() {
        return this.dataManager.get(MAX_SPEED_LVL);
    }

    /**
     * Returns current speed level.
     */
    public int getSpeedLevel() {
        return this.dataManager.get(SPEED_LVL);
    }

    /**
     * Identifier used for the attribute modifier.
     */
    protected Identifier getAttributeId() {
        return Identifier.withDefaultNamespace(this.builder.id);
    }
}
