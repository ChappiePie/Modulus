package chappie.modulus.networking.client;

import chappie.modulus.common.capability.anim.PlayerAnimCap;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ClientTriggerPlayerAnim {
    private final int entityId;
    private final String controllerName;
    private final boolean firstPerson;
    private final String animName;

    public ClientTriggerPlayerAnim(int entityId, @Nullable String controllerName, boolean firstPerson, String animName) {
        this.entityId = entityId;
        this.controllerName = controllerName == null ? "" : controllerName;
        this.firstPerson = firstPerson;
        this.animName = animName;
    }

    public ClientTriggerPlayerAnim(FriendlyByteBuf buffer) {
        this.entityId = buffer.readInt();
        this.controllerName = buffer.readUtf();
        this.firstPerson = buffer.readBoolean();
        this.animName = buffer.readUtf();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeUtf(this.controllerName);
        buffer.writeBoolean(this.firstPerson);
        buffer.writeUtf(this.animName);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);
            if (entity != null) {
                entity.getCapability(PlayerAnimCap.CAPABILITY).ifPresent(cap ->
                        cap.triggerAnim(this.controllerName, this.firstPerson, this.animName));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}