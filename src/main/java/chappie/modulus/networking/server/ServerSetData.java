package chappie.modulus.networking.server;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.client.ClientSyncData;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record ServerSetData(String id, String abilityName, CompoundTag tag) implements CustomPacketPayload {
    public static final ResourceLocation PACKET_ID = Modulus.id("set_data");
    public static final Type<ServerSetData> PACKET = new Type<>(PACKET_ID);
    public static final StreamCodec<ByteBuf, ServerSetData> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerSetData::id,
            ByteBufCodecs.STRING_UTF8, ServerSetData::abilityName,
            ByteBufCodecs.COMPOUND_TAG, ServerSetData::tag,
            ServerSetData::new
    );

    public void handle(ServerPlayer player) {
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
