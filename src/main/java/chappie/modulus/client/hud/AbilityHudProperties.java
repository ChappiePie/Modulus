package chappie.modulus.client.hud;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.common.ability.base.condition.KeyCondition;
import chappie.modulus.util.KeyMap;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Metadata for rendering an ability's icon in the ability bar.
 * Configured via {@code AbilityBuilder.hud(...)}.
 *
 * <pre>{@code
 * AbilityBuilder.of("flight", FLIGHT)
 *     .hud(hud -> hud.texture(MyMod.id("textures/gui/ui.png")).uv(32, 0).autoKey())
 * }</pre>
 */
public class AbilityHudProperties {

    private Identifier texture;
    private int u = 0;
    private int v = 0;
    private int width = 16;
    private int height = 16;
    private Supplier<Integer> backgroundColor = () -> -1;
    private Supplier<KeyMap.KeyType> keyType = null;
    private boolean autoKey = false;

    // ===== Builder-style setters =====

    public AbilityHudProperties texture(Identifier texture) {
        this.texture = texture;
        return this;
    }

    public AbilityHudProperties uv(int u, int v) {
        this.u = u;
        this.v = v;
        return this;
    }

    public AbilityHudProperties size(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public AbilityHudProperties backgroundColor(Supplier<Integer> backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public AbilityHudProperties keyType(Supplier<KeyMap.KeyType> keyType) {
        this.keyType = keyType;
        return this;
    }

    /**
     * Automatically detect the activation key from the first KeyCondition in "enabling".
     */
    public AbilityHudProperties autoKey() {
        this.autoKey = true;
        return this;
    }

    // ===== Getters =====

    public Identifier texture() {
        return texture;
    }

    public int u() {
        return u;
    }

    public int v() {
        return v;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int backgroundColor() {
        return backgroundColor.get();
    }

    /**
     * Resolves the key type for display in the HUD.
     * If autoKey is enabled, scans the ability's "enabling" conditions for a KeyCondition.
     */
    @Nullable
    public KeyMap.KeyType resolveKeyType(Ability ability) {
        if (this.keyType != null) {
            return this.keyType.get();
        }
        if (this.autoKey) {
            for (Condition condition : ability.conditionManager.conditionsFor("enabling")) {
                if (condition instanceof KeyCondition keyCondition) {
                    return keyCondition.keyType;
                }
            }
        }
        return null;
    }
}
