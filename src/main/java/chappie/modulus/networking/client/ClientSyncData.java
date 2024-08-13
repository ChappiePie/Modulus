package chappie.modulus.networking.client;

import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.util.data.DataAccessor;
import chappie.modulus.util.data.DataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class ClientSyncData {

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

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeUtf(this.id);
        buf.writeUtf(this.abilityName);
        buf.writeNbt(this.tag);
    }

    public void handle(CustomPayloadEvent.Context ctx) {
        Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);
        if (entity != null) {
            entity.getCapability(PowerCap.CAPABILITY).ifPresent(cap -> {
                DataManager dataManager = cap.getAbility(this.abilityName).dataManager;
                DataAccessor<?> accessor = dataManager.getAccessorById(this.id);
                if (accessor != null) {
                    dataManager.getDataValue(accessor).deserialize(this.tag, true);
                    cap.getAbility(this.abilityName).onDataUpdated(accessor);
                }
            });
        }
        ctx.setPacketHandled(true);
    }
}
