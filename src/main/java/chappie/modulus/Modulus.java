package chappie.modulus;

import chappie.modulus.client.model.anim.PlayerGeoModel;
import chappie.modulus.client.ClientEvents;
import chappie.modulus.client.gui.ModulusMainScreen;
import chappie.modulus.client.model.CapeModel;
import chappie.modulus.client.model.SuitModel;
import chappie.modulus.common.CommonEvents;
import chappie.modulus.common.ModSounds;
import chappie.modulus.common.ability.base.AbilityType;
import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.util.ModAttributes;
import com.mojang.logging.LogUtils;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

import java.util.Arrays;

@Mod(Modulus.MODID)
public class Modulus {

    public static final String MODID = "modulus";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Modulus() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
        AbilityType.ABILITY_TYPES.register(bus);
        Superpower.SUPERPOWERS.register(bus);
        ModSounds.SOUNDS.register(bus);
        ModAttributes.ATTRIBUTES.register(bus);

        GeckoLib.initialize();
        MinecraftForge.EVENT_BUS.register(new CommonEvents());
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                MinecraftForge.EVENT_BUS.register(new ClientEvents()));
    }

    static {
        PlayerGeoModel.registerMolangQueries();
    }

    @SubscribeEvent
    public void commonSetup(final FMLCommonSetupEvent event) {
        ModNetworking.registerMessages();
    }

    @SubscribeEvent
    public void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.register(PowerCap.class);
    }

    @SubscribeEvent
    public void entityAttributeModification(final EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            if (!event.has(type, ModAttributes.JUMP_BOOST.get())) {
                event.add(type, ModAttributes.JUMP_BOOST.get());
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void registerKeyBinds(RegisterKeyMappingsEvent e) {
        Arrays.stream(ClientEvents.KEY_MAPPINGS).forEach(e::register);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void registerLayerDefinitions(final EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SuitModel.SUIT, () -> SuitModel.createLayerDefinition(CubeDeformation.NONE, false));
        event.registerLayerDefinition(SuitModel.SUIT_SLIM, () -> SuitModel.createLayerDefinition(CubeDeformation.NONE, true));
        event.registerLayerDefinition(CapeModel.LAYER_LOCATION, CapeModel::createBodyLayer);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onLoadComplete(FMLLoadCompleteEvent event) {
        ModContainer createContainer = ModList.get()
                .getModContainerById(Modulus.MODID)
                .orElseThrow(() -> new IllegalStateException("Modulus mod container missing on LoadComplete"));
        createContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, previousScreen) -> new ModulusMainScreen(previousScreen)));
    }
}
