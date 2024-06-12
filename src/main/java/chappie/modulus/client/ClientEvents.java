package chappie.modulus.client;

import chappie.modulus.Modulus;
import chappie.modulus.client.gui.ModulusMenuButton;
import chappie.modulus.client.gui.ModulusMainScreen;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.server.ServerKeyInput;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.KeyMap;
import chappie.modulus.util.events.RendererChangeEvent;
import chappie.modulus.util.events.SetupAnimEvent;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

    @SubscribeEvent
    public void setupAnim(SetupAnimEvent<? extends LivingEntity, HumanoidModel<?>> event) {
        CommonUtil.getAbilities(event.getEntity()).forEach(ability -> ability.clientProperties(c -> c.setupAnim(event)));
    }

    @SubscribeEvent
    public void renderChange(RendererChangeEvent<? extends LivingEntity, HumanoidModel<?>> event) {
        CommonUtil.getAbilities(event.getEntity()).forEach(ability -> ability.clientProperties(c -> c.rendererChange(event)));
    }

    @SubscribeEvent
    public void livingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level.isClientSide && !(event.getEntity() instanceof Player)) {
            for (Ability ability : CommonUtil.getAbilities(event.getEntity())) {
                if (ability instanceof IHasTimer iHasTimer) {
                    iHasTimer.timers().forEach(IHasTimer.Timer::update);
                }
            }
        }
    }

    @SubscribeEvent
    public void onGuiInit(ScreenEvent.Init.Post event) {
        Screen screen = event.getScreen();
        if (screen instanceof TitleScreen || screen instanceof PauseScreen) {
            int x = screen.width;
            int y = screen.height / 4 + 96;
            boolean b = true;
            if (screen instanceof PauseScreen) {
                boolean b1 = false;
                for (GuiEventListener guiEventListener : event.getListenersList()) {
                    if (guiEventListener instanceof Button button && button.getMessage().equals(Component.translatable("menu.options"))) {
                        y = button.getY();
                        b1 = true;
                    }
                }
                b = b1;
            }
            for (GuiEventListener guiEventListener : event.getListenersList()) {
                if (guiEventListener instanceof Button button) {
                    if (button.getX() < x && button.getY() == y) {
                        x = button.getX();
                    }
                }
            }

            if (b) {
                event.addListener(new ModulusMenuButton(x - 24, y, (button) ->
                        screen.getMinecraft().setScreen(new ModulusMainScreen(screen))));
            }
        }
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
                if (event.phase == TickEvent.Phase.START && event.side.isClient()) {
            for (KeyMap.KeyType keyType : KeyMap.KeyType.values()) {
                if (!keyType.isMouse) {
                    KeyMapping keyMapping = getMappingFromType(keyType);
                    if (KEYS.isDown(keyType) != keyMapping.isDown()) {
                        KEYS.setDown(keyType, keyMapping.isDown());
                    }
                } else {
                    if (ClientEvents.KEYS.isDown(keyType)) {
                        if (keyType.equals(KeyMap.KeyType.MOUSE_LEFT) && !Minecraft.getInstance().options.keyAttack.isDown()
                                || keyType.equals(KeyMap.KeyType.MOUSE_RIGHT) && !Minecraft.getInstance().options.keyUse.isDown()) {
                            ClientEvents.KEYS.setDown(keyType, false);
                        }
                    }
                }
            }

            for (Ability ability : CommonUtil.getAbilities(event.player)) {
                if (ability instanceof IHasTimer iHasTimer) {
                    iHasTimer.timers().forEach(IHasTimer.Timer::update);
                }
                if (ability.keys.notEquals(KEYS)) {
                    ModNetworking.INSTANCE.sendToServer(new ServerKeyInput(ability.builder.id, KEYS));
                }
            }
        }
    }

    private static KeyMapping getMappingFromType(KeyMap.KeyType type) {
        return switch (type) {
            case FIRST -> KEY_MAPPINGS[0];
            case SECOND -> KEY_MAPPINGS[1];
            case THIRD -> KEY_MAPPINGS[2];
            case FOURTH -> KEY_MAPPINGS[3];
            case FIFTH -> KEY_MAPPINGS[4];
            case JUMP -> Minecraft.getInstance().options.keyJump;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }

    public static class AbilityKeyMapping extends KeyMapping {

        public AbilityKeyMapping(int id, int keyCode) {
            super("%s.key.ability.%s".formatted(Modulus.MODID, id), KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, keyCode, "key.categories.%s".formatted(Modulus.MODID));
        }
    }
}
