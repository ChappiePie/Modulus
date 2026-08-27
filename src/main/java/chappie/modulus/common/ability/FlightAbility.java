package chappie.modulus.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.data.DataAccessor;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Generic flight ability providing hover and sprint-flight physics.
 * Subclass this to add custom particles, boost mechanics, animations, etc.
 *
 * <p>Configurable via DataAccessors:
 * <ul>
 *     <li>{@link #SPEED} — hover flight speed (default 1.0)</li>
 *     <li>{@link #SPRINT_SPEED} — sprint flight speed (default 2.0)</li>
 * </ul>
 *
 * <p>Synced from client:
 * <ul>
 *     <li>{@link #SPRINTING} — whether the player is sprinting</li>
 *     <li>{@link #FORWARD_IMPULSE} — forward/backward input</li>
 * </ul>
 */
public class FlightAbility extends Ability {

    public static final EntityDimensions FLIGHT_DIMENSIONS = EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.51F);

    public static final DataAccessor<Float> SPEED = new DataAccessor<>("speed", DataAccessor.DataSerializer.FLOAT);
    public static final DataAccessor<Float> SPRINT_SPEED = new DataAccessor<>("sprint_speed", DataAccessor.DataSerializer.FLOAT);
    public static final DataAccessor<Boolean> SPRINTING = new DataAccessor<>("sprinting", DataAccessor.DataSerializer.BOOLEAN);
    public static final DataAccessor<Integer> FORWARD_IMPULSE = new DataAccessor<>("forward_impulse", DataAccessor.DataSerializer.INT);
    public static final DataAccessor<Integer> STRAFE_IMPULSE = new DataAccessor<>("strafe_impulse", DataAccessor.DataSerializer.INT);

    public final IHasTimer.Timer timer = addTimer(() -> 5, this::isEnabled);
    public final IHasTimer.Timer sprintingTimer = addTimer(() -> 10, () -> this.isEnabled() && this.dataManager.get(SPRINTING));
    public final IHasTimer.Timer forwardTimer = addTimer(() -> 7, () -> this.isEnabled() && this.dataManager.get(FORWARD_IMPULSE) > 0);
    public final IHasTimer.Timer backwardTimer = addTimer(() -> 7, () -> this.isEnabled() && this.dataManager.get(FORWARD_IMPULSE) < 0);

    public FlightAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(SPEED, 1.0F);
        this.dataManager.define(SPRINT_SPEED, 2.0F);
        this.dataManager.define(SPRINTING, false, false);
        this.dataManager.define(FORWARD_IMPULSE, 0, false);
        this.dataManager.define(STRAFE_IMPULSE, 0, false);
        this.dataManager.clientWritable(SPRINTING, FORWARD_IMPULSE);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (enabled) {
            this.syncClientInput(entity);
            boolean sprinting = this.dataManager.get(SPRINTING) || entity.isSprinting();

            if (!(entity instanceof Player) && this.enabledTicks == 0) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.4, 0));
            }

            float speed = sprinting ? this.dataManager.get(SPRINT_SPEED) : this.dataManager.get(SPEED);
            speed = this.modifySpeed(entity, speed, sprinting);

            Vec3 vec3;
            if (sprinting) {
                vec3 = this.computeSprintVelocity(entity, speed);
            } else {
                vec3 = this.computeHoverVelocity(entity, speed);
            }
            entity.setDeltaMovement(vec3);
        }
    }

    /**
     * Syncs client-side input (sprinting, forward impulse) to server.
     */
    protected void syncClientInput(LivingEntity entity) {
        if (entity.level().isClientSide()) {
            boolean sprinting = entity.isSprinting();
            if (this.dataManager.get(SPRINTING) != sprinting) {
                this.dataManager.setFromClient(SPRINTING, sprinting);
            }
            int forward = Math.round(entity.zza);
            if (this.dataManager.get(FORWARD_IMPULSE) != forward) {
                this.dataManager.setFromClient(FORWARD_IMPULSE, forward);
            }
        }
    }

    /**
     * Override to add boost or modify speed based on state.
     * Called every tick while flying.
     *
     * @return modified speed value
     */
    protected float modifySpeed(LivingEntity entity, float baseSpeed, boolean sprinting) {
        return baseSpeed;
    }

    /**
     * Computes velocity for sprint-flight (forward along look vector).
     */
    protected Vec3 computeSprintVelocity(LivingEntity entity, float speed) {
        return entity.getDeltaMovement().scale(0.25F).add(entity.getLookAngle().scale(speed));
    }

    /**
     * Computes velocity for hover-flight (WASD movement with gentle bob).
     */
    protected Vec3 computeHoverVelocity(LivingEntity entity, float speed) {
        Vec3 vec3 = entity.getDeltaMovement().multiply(1.05, 0.1F, 1.05);
        vec3 = vec3.add(0, Math.sin(entity.tickCount / 10F) / 50F, 0); // hover bob
        vec3 = vec3.add(inputVector(entity, speed * 2));
        return vec3;
    }

    /**
     * Converts player input (xxa, zza) into a world-space movement vector.
     */
    protected Vec3 inputVector(LivingEntity entity, float speedModifier) {
        double yya = entity.zza == 0 ? 0 : entity.getLookAngle().y;
        yya = entity.zza < 0 ? -yya : yya;
        speedModifier *= 0.01F;
        Vec3 vec = new Vec3(entity.xxa, yya, entity.zza);
        if (entity.xxa == 0 && entity.zza == 0) {
            vec = entity.getDeltaMovement();
        }

        double d0 = vec.lengthSqr();
        if (d0 < 1.0E-7D) {
            return Vec3.ZERO;
        } else {
            Vec3 vec3 = (d0 > 1.0D ? vec.normalize() : vec).scale(speedModifier);
            double f = Math.sin(Math.toRadians(entity.getYRot()));
            double f1 = Math.cos(Math.toRadians(entity.getYRot()));
            return new Vec3(vec3.x * f1 - vec3.z * f, yya * speedModifier * 16, vec3.z * f1 + vec3.x * f);
        }
    }

    /**
     * Returns the dimensions to use when sprint-flying.
     * Override to customize hitbox during flight.
     */
    public EntityDimensions getFlightDimensions(float scale) {
        return FLIGHT_DIMENSIONS.scale(scale);
    }
}
