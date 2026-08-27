package chappie.modulus.client;

import chappie.modulus.Modulus;
import chappie.modulus.client.gui.ModulusMainScreen;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.server.ServerKeysInput;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.KeyMap;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public class ClientEvents {

    public static final KeyMap KEYS = new KeyMap();
    public static final KeyMapping[] KEY_MAPPINGS = new KeyMapping[]{
            new AbilityKeyMapping(0, GLFW.GLFW_KEY_V),
            new AbilityKeyMapping(1, GLFW.GLFW_KEY_B),
            new AbilityKeyMapping(2, GLFW.GLFW_KEY_N),
            new AbilityKeyMapping(3, GLFW.GLFW_KEY_M),
            new AbilityKeyMapping(4, GLFW.GLFW_KEY_COMMA)
    };

    public static void onGuiInit(Minecraft client, Screen screen, int scaledWidth, int scaledHeight) {
        if (screen instanceof TitleScreen || screen instanceof PauseScreen) {
            int x = 0;
            int y = 0;
            boolean b = true;

            if (screen instanceof PauseScreen) {
                boolean b1 = false;
                for (GuiEventListener guiEventListener : screen.children()) {
                    if (guiEventListener instanceof Button button && button.getMessage().equals(Component.translatable("menu.options"))) {
                        y = button.getY();
                        b1 = true;
                    }
                }
                b = b1;
                // Find leftmost button X for pause screen
                x = screen.width;
                for (GuiEventListener guiEventListener : screen.children()) {
                    if (guiEventListener instanceof Button button) {
                        if (button.getX() < x) {
                            x = button.getX();
                        }
                    }
                }
                x -= 24;
            } else {
                // TitleScreen: position next to the small bottom-row buttons (accessibility, language)
                // These are typically 20x20 buttons at the bottom of the screen
                int maxX = 0;
                int bottomY = 0;
                for (GuiEventListener guiEventListener : screen.children()) {
                    if (guiEventListener instanceof AbstractWidget widget && widget.getWidth() == 20 && widget.getHeight() == 20) {
                        if (widget.getX() + widget.getWidth() > maxX) {
                            maxX = widget.getX() + widget.getWidth();
                            bottomY = widget.getY();
                        }
                    }
                }
                if (maxX > 0) {
                    x = maxX + 4; // 4px gap after the last small button
                    y = bottomY;
                } else {
                    // Fallback: bottom-left area
                    x = screen.width / 2 + 104;
                    y = screen.height - 40;
                }
            }

            if (b) {
                Screens.getWidgets(screen).add(ClientEvents.modulusButton(x, y, (button) ->
                        client.gui.setScreen(new ModulusMainScreen(screen))));
            }
        }
    }

    public static SpriteIconButton modulusButton(int x, int y, Button.OnPress onPress) {
        SpriteIconButton b = SpriteIconButton.builder(Component.translatable("narrator.modulus.button"), onPress, true)
                .size(20, 20)
                .sprite(Modulus.id("logo"), 16, 16)
                .build();
        b.setX(x);
        b.setY(y);
        return b;
    }

    public static void playerTick(Player player) {
        for (KeyMap.KeyType keyType : KeyMap.KeyType.values()) {
            if (!keyType.isMouse) {
                KeyMapping keyMapping = getMappingFromType(keyType);
                if (KEYS.isDown(keyType) != keyMapping.isDown()) {
                    KEYS.setDown(keyType, keyMapping.isDown());
                }
            } else {
                if (ClientEvents.KEYS.isDown(keyType)) {
                    if (keyType.equals(KeyMap.KeyType.MOUSE_SCROLL_UP) || keyType.equals(KeyMap.KeyType.MOUSE_SCROLL_DOWN)
                            || keyType.equals(KeyMap.KeyType.MOUSE_LEFT) && !Minecraft.getInstance().options.keyAttack.isDown()
                            || keyType.equals(KeyMap.KeyType.MOUSE_RIGHT) && !Minecraft.getInstance().options.keyUse.isDown()) {
                        ClientEvents.KEYS.setDown(keyType, false);
                    }
                }
            }
        }

        for (Ability ability : CommonUtil.getAbilities(player)) {
            if (ability.keys.notEquals(KEYS)) {
                ModNetworking.sendToServer(new ServerKeysInput(KEYS));
                break;
            }
        }
    }

    public static KeyMapping getMappingFromType(KeyMap.KeyType type) {
        return switch (type) {
            case FIRST -> KEY_MAPPINGS[0];
            case SECOND -> KEY_MAPPINGS[1];
            case THIRD -> KEY_MAPPINGS[2];
            case FOURTH -> KEY_MAPPINGS[3];
            case FIFTH -> KEY_MAPPINGS[4];
            case JUMP -> Minecraft.getInstance().options.keyJump;
            case SPRINT -> Minecraft.getInstance().options.keySprint;
            case CROUCH -> Minecraft.getInstance().options.keyShift;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public static class AbilityKeyMapping extends KeyMapping {

        public AbilityKeyMapping(int id, int keyCode) {
            super("key.categories.%s.ability.%s".formatted(Modulus.MODID, id), InputConstants.Type.KEYSYM, keyCode, ModulusClient.MODULUS_CATEGORY);
        }
    }
}
