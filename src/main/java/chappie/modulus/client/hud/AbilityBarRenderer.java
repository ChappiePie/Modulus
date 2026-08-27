package chappie.modulus.client.hud;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.util.KeyMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.util.Collection;

/**
 * Default ability bar renderer provided by Core.
 * Renders ability icons based on {@link AbilityHudProperties} and superpower icon.
 *
 * <p>Can be disabled by mods that want to provide their own HUD:
 * <pre>{@code
 * AbilityBarRenderer.setEnabled(false);
 * }</pre>
 */
public class AbilityBarRenderer {

    private static final int ICON_SIZE = 16;
    private static final int SLOT_SIZE = 20;
    private static final int GAP = 2;
    private static boolean enabled = true;

    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable the default ability bar renderer.
     * Disable this if your mod provides its own ability HUD.
     */
    public static void setEnabled(boolean enabled) {
        AbilityBarRenderer.enabled = enabled;
    }

    /**
     * Called by Core's HUD render event. Renders the default ability bar.
     */
    public static void render(GuiGraphicsExtractor graphics, float partialTick, int screenWidth, int screenHeight) {
        if (!enabled) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        PowerCap cap = PowerCap.getCap(mc.player);
        if (cap == null || cap.getSuperpower() == null) return;

        Superpower superpower = cap.getSuperpower();
        Collection<Ability> abilities = cap.getAbilities();

        int x = 7;
        int y = 7;
        int bgColor = ARGB.color(127, 0x282828);
        int textColor = ARGB.color(200, 0xFFFFFF);

        // Render superpower icon
        graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, bgColor);
        if (superpower.iconTexture() != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, superpower.iconTexture(),
                    x + 2, y + 2, superpower.iconU(), superpower.iconV(), ICON_SIZE, ICON_SIZE, 256, 256);
        } else {
            // Placeholder: nether star
            graphics.item(net.minecraft.world.item.Items.NETHER_STAR.getDefaultInstance(), x + 2, y + 2);
        }

        // Render ability icons with gap between each
        int currentY = y + SLOT_SIZE + GAP;
        for (Ability ability : abilities) {
            if (ability.isHidden()) continue;
            AbilityHudProperties hud = ability.getHudProperties();
            if (hud == null) continue;

            float alpha = ability.isEnabled() ? 0.75F : 0.25F;
            int slotColor = ARGB.color((int) (alpha * 127), 0x282828);

            // Background slot
            graphics.fill(x, currentY, x + SLOT_SIZE, currentY + SLOT_SIZE, slotColor);

            // Enabled border
            if (ability.isEnabled()) {
                int borderColor = hud.backgroundColor() != -1
                        ? ARGB.color((int) (alpha * 255), hud.backgroundColor())
                        : ARGB.color((int) (alpha * 200), 0x44FF44);
                graphics.fill(x, currentY, x + SLOT_SIZE, currentY + 1, borderColor);
                graphics.fill(x, currentY + SLOT_SIZE - 1, x + SLOT_SIZE, currentY + SLOT_SIZE, borderColor);
                graphics.fill(x, currentY, x + 1, currentY + SLOT_SIZE, borderColor);
                graphics.fill(x + SLOT_SIZE - 1, currentY, x + SLOT_SIZE, currentY + SLOT_SIZE, borderColor);
            }

            // Icon or placeholder
            if (hud.texture() != null) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, hud.texture(),
                        x + 2, currentY + 2, hud.u(), hud.v(), hud.width(), hud.height(), 256, 256,
                        ARGB.colorFromFloat(alpha, 1F, 1F, 1F));
            } else {
                // Placeholder: blaze powder
                graphics.item(net.minecraft.world.item.Items.BLAZE_POWDER.getDefaultInstance(), x + 2, currentY + 2);
            }

            // Key hint (to the right of the slot)
            KeyMap.KeyType keyType = hud.resolveKeyType(ability);
            if (keyType != null) {
                Component key = getKeyName(keyType);
                graphics.text(mc.font, key, x + SLOT_SIZE + 3, currentY + 6, textColor, true);
            }

            currentY += SLOT_SIZE + GAP;
        }
    }

    private static Component getKeyName(KeyMap.KeyType keyType) {
        if (keyType.isMouse) {
            return Component.literal(switch (keyType) {
                case MOUSE_LEFT -> "LMB";
                case MOUSE_RIGHT -> "RMB";
                case MOUSE_SCROLL_UP -> "↑";
                case MOUSE_SCROLL_DOWN -> "↓";
                default -> "?";
            });
        }
        return chappie.modulus.client.ClientEvents.getMappingFromType(keyType).getTranslatedKeyMessage();
    }
}
