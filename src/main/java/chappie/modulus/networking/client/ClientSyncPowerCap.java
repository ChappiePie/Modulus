package chappie.modulus.networking.client;

import chappie.modulus.common.capability.PowerCap;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class ClientSyncPowerCap {

    public int entityId;
    private final CompoundTag data;

    public ClientSyncPowerCap(int entityId, CompoundTag data) {
        this.entityId = entityId;
        this.data = data;
    }

    public ClientSyncPowerCap(FriendlyByteBuf buf) {
        this.entityId = buf.readInt();
        this.data = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeNbt(this.data);
    }

    public static void handle(ClientSyncPowerCap msg, CustomPayloadEvent.Context ctx) {
        Entity entity = Minecraft.getInstance().level.getEntity(msg.entityId);
        if (entity instanceof LivingEntity e) {
            e.getCapability(PowerCap.CAPABILITY).ifPresent(data -> data.deserializeNBT(msg.data));
        }
        ctx.setPacketHandled(true);
    }
}