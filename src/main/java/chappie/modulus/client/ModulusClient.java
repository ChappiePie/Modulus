package chappie.modulus.client;

import chappie.modulus.client.model.CapeModel;
import chappie.modulus.client.model.SuitModel;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.events.RendererChangeCallback;
import chappie.modulus.util.events.SetupAnimCallback;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.world.entity.LivingEntity;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class ModulusClient implements ClientModInitializer {

    @SuppressWarnings("unchecked")
    @Override
    public void onInitializeClient() {
        ModNetworking.registerClientMessages();
        Arrays.stream(ClientEvents.KEY_MAPPINGS).forEach(KeyBindingHelper::registerKeyBinding);
        EntityModelLayerRegistry.registerModelLayer(SuitModel.SUIT, () -> SuitModel.createLayerDefinition(CubeDeformation.NONE, false));
        EntityModelLayerRegistry.registerModelLayer(SuitModel.SUIT_SLIM, () -> SuitModel.createLayerDefinition(CubeDeformation.NONE, true));
        EntityModelLayerRegistry.registerModelLayer(CapeModel.LAYER_LOCATION, CapeModel::createBodyLayer);
        ScreenEvents.AFTER_INIT.register(ClientEvents::onGuiInit);
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
            if (event.entity().isCrouching() && event.model() instanceof PlayerModel<? extends LivingEntity> model) {
                model.leftLeg.xRot = (float) Math.toRadians(90F);
                model.body.xRot = (float) Math.toRadians(-90F);
            }

            CommonUtil.getAbilities(event.entity()).forEach(ability ->
                    ability.clientProperties(c -> c.setupAnim(event)));
        }));
    }
}