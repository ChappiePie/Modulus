package chappie.modulus.networking.server;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.client.ClientKeyInput;
import chappie.modulus.util.KeyMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;

public class ServerKeyInput {

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

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.id);
        for (KeyMap.KeyType type : KeyMap.KeyType.values()) {
            buf.writeBoolean(this.keys.isDown(type));
        }
    }

    public static void handle(ServerKeyInput msg, CustomPayloadEvent.Context ctx) {
            Player player = ctx.getSender();
            if (player != null) {
                player.getCapability(PowerCap.CAPABILITY).ifPresent(cap -> {
                    Ability ability = cap.getAbility(msg.id);
                    ability.keys.copyFrom(msg.keys);
                    ability.conditionManager.conditions().forEach(Condition::keyEvent);
                    ModNetworking.INSTANCE.send(new ClientKeyInput(player.getId(), msg.id, msg.keys), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(player));
                });
            }
        ctx.setPacketHandled(true);
    }
}
