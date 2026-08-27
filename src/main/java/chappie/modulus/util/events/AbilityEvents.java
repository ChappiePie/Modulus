package chappie.modulus.util.events;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.Superpower;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;

/**
 * Events fired during ability lifecycle and superpower changes.
 *
 * <h3>Cancelable events</h3>
 * {@link #BEFORE_ABILITY_ACTIVATE} returns {@code boolean} — return {@code false} to cancel.
 *
 * <h3>Usage example</h3>
 * <pre>{@code
 * AbilityEvents.BEFORE_ABILITY_ACTIVATE.register((ability, entity) -> {
 *     // cancel activation if entity has weakness effect
 *     if (entity.hasEffect(MobEffects.WEAKNESS)) return false;
 *     return true;
 * });
 *
 * AbilityEvents.ENERGY_DEPLETED.register((ability, entity) -> {
 *     entity.sendSystemMessage(Component.literal("Energy depleted!"));
 * });
 * }</pre>
 */
public final class AbilityEvents {

    public static final Event<AbilityEnabled> ABILITY_ENABLED =
            EventFactory.createArrayBacked(AbilityEnabled.class,
                    callbacks -> (ability, entity) -> {
                        for (AbilityEnabled cb : callbacks) cb.onAbilityEnabled(ability, entity);
                    });

    public static final Event<AbilityDisabled> ABILITY_DISABLED =
            EventFactory.createArrayBacked(AbilityDisabled.class,
                    callbacks -> (ability, entity) -> {
                        for (AbilityDisabled cb : callbacks) cb.onAbilityDisabled(ability, entity);
                    });

    public static final Event<SuperpowerChanged> SUPERPOWER_CHANGED =
            EventFactory.createArrayBacked(SuperpowerChanged.class,
                    callbacks -> (entity, oldSp, newSp) -> {
                        for (SuperpowerChanged cb : callbacks) cb.onSuperpowerChanged(entity, oldSp, newSp);
                    });

    public static final Event<AbilityTick> ABILITY_TICK =
            EventFactory.createArrayBacked(AbilityTick.class,
                    callbacks -> (ability, entity) -> {
                        for (AbilityTick cb : callbacks) cb.onAbilityTick(ability, entity);
                    });

    public static final Event<AbilityLevelUp> ABILITY_LEVEL_UP =
            EventFactory.createArrayBacked(AbilityLevelUp.class,
                    callbacks -> (ability, entity, level) -> {
                        for (AbilityLevelUp cb : callbacks) cb.onLevelUp(ability, entity, level);
                    });

    /**
     * Fired before an ability activates (transitions from disabled → enabled).
     * Return {@code false} from any listener to cancel the activation.
     * Fired server-side only.
     */
    public static final Event<BeforeAbilityActivate> BEFORE_ABILITY_ACTIVATE =
            EventFactory.createArrayBacked(BeforeAbilityActivate.class,
                    callbacks -> (ability, entity) -> {
                        for (BeforeAbilityActivate cb : callbacks) {
                            if (!cb.onBeforeActivate(ability, entity)) return false;
                        }
                        return true;
                    });

    /**
     * Fired after an ability successfully activates. Server-side only.
     */
    public static final Event<AfterAbilityActivate> AFTER_ABILITY_ACTIVATE =
            EventFactory.createArrayBacked(AfterAbilityActivate.class,
                    callbacks -> (ability, entity) -> {
                        for (AfterAbilityActivate cb : callbacks) cb.onAfterActivate(ability, entity);
                    });

    /**
     * Fired every time a condition is evaluated. Useful for debug overlays.
     * {@code result} is the raw outcome before creative-mode bypass.
     */
    public static final Event<ConditionChecked> CONDITION_CHECKED =
            EventFactory.createArrayBacked(ConditionChecked.class,
                    callbacks -> (ability, entity, conditionClass, result) -> {
                        for (ConditionChecked cb : callbacks)
                            cb.onConditionChecked(ability, entity, conditionClass, result);
                    });

    @FunctionalInterface
    public interface AbilityEnabled {
        void onAbilityEnabled(Ability a, LivingEntity e);
    }

    @FunctionalInterface
    public interface AbilityDisabled {
        void onAbilityDisabled(Ability a, LivingEntity e);
    }

    @FunctionalInterface
    public interface SuperpowerChanged {
        void onSuperpowerChanged(LivingEntity e, Superpower oldSp, Superpower newSp);
    }

    @FunctionalInterface
    public interface AbilityTick {
        void onAbilityTick(Ability a, LivingEntity e);
    }

    @FunctionalInterface
    public interface AbilityLevelUp {
        void onLevelUp(Ability a, LivingEntity e, int newLevel);
    }

    @FunctionalInterface
    public interface BeforeAbilityActivate {
        boolean onBeforeActivate(Ability a, LivingEntity e);
    }

    @FunctionalInterface
    public interface AfterAbilityActivate {
        void onAfterActivate(Ability a, LivingEntity e);
    }

    @FunctionalInterface
    public interface ConditionChecked {
        void onConditionChecked(Ability a, LivingEntity e, Class<?> conditionClass, boolean result);
    }
}
