package chappie.modulus.networking.client;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.util.KeyMap;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;

public class ClientKeyInput implements FabricPacket {

    public static final PacketType<ClientKeyInput> PACKET = PacketType.create(Modulus.id("client_key_input"), ClientKeyInput::new);

    @Override
    public PacketType<?> getType() {
        return PACKET;
    }


    private final int entityId;
    private final String id;
    private final KeyMap keys;

    public ClientKeyInput(int entityId, String id, KeyMap keys) {
        this.entityId = entityId;
        this.id = id;
        this.keys = keys;
    }

    public ClientKeyInput(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.id = buf.readUtf();
        KeyMap map = new KeyMap();
        for (KeyMap.KeyType type : KeyMap.KeyType.values()) {
            map.setDown(type, buf.readBoolean());
        }
        this.keys = map;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeUtf(this.id);
        for (KeyMap.KeyType type : KeyMap.KeyType.values()) {
            buf.writeBoolean(this.keys.isDown(type));
        }
    }

    public void handle(LocalPlayer localPlayer, PacketSender packetSender) {
        Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);
        if (entity != null) {
            PowerCap cap = PowerCap.getCap(entity);
            if (cap != null) {
                Ability ability = cap.getAbility(this.id);
                ability.keys.copyFrom(this.keys);
                ability.conditionManager.conditions().forEach(Condition::keyEvent);
            }
        }
        
    }
}
