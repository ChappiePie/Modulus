package chappie.modulus.util;

import java.util.HashMap;

public class KeyMap {
    private final HashMap<KeyType, Boolean> init = new HashMap<>();

    public KeyMap() {
        for (KeyType key : KeyType.values()) {
            this.init.put(key, false);
        }
    }

    public boolean isDown(KeyType key) {
        return this.init.get(key);
    }

    public void setDown(KeyType key, boolean down) {
        this.init.put(key, down);
    }

    public void copyFrom(KeyMap keyMap) {
        this.init.replaceAll((keyType, b) -> keyMap.init.get(keyType));
    }

    public boolean notEquals(KeyMap keyMap) {
        return !this.init.equals(keyMap.init);
    }

    public enum KeyType {
        FIRST, SECOND, THIRD, FOURTH, FIFTH, MOUSE_LEFT(true), MOUSE_RIGHT(true), MOUSE_SCROLL_UP(true), MOUSE_SCROLL_DOWN(true), JUMP, SPRINT, CROUCH;

        public final boolean isMouse;

        KeyType() {
            this(false);
        }

        KeyType(boolean isMouse) {
            this.isMouse = isMouse;
        }
    }
}
