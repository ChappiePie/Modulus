package chappie.modulus.common.ability.base.condition;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.util.KeyMap;
import net.minecraft.nbt.CompoundTag;

import java.util.function.Supplier;

public class DoubleKeyCondition extends Condition {
    public KeyMap.KeyType keyType = KeyMap.KeyType.JUMP;
    private Supplier<Boolean> shouldStop = () -> false;
    private boolean isPressed, enabled;
    private int keyTriggerTime;

    public DoubleKeyCondition(Ability ability) {
        super(ability, (c) -> c instanceof DoubleKeyCondition k && k.enabled);
    }

    @Override
    public void update() {
        super.update();
        if (this.keyTriggerTime > 0) {
            --this.keyTriggerTime;
        }
        this.isPressed = this.ability.keys.isDown(this.keyType);
        if (this.shouldStop.get()) {
            this.enabled = false;
        }
    }

    @Override
    public void keyEvent() {
        super.keyEvent();
        if (!this.isPressed && this.ability.keys.isDown(this.keyType) && !this.shouldStop.get()) {
            if (this.keyTriggerTime == 0) {
                this.keyTriggerTime = 8;
            } else {
                this.enabled = !this.enabled;
                this.keyTriggerTime = 0;
            }
        }
    }

    public DoubleKeyCondition shouldStop(Supplier<Boolean> shouldStop) {
        this.shouldStop = shouldStop;
        return this;
    }

    public DoubleKeyCondition keyType(KeyMap.KeyType keyType) {
        this.keyType = keyType;
        return this;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = super.serializeNBT();
        tag.putBoolean("enabled", this.enabled);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt);
        this.enabled = nbt.getBoolean("enabled");
    }
}
