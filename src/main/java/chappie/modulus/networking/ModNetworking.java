package chappie.modulus.networking;

import chappie.modulus.Modulus;
import chappie.modulus.networking.client.ClientKeyInput;
import chappie.modulus.networking.client.ClientSyncAbility;
import chappie.modulus.networking.client.ClientSyncData;
import chappie.modulus.networking.client.ClientTriggerPlayerAnim;
import chappie.modulus.networking.server.ServerKeyInput;
import chappie.modulus.networking.server.ServerSetData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class ModNetworking {

    public static void registerMessages() {
        ServerPlayNetworking.registerGlobalReceiver(ServerSetData.PACKET, ServerSetData::handle);
        ServerPlayNetworking.registerGlobalReceiver(ServerKeyInput.PACKET, ServerKeyInput::handle);

        Modulus.LOGGER.debug("Registered server network");
    }

    public static void registerClientMessages() {
        ClientPlayNetworking.registerGlobalReceiver(ClientTriggerPlayerAnim.PACKET, ClientTriggerPlayerAnim::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientSyncAbility.PACKET, ClientSyncAbility::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientSyncData.PACKET, ClientSyncData::handle);
        ClientPlayNetworking.registerGlobalReceiver(ClientKeyInput.PACKET, ClientKeyInput::handle);

        Modulus.LOGGER.debug("Registered client network");
    }

    public static void sendToServer(FabricPacket packet) {
        ClientPlayNetworking.send(packet);
    }

    public static void send(FabricPacket packet, ServerPlayer player) {
        try {
            ServerPlayNetworking.send(player, packet);
        } catch (Throwable throwable) {
            throwable.fillInStackTrace();
        }
    }

    public static void sendToTrackingEntityAndSelf(FabricPacket packet, Entity entityToTrack) {
        for (ServerPlayer trackingPlayer : PlayerLookup.tracking(entityToTrack)) {
            ModNetworking.send(packet, trackingPlayer);
        }

        if (entityToTrack instanceof ServerPlayer serverPlayer) {
            ModNetworking.send(packet, serverPlayer);
        }
    }

    public static void sendToEntitiesTrackingChunk(FabricPacket packet, ServerLevel level, BlockPos blockPos) {
        for (ServerPlayer trackingPlayer : PlayerLookup.tracking(level, blockPos)) {
            ModNetworking.send(packet, trackingPlayer);
        }
    }

}