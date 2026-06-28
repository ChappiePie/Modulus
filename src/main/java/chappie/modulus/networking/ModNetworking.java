package chappie.modulus.networking;

import chappie.modulus.Modulus;
import chappie.modulus.networking.client.ClientKeyInput;
import chappie.modulus.networking.client.ClientSyncAbility;
import chappie.modulus.networking.client.ClientSyncData;
import chappie.modulus.networking.client.ClientSyncPowerCap;
import chappie.modulus.networking.server.ServerKeyInput;
import chappie.modulus.networking.server.ServerSetData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModNetworking::onRegisterPayloadHandler);
    }

    private static void onRegisterPayloadHandler(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // Server-bound packets
        registrar.playToServer(ServerSetData.PACKET, ServerSetData.CODEC, (packet, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer serverPlayer) {
                    packet.handle(serverPlayer);
                }
            });
        });

        registrar.playToServer(ServerKeyInput.PACKET, ServerKeyInput.CODEC, (packet, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer serverPlayer) {
                    packet.handle(serverPlayer);
                }
            });
        });

        // Client-bound packets
        registrar.playToClient(ClientSyncAbility.PACKET, ClientSyncAbility.CODEC, (packet, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof net.minecraft.client.player.LocalPlayer localPlayer) {
                    packet.handle(localPlayer);
                }
            });
        });

        registrar.playToClient(ClientSyncData.PACKET, ClientSyncData.CODEC, (packet, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof net.minecraft.client.player.LocalPlayer localPlayer) {
                    packet.handle(localPlayer);
                }
            });
        });

        registrar.playToClient(ClientKeyInput.PACKET, ClientKeyInput.CODEC, (packet, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof net.minecraft.client.player.LocalPlayer localPlayer) {
                    packet.handle(localPlayer);
                }
            });
        });
        
        registrar.playToClient(ClientSyncPowerCap.PACKET, ClientSyncPowerCap.CODEC, (packet, context) -> {
            context.enqueueWork(() -> {
                if (context.player() instanceof net.minecraft.client.player.LocalPlayer localPlayer) {
                    packet.handle(localPlayer);
                }
            });
        });

        Modulus.LOGGER.debug("Registered network packets");
    }

    public static void sendToServer(CustomPacketPayload packet) {
        PacketDistributor.sendToServer(packet);
    }

    public static void send(CustomPacketPayload packet, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToTrackingEntityAndSelf(CustomPacketPayload packet, Entity entityToTrack) {
        if (entityToTrack instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, packet);
        }
        if (entityToTrack.level() instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingEntity(entityToTrack, packet);
        }
    }

    public static void sendToEntitiesTrackingChunk(CustomPacketPayload packet, ServerLevel level, BlockPos blockPos) {
        PacketDistributor.sendToPlayersTrackingChunk(level, level.getChunkAt(blockPos).getPos(), packet);
    }
}