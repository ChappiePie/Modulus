package chappie.modulus.networking.server;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.client.ClientKeyInput;
import chappie.modulus.util.KeyMap;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ServerKeyInput implements CustomPacketPayload {

    public static final ResourceLocation PACKET_ID = Modulus.id("server_key_input");
    public static final Type<ServerKeyInput> PACKET = new Type<>(PACKET_ID);
    public static StreamCodec<FriendlyByteBuf, ServerKeyInput> CODEC = CustomPacketPayload.codec(ServerKeyInput::write, ServerKeyInput::new);

    private final String id;
    private final KeyMap keys;

    public ServerKeyInput(String id, KeyMap keys) {
        this.id = id;
        this.keys = keys;
    }

    public ServerKeyInput(FriendlyByteBuf buf) {
        this.id = buf.readUtf();
        KeyMap map = new KeyMap();
        for (KeyMap.KeyType type : KeyMap.KeyType.values()) {
            map.setDown(type, buf.readBoolean());
        }
        this.keys = map;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.id);
        for (KeyMap.KeyType type : KeyMap.KeyType.values()) {
            buf.writeBoolean(this.keys.isDown(type));
        }
    }

    public void handle(ServerPlayer player, PacketSender packetSender) {
        if (player != null) {
            PowerCap cap = PowerCap.getCap(player);
            if (cap != null) {
                Ability ability = cap.getAbility(this.id);
                if (ability != null) {
                    ability.keys.copyFrom(this.keys);
                    ability.conditionManager.conditions().forEach(Condition::keyEvent);
                    ModNetworking.sendToTrackingEntityAndSelf(new ClientKeyInput(player.getId(), this.id, this.keys), player);
                }
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET;
    }
}
