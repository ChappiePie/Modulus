package chappie.modulus.common.ability.base.condition;

import chappie.modulus.common.ability.base.Ability;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

/**
 * A condition that composes multiple child conditions using AND or OR logic.
 *
 * <pre>{@code
 * // OR: either key pressed
 * .condition(a -> new KeyCondition(a).keyType(FIRST).action(HELD)
 *     .or(new KeyCondition(a).keyType(MOUSE_RIGHT).action(HELD)), "enabling")
 *
 * // AND: key pressed AND in water
 * .condition(a -> new KeyCondition(a).keyType(FIRST).action(HELD)
 *     .and(new Condition(a, c -> a.getEntity().isInWater())), "enabling")
 * }</pre>
 */
public class CompositeCondition extends Condition {

    private final List<Condition> children;
    private final Mode mode;

    public CompositeCondition(Ability ability, List<Condition> children, Mode mode) {
        super(ability, c -> false);
        this.children = children;
        this.mode = mode;
    }

    @Override
    public void init() {
        children.forEach(Condition::init);
    }

    @Override
    public void update() {
        children.forEach(Condition::update);
    }

    @Override
    public void keyEvent() {
        children.forEach(Condition::keyEvent);
    }

    @Override
    public boolean get() {
        return switch (mode) {
            case AND -> children.stream().allMatch(Condition::get);
            case OR -> children.stream().anyMatch(Condition::get);
        };
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < children.size(); i++) {
            tag.put(String.valueOf(i), children.get(i).serializeNBT());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (int i = 0; i < children.size(); i++) {
            int index = i;
            nbt.getCompound(String.valueOf(i)).ifPresent(t -> children.get(index).deserializeNBT(t));
        }
    }

    /**
     * Appends another condition with AND logic.
     */
    public CompositeCondition and(Condition other) {
        List<Condition> newChildren = new ArrayList<>(this.children);
        newChildren.add(other);
        return new CompositeCondition(this.ability, newChildren, Mode.AND);
    }

    /**
     * Appends another condition with OR logic.
     */
    public CompositeCondition or(Condition other) {
        List<Condition> newChildren = new ArrayList<>(this.children);
        newChildren.add(other);
        return new CompositeCondition(this.ability, newChildren, Mode.OR);
    }

    public enum Mode {AND, OR}
}
