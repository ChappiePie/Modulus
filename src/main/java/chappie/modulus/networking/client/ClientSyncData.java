package chappie.modulus.networking.client;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.util.data.DataAccessor;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class ClientSyncData implements CustomPacketPayload {
    public static final ResourceLocation PACKET_ID = Modulus.id("sync_data");
    public static final Type<ClientSyncData> PACKET = new Type<>(PACKET_ID);
    public static StreamCodec<FriendlyByteBuf, ClientSyncData> CODEC = CustomPacketPayload.codec(ClientSyncData::write, ClientSyncData::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET;
    }

    public int entityId;
    public String id, abilityName;
    public CompoundTag tag;

    public ClientSyncData(int entityId, String id, String abilityName, CompoundTag tag) {
        this.entityId = entityId;
        this.id = id;
        this.abilityName = abilityName;
        this.tag = tag;
    }

    public ClientSyncData(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.id = buf.readUtf();
        this.abilityName = buf.readUtf();
        this.tag = buf.readNbt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeUtf(this.id);
        buf.writeUtf(this.abilityName);
        buf.writeNbt(this.tag);
    }

    public void handle(LocalPlayer localPlayer, PacketSender packetSender) {
        Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);
        if (entity != null) {
            PowerCap cap = PowerCap.getCap(entity);
            if (cap != null) {
                Ability ability = cap.getAbility(this.abilityName);
                if (ability != null) {
                    DataAccessor<?> accessor = ability.dataManager.getAccessorById(this.id);
                    if (accessor != null) {
                        ability.dataManager.getDataValue(accessor).deserialize(this.tag, true);
                        cap.getAbility(this.abilityName).onDataUpdated(accessor);
                    }
                }
            }
        }
    }
}
