package chappie.modulus.networking.client;

import chappie.modulus.Modulus;
import chappie.modulus.common.capability.PowerCap;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClientSyncPowerCap(CompoundTag tag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ClientSyncPowerCap> PACKET = new CustomPacketPayload.Type<>(Modulus.id("sync_power_cap"));
    public static final StreamCodec<ByteBuf, ClientSyncPowerCap> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG,
            ClientSyncPowerCap::tag,
            ClientSyncPowerCap::new
    );

    public void handle(LocalPlayer player) {
        PowerCap cap = PowerCap.getCap(player);
        if (cap != null) {
            cap.deserializeNBT(player.level().registryAccess(), this.tag);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET;
    }
}
