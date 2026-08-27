package chappie.modulus.test;

import chappie.modulus.Modulus;
import chappie.modulus.client.hud.AbilityHudProperties;
import chappie.modulus.common.ability.AttributeModifierAbility;
import chappie.modulus.common.ability.DamageImmunityAbility;
import chappie.modulus.common.ability.DamageResistanceAbility;
import chappie.modulus.common.ability.SpeedAbility;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityType;
import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.common.ability.base.condition.DoubleKeyCondition;
import chappie.modulus.common.ability.base.condition.KeyCondition;
import chappie.modulus.util.KeyMap;
import chappie.modulus.util.ModRegistries;
import chappie.modulus.util.data.CommonAccessors;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.awt.*;
import java.util.List;

/**
 * Test superpowers for development. These are registered only in dev environment
 * and serve as examples + test cases for all Core systems.
 *
 * <p>Use: {@code /superpower @s modulus:test_flight}
 *
 * <p>Available test superpowers:
 * <ul>
 *     <li>{@code modulus:test_flight} — Flight + glow eyes + damage resistance</li>
 *     <li>{@code modulus:test_speed} — Speed ability with progressive scaling</li>
 *     <li>{@code modulus:test_tank} — Attribute modifiers + damage immunity</li>
 *     <li>{@code modulus:test_all} — All core abilities combined</li>
 * </ul>
 */
public class ModulusTestSuperpowers {


    /**
     * Flight + Glow Eyes. Tests: FlightAbility, GlowAbility, DoubleKeyCondition, lifecycle hooks.
     */
    public static final Superpower TEST_FLIGHT = Superpower.builder()
            .add(AbilityBuilder.of("flight", AbilityType.FLIGHT)
                    .condition(a -> new DoubleKeyCondition(a)
                            .shouldStop(() -> a.getEntity().onGround() || a.getEntity().isShiftKeyDown()), "enabling")
                    .hud(a -> new AbilityHudProperties().uv(0, 0).autoKey()))
            .add(AbilityBuilder.of("glow_eyes", AbilityType.GLOW)
                    .change(CommonAccessors.COLOR, Color.CYAN)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.HELD), "enabling")
                    .hud(a -> new AbilityHudProperties().uv(16, 0).autoKey()))
            .passive("damage_resistance", AbilityType.DAMAGE_RESISTANCE, a ->
                    a.dataManager.set(DamageResistanceAbility.AMPLIFIER, 5F))
            .add(AttributeModifierAbility.of("fall_resistance", b ->
                    b.attribute(ModRegistries.FALL_RESISTANCE).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADD_VALUE)))
            .register(Modulus.id("test_flight"));

    /**
     * Speed ability. Tests: SpeedAbility, progressive leveling, KeyCondition TOGGLE.
     */
    public static final Superpower TEST_SPEED = Superpower.builder()
            .add(AbilityBuilder.of("speed", AbilityType.SPEED)
                    .change(CommonAccessors.COLOR, Color.YELLOW)
                    .change(SpeedAbility.MAX_SPEED_LVL, 15)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.TOGGLE), "enabling")
                    .hud(a -> new AbilityHudProperties().uv(16, 0).autoKey()))
            .add(AttributeModifierAbility.of("jump_boost", b ->
                    b.attribute(ModRegistries.JUMP_BOOST).amount(0.5D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("fall_resistance", b ->
                    b.attribute(ModRegistries.FALL_RESISTANCE).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADD_VALUE)))
            .register(Modulus.id("test_speed"));

    /**
     * Tank build. Tests: AttributeModifierAbility, DamageImmunityAbility, DamageResistanceAbility.
     */
    public static final Superpower TEST_TANK = Superpower.builder()
            .passive("damage_resistance", AbilityType.DAMAGE_RESISTANCE, a ->
                    a.dataManager.set(DamageResistanceAbility.AMPLIFIER, 10F))
            .add(AttributeModifierAbility.of("max_health", b ->
                    b.attribute(Attributes.MAX_HEALTH).amount(20.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("armor", b ->
                    b.attribute(Attributes.ARMOR).amount(10.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("attack_damage", b ->
                    b.attribute(Attributes.ATTACK_DAMAGE).amount(5.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(DamageImmunityAbility.of("fire_immunity", List.of(DamageTypeTags.IS_FIRE)))
            .register(Modulus.id("test_tank"));

    /**
     * All abilities combined. Tests: everything together, condition composition, timers.
     */
    public static final Superpower TEST_ALL = Superpower.builder()
            .icon(Modulus.id("textures/gui/test_icons.png"), 0, 0)
            .add(AbilityBuilder.of("flight", AbilityType.FLIGHT)
                    .condition(a -> new DoubleKeyCondition(a)
                            .shouldStop(() -> a.getEntity().onGround() || a.getEntity().isShiftKeyDown()), "enabling")
                    .hud(a -> new AbilityHudProperties().uv(0, 0).autoKey()))
            .add(AbilityBuilder.of("speed", AbilityType.SPEED)
                    .change(CommonAccessors.COLOR, Color.RED)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.FIRST).action(KeyCondition.Action.TOGGLE), "enabling")
                    .hud(a -> new AbilityHudProperties().uv(16, 0).autoKey()))
            .add(AbilityBuilder.of("glow", AbilityType.GLOW)
                    .change(CommonAccessors.COLOR, Color.ORANGE)
                    .condition(a -> new KeyCondition(a).keyType(KeyMap.KeyType.SECOND).action(KeyCondition.Action.HELD), "enabling")
                    .hud(a -> new AbilityHudProperties().uv(32, 0).autoKey()))
            .passive("damage_resistance", AbilityType.DAMAGE_RESISTANCE, a ->
                    a.dataManager.set(DamageResistanceAbility.AMPLIFIER, 8F))
            .add(AttributeModifierAbility.of("max_health", b ->
                    b.attribute(Attributes.MAX_HEALTH).amount(10.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("jump_boost", b ->
                    b.attribute(ModRegistries.JUMP_BOOST).amount(1.0D).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(AttributeModifierAbility.of("fall_resistance", b ->
                    b.attribute(ModRegistries.FALL_RESISTANCE).amount(-Integer.MAX_VALUE).operation(AttributeModifier.Operation.ADD_VALUE)))
            .add(DamageImmunityAbility.of("fire_immunity", List.of(DamageTypeTags.IS_FIRE)))
            .register(Modulus.id("test_all"));

    public static void init() {
        Modulus.LOGGER.info("[Modulus] Registered test superpowers: test_flight, test_speed, test_tank, test_all");
    }
}
