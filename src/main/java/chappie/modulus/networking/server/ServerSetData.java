package chappie.modulus.networking.server;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.client.ClientSyncData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.PacketDistributor;

public class ServerSetData {

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

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.id);
        buf.writeUtf(this.abilityName);
        buf.writeNbt(this.tag);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                player.getCapability(PowerCap.CAPABILITY).ifPresent(cap -> {
                    Ability ability = cap.getAbility(this.abilityName);
                    var accessor = ability.dataManager.getAccessorById(this.id);
                    if (accessor != null) {
                        var value = ability.dataManager.getDataValue(accessor);
                        value.deserialize(this.tag, true);
                        ability.onDataUpdated(accessor);
                        ModNetworking.INSTANCE.send(new ClientSyncData(player.getId(), this.id, this.abilityName, this.tag), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(player));
                    }
                });
            }
        ctx.setPacketHandled(true);
    }
}
