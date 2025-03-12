package chappie.modulus.networking;

import chappie.modulus.Modulus;
import chappie.modulus.networking.client.ClientKeyInput;
import chappie.modulus.networking.client.ClientSyncAbility;
import chappie.modulus.networking.client.ClientSyncData;
import chappie.modulus.networking.server.ServerKeyInput;
import chappie.modulus.networking.server.ServerSetData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class ModNetworking {

    public static void registerMessages() {
        PayloadTypeRegistry.playC2S().register(ServerSetData.PACKET, ServerSetData.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ServerSetData.PACKET, (packet, context) -> packet.handle(context.player(), context.responseSender()));

        PayloadTypeRegistry.playC2S().register(ServerKeyInput.PACKET, ServerKeyInput.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ServerKeyInput.PACKET, (packet, context) -> packet.handle(context.player(), context.responseSender()));

        Modulus.LOGGER.debug("Registered server network");
    }

    public static void registerClientMessages() {
        PayloadTypeRegistry.playS2C().register(ClientSyncAbility.PACKET, ClientSyncAbility.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(ClientSyncAbility.PACKET, (packet, context) -> packet.handle(context.player(), context.responseSender()));
        PayloadTypeRegistry.playS2C().register(ClientSyncData.PACKET, ClientSyncData.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(ClientSyncData.PACKET, (packet, context) -> packet.handle(context.player(), context.responseSender()));
        PayloadTypeRegistry.playS2C().register(ClientKeyInput.PACKET, ClientKeyInput.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(ClientKeyInput.PACKET, (packet, context) -> packet.handle(context.player(), context.responseSender()));

        Modulus.LOGGER.debug("Registered client network");
    }

    public static void sendToServer(CustomPacketPayload packet) {
        ClientPlayNetworking.send(packet);
    }

    public static void send(CustomPacketPayload packet, ServerPlayer player) {
        try {
            ServerPlayNetworking.send(player, packet);
        } catch (Throwable throwable) {
            throwable.fillInStackTrace();
        }
    }

    public static void sendToTrackingEntityAndSelf(CustomPacketPayload packet, Entity entityToTrack) {
        for (ServerPlayer trackingPlayer : PlayerLookup.tracking(entityToTrack)) {
            ModNetworking.send(packet, trackingPlayer);
        }

        if (entityToTrack instanceof ServerPlayer serverPlayer) {
            ModNetworking.send(packet, serverPlayer);
        }
    }

    public static void sendToEntitiesTrackingChunk(CustomPacketPayload packet, ServerLevel level, BlockPos blockPos) {
        for (ServerPlayer trackingPlayer : PlayerLookup.tracking(level, blockPos)) {
            ModNetworking.send(packet, trackingPlayer);
        }
    }

}