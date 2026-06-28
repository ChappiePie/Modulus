package chappie.modulus.client;

import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.events.RendererChangeCallback;
import chappie.modulus.util.events.SetupAnimCallback;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod(value = "modulus", dist = Dist.CLIENT)
public class ModulusClient {

    public ModulusClient(IEventBus modEventBus) {
        modEventBus.addListener(this::onRegisterKeyMappings);

        NeoForge.EVENT_BUS.addListener(this::onScreenInit);

        RendererChangeCallback.EVENT.register((event -> {
            AtomicBoolean b = new AtomicBoolean(false);
            CommonUtil.getAbilities(event.getEntity()).forEach(ability -> ability.clientProperties(c -> {
                if (c.rendererChange(event)) {
                    b.set(true);
                }
            }));
            return b.get();
        }));

        SetupAnimCallback.EVENT.register((event -> {
            CommonUtil.getAbilities(event.entity()).forEach(ability ->
                    ability.clientProperties(c -> c.setupAnim(event)));
        }));
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        Arrays.stream(ClientEvents.KEY_MAPPINGS).forEach(event::register);
    }

    private void onScreenInit(ScreenEvent.Init.Post event) {
        ClientEvents.onGuiInit(Minecraft.getInstance(), event);
    }
}
