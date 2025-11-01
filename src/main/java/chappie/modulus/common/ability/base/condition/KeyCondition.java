package chappie.modulus.common.ability.base.condition;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.util.KeyMap;
import net.minecraft.nbt.CompoundTag;

public class KeyCondition extends Condition {

    public KeyMap.KeyType keyType = KeyMap.KeyType.FIRST;
    protected Action action = Action.ACTION;
    protected boolean enabled;

    public KeyCondition(Ability ability) {
        super(ability, (c) -> c instanceof KeyCondition k && k.enabled);
    }

    @Override
    public void update() {
        super.update();
        if (this.action == Action.ACTION) {
            this.enabled = false;
        }
    }

    @Override
    public void keyEvent() {
        super.keyEvent();
        if (this.ability.keys.isDown(this.keyType)) {
            this.enabled = this.action != Action.TOGGLE || !this.enabled;
        } else {
            if (this.action == Action.HELD) {
                this.enabled = false;
            }
        }
    }

    public KeyCondition keyType(KeyMap.KeyType keyType) {
        this.keyType = keyType;
        return this;
    }

    public KeyCondition action(Action action) {
        this.action = action;
        return this;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        if (this.action == Action.TOGGLE) {
            tag.putBoolean("enabled", this.enabled);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        if (this.action == Action.TOGGLE) {
            nbt.getBoolean("enabled").ifPresent(enabled -> this.enabled = enabled);
        }
    }

    public enum Action {
        ACTION,
        TOGGLE,
        HELD
    }
}
