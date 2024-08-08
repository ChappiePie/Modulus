package chappie.modulus.networking.client;

import chappie.modulus.common.capability.PowerCap;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class ClientSyncAbility {

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

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeUtf(this.id);
        buffer.writeNbt(this.nbt);
    }

    public static void handle(ClientSyncAbility msg, CustomPayloadEvent.Context ctx) {
        Entity entity = Minecraft.getInstance().level.getEntity(msg.entityId);
        if (entity instanceof LivingEntity) {
            entity.getCapability(PowerCap.CAPABILITY).ifPresent(cap ->
                    cap.getAbility(msg.id).deserializeNBT(msg.nbt));
        }
        ctx.setPacketHandled(true);
    }
}