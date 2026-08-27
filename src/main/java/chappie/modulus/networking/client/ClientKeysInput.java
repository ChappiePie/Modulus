package chappie.modulus.networking.client;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.util.KeyMap;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public record ClientKeysInput(int entityId, KeyMap keys) implements CustomPacketPayload {

    public static final Identifier PACKET_ID = Modulus.id("client_keys_input");
    public static final Type<ClientKeysInput> PACKET = new Type<>(PACKET_ID);
    public static final StreamCodec<FriendlyByteBuf, ClientKeysInput> CODEC = StreamCodec.ofMember(ClientKeysInput::write, ClientKeysInput::read);

    private static ClientKeysInput read(FriendlyByteBuf buf) {
        int entityId = buf.readInt();
        KeyMap map = new KeyMap();
        for (KeyMap.KeyType type : KeyMap.KeyType.values()) {
            map.setDown(type, buf.readBoolean());
        }
        return new ClientKeysInput(entityId, map);
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        for (KeyMap.KeyType type : KeyMap.KeyType.values()) {
            buf.writeBoolean(this.keys.isDown(type));
        }
    }

    public void handle(LocalPlayer localPlayer, PacketSender packetSender) {
        Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);
        if (entity == null) return;

        PowerCap cap = PowerCap.getCap(entity);
        if (cap == null) return;

        for (Ability ability : cap.getAbilities()) {
            ability.keys.copyFrom(this.keys);
            ability.conditionManager.conditions().forEach(Condition::keyEvent);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET;
    }
}
