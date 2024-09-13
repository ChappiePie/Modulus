package chappie.modulus.networking.client;

import chappie.modulus.Modulus;
import chappie.modulus.common.capability.anim.PlayerAnimCap;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class ClientTriggerPlayerAnim implements FabricPacket {

    public static final PacketType<ClientTriggerPlayerAnim> PACKET = PacketType.create(Modulus.id("trigger_player_anim"), ClientTriggerPlayerAnim::new);

    @Override
    public PacketType<?> getType() {
        return PACKET;
    }

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

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeUtf(this.controllerName);
        buffer.writeBoolean(this.firstPerson);
        buffer.writeUtf(this.animName);
    }

    public void handle(LocalPlayer localPlayer, PacketSender packetSender) {
        Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);
        if (entity != null) {
            PlayerAnimCap.getCap(entity).triggerAnim(this.controllerName, this.firstPerson, this.animName);
        }

    }
}