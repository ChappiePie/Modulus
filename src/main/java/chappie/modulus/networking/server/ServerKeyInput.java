package chappie.modulus.networking.server;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.client.ClientKeyInput;
import chappie.modulus.util.KeyMap;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class ServerKeyInput implements FabricPacket {

    public static final PacketType<ServerKeyInput> PACKET = PacketType.create(Modulus.id("server_key_input"), ServerKeyInput::new);

    @Override
    public PacketType<?> getType() {
        return PACKET;
    }

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
                ability.keys.copyFrom(this.keys);
                ability.conditionManager.conditions().forEach(Condition::keyEvent);
                ModNetworking.sendToTrackingEntityAndSelf(new ClientKeyInput(player.getId(), this.id, this.keys), player);
            }
        }
    }
}
