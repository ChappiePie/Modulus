package chappie.modulus;

import chappie.modulus.common.capability.ModAttachments;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.common.command.SuperpowerCommand;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.util.ModRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Modulus.MODID)
public class Modulus {
    public static final String MODID = "modulus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(MODID, id);
    }

    public Modulus(IEventBus modEventBus) {
        ModRegistries.init(modEventBus);
        ModAttachments.register(modEventBus);
        ModNetworking.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.addListener(this::onLivingTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLogin);
        NeoForge.EVENT_BUS.addListener(this::onPlayerRespawn);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        SuperpowerCommand.register(event.getDispatcher());
    }

    private void onLivingTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            PowerCap cap = PowerCap.getCap(entity);
            if (cap != null) {
                cap.tick();
            }
        }
    }

    private void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PowerCap cap = PowerCap.getCap(player);
            if (cap != null) {
                cap.syncToAll();
            }
        }
    }

    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            PowerCap cap = PowerCap.getCap(player);
            if (cap != null) {
                cap.syncToAll();
            }
        }
    }
}
