package chappie.modulus.client;

import chappie.modulus.Modulus;
import chappie.modulus.client.hud.AbilityBarRenderer;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.events.RendererChangeCallback;
import chappie.modulus.util.events.SetupAnimCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModulusClient implements ClientModInitializer {

    public static final KeyMapping.Category MODULUS_CATEGORY = KeyMapping.Category.register(Modulus.id("modulus"));

    @SuppressWarnings("unchecked")
    @Override
    public void onInitializeClient() {
        ModNetworking.registerClientMessages();
        Arrays.stream(ClientEvents.KEY_MAPPINGS).forEach(KeyMappingHelper::registerKeyMapping);
        ScreenEvents.AFTER_INIT.register(ClientEvents::onGuiInit);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                ClientEvents.playerTick(client.player);
            }
        });
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(Modulus.MODID, "ability_bar"), (graphics, deltaTracker) -> {
            var player = Minecraft.getInstance().player;
            if (player != null) {
                int width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
                int height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
                float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
                AbilityBarRenderer.render(graphics, partialTick, width, height);
                CommonUtil.getAbilities(player).forEach(ability ->
                        ability.clientProperties(c -> c.renderOverlay(graphics, partialTick, width, height)));
            }
        });
        RendererChangeCallback.EVENT.register((event -> {
            AtomicBoolean b = new AtomicBoolean(false);
            CommonUtil.getAbilities(event.entity()).forEach(ability -> ability.clientProperties(c -> {
                if (c.rendererChange(event)) {
                    b.set(true);
                }
            }));
            return b.get();
        }));
        SetupAnimCallback.EVENT.register((event ->
                CommonUtil.getAbilities(event.entity()).forEach(ability ->
                        ability.clientProperties(c -> c.setupAnim(event)))));
    }
}