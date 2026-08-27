package chappie.modulus.common.ability.base.condition;

import chappie.modulus.common.ability.base.Ability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

public class Condition {

    protected final Ability ability;
    private final Predicate<Condition> predicate;
    protected boolean creative, invert;

    public Condition(Ability ability, Predicate<Condition> predicate) {
        this.ability = ability;
        this.predicate = predicate;
    }

    public void init() {
    }

    public void update() {
    }

    public void keyEvent() {

    }

    public void invert() {
        this.invert = true;
    }

    public void availableInCreative() {
        this.creative = true;
    }

    public boolean get() {
        if (this.ability.getEntity() instanceof Player player && player.isCreative() && this.creative) {
            return true;
        }
        return this.invert != this.predicate.test(this);
    }

    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    public void deserializeNBT(CompoundTag nbt) {
    }

    // ===== Composition =====

    /**
     * Creates a composite condition: this AND other.
     */
    public CompositeCondition and(Condition other) {
        return new CompositeCondition(this.ability, java.util.List.of(this, other), CompositeCondition.Mode.AND);
    }

    /**
     * Creates a composite condition: this OR other.
     */
    public CompositeCondition or(Condition other) {
        return new CompositeCondition(this.ability, java.util.List.of(this, other), CompositeCondition.Mode.OR);
    }

    /**
     * Returns a new condition that inverts this one's result (without mutating the original).
     */
    public Condition not() {
        Condition self = this;
        return new Condition(this.ability, c -> !self.get()) {
            @Override
            public void init() {
                self.init();
            }

            @Override
            public void update() {
                self.update();
            }

            @Override
            public void keyEvent() {
                self.keyEvent();
            }

            @Override
            public CompoundTag serializeNBT() {
                return self.serializeNBT();
            }

            @Override
            public void deserializeNBT(CompoundTag nbt) {
                self.deserializeNBT(nbt);
            }
        };
    }
}
