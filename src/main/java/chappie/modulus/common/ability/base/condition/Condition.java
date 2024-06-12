package chappie.modulus.common.ability.base.condition;

import chappie.modulus.common.ability.base.Ability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.function.Predicate;

public class Condition {

    private final Predicate<Condition> predicate;
    protected final Ability ability;
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
        if (this.ability.entity instanceof Player player && player.isCreative() && this.creative) {
            return true;
        }
        return this.invert != this.predicate.test(this);
    }

    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    public void deserializeNBT(CompoundTag nbt) {
    }
}
