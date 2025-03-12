package chappie.modulus.networking.server;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.client.ClientSyncData;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record ServerSetData(String id, String abilityName, CompoundTag tag) implements CustomPacketPayload {
    public static final ResourceLocation PACKET_ID = Modulus.id("set_data");
    public static final Type<ServerSetData> PACKET = new Type<>(PACKET_ID);
    public static StreamCodec<FriendlyByteBuf, ServerSetData> CODEC = CustomPacketPayload.codec(ServerSetData::write, ServerSetData::new);

    public ServerSetData(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readUtf(), buf.readNbt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.id);
        buf.writeUtf(this.abilityName);
        buf.writeNbt(this.tag);
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

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET;
    }
}
