package chappie.modulus.networking.client;

import chappie.modulus.Modulus;
import chappie.modulus.common.capability.PowerCap;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ClientSyncAbility implements CustomPacketPayload {

    public static final ResourceLocation PACKET_ID = Modulus.id("sync_ability");
    public static final Type<ClientSyncAbility> PACKET = new Type<>(PACKET_ID);
    public static StreamCodec<FriendlyByteBuf, ClientSyncAbility> CODEC = CustomPacketPayload.codec(ClientSyncAbility::write, ClientSyncAbility::new);
    public int entityId;
    public String id;
    public CompoundTag nbt;
    public ClientSyncAbility(int entityId, String id, CompoundTag nbt) {
        this.entityId = entityId;
        this.id = id;
        this.nbt = nbt;
    }

    public ClientSyncAbility(FriendlyByteBuf buffer) {
        this.entityId = buffer.readInt();
        this.id = buffer.readUtf();
        this.nbt = buffer.readNbt();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET;
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeUtf(this.id);
        buffer.writeNbt(this.nbt);
    }

    public void handle(LocalPlayer localPlayer, PacketSender packetSender) {
        Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);
        if (entity instanceof LivingEntity) {
            PowerCap cap = PowerCap.getCap(entity);
            if (cap != null) {
                cap.getAbility(this.id).deserializeNBT(this.nbt);
            }
        }

    }
}