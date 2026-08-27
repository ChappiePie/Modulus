package chappie.modulus.util;

import java.util.EnumMap;

public class KeyMap {
    private final EnumMap<KeyType, Boolean> map = new EnumMap<>(KeyType.class);

    public KeyMap() {
        for (KeyType key : KeyType.values()) {
            this.map.put(key, false);
        }
    }

    public boolean isDown(KeyType key) {
        return this.map.get(key);
    }

    public void setDown(KeyType key, boolean down) {
        this.map.put(key, down);
    }

    public void copyFrom(KeyMap keyMap) {
        this.map.putAll(keyMap.map);
    }

    public boolean notEquals(KeyMap keyMap) {
        return !this.map.equals(keyMap.map);
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
