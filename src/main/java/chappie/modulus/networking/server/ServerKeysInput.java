package chappie.modulus.networking.server;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.client.ClientKeysInput;
import chappie.modulus.util.KeyMap;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public record ServerKeysInput(KeyMap keys) implements CustomPacketPayload {

    public static final Identifier PACKET_ID = Modulus.id("server_keys_input");
    public static final Type<ServerKeysInput> PACKET = new Type<>(PACKET_ID);
    public static final StreamCodec<FriendlyByteBuf, ServerKeysInput> CODEC = StreamCodec.ofMember(ServerKeysInput::write, ServerKeysInput::read);

    private static ServerKeysInput read(FriendlyByteBuf buf) {
        KeyMap map = new KeyMap();
        for (KeyMap.KeyType type : KeyMap.KeyType.values()) {
            map.setDown(type, buf.readBoolean());
        }
        return new ServerKeysInput(map);
    }

    private void write(FriendlyByteBuf buf) {
        for (KeyMap.KeyType type : KeyMap.KeyType.values()) {
            buf.writeBoolean(this.keys.isDown(type));
        }
    }

    public void handle(ServerPlayer player, PacketSender packetSender) {
        if (player == null) return;

        PowerCap cap = PowerCap.getCap(player);
        if (cap == null) return;

        for (Ability ability : cap.getAbilities()) {
            ability.keys.copyFrom(this.keys);
            ability.conditionManager.conditions().forEach(Condition::keyEvent);
        }

        ModNetworking.sendToTrackingEntityAndSelf(new ClientKeysInput(player.getId(), this.keys), player);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET;
    }
}
