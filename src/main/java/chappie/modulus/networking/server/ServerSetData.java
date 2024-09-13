package chappie.modulus.networking.server;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.client.ClientSyncData;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class ServerSetData implements FabricPacket {

    public static final PacketType<ServerSetData> PACKET = PacketType.create(Modulus.id("set_data"), ServerSetData::new);
  
    public String id, abilityName;
    public CompoundTag tag;

    public ServerSetData(String id, String abilityName, CompoundTag tag) {
        this.id = id;
        this.abilityName = abilityName;
        this.tag = tag;
    }

    public ServerSetData(FriendlyByteBuf buf) {
        this.id = buf.readUtf();
        this.abilityName = buf.readUtf();
        this.tag = buf.readNbt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.id);
        buf.writeUtf(this.abilityName);
        buf.writeNbt(this.tag);
    }

    @Override
    public PacketType<?> getType() {
        return PACKET;
    }

    public void handle(ServerPlayer player, PacketSender packetSender) {
        PowerCap cap = PowerCap.getCap(player);
        if (cap != null) {
            Ability ability = cap.getAbility(this.abilityName);
            var accessor = ability.dataManager.getAccessorById(this.id);
            if (accessor != null) {
                var value = ability.dataManager.getDataValue(accessor);
                value.deserialize(this.tag, true);
                ability.onDataUpdated(accessor);
                ModNetworking.sendToTrackingEntityAndSelf(new ClientSyncData(player.getId(), this.id, this.abilityName, this.tag), player);
            }
        }
    }
}
