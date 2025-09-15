package chappie.modulus.common.ability.base.condition;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.util.KeyMap;

public class ScrollCondition extends Condition {

    public KeyMap.KeyType keyType = KeyMap.KeyType.MOUSE_SCROLL_UP;
    protected boolean enabled;

    public ScrollCondition(Ability ability) {
        super(ability, (c) -> c instanceof ScrollCondition k && k.enabled);
    }

    @Override
    public void update() {
        super.update();
        this.enabled = false;
    }

    @Override
    public void keyEvent() {
        super.keyEvent();
        this.enabled = this.ability.keys.isDown(this.keyType);
    }

    public ScrollCondition scrollUp() {
        this.keyType = KeyMap.KeyType.MOUSE_SCROLL_UP;
        return this;
    }

    public ScrollCondition scrollDown() {
        this.keyType = KeyMap.KeyType.MOUSE_SCROLL_DOWN;
        return this;
    }
}
