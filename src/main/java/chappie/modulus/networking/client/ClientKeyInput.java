package chappie.modulus.networking.client;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.util.KeyMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record ClientKeyInput(int entityId, String id, KeyMap keys) implements CustomPacketPayload {

    public static final ResourceLocation PACKET_ID = Modulus.id("client_key_input");
    public static final Type<ClientKeyInput> PACKET = new Type<>(PACKET_ID);
    public static final StreamCodec<ByteBuf, ClientKeyInput> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientKeyInput::entityId,
            ByteBufCodecs.STRING_UTF8, ClientKeyInput::id,
            KeyMap.STREAM_CODEC, ClientKeyInput::keys,
            ClientKeyInput::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET;
    }

    public void handle(LocalPlayer localPlayer) {
        Entity entity = Minecraft.getInstance().level.getEntity(this.entityId);
        if (entity != null) {
            PowerCap cap = PowerCap.getCap(entity);
            if (cap != null) {
                Ability ability = cap.getAbility(this.id);
                if (ability == null) {
                    return;
                }
                ability.keys.copyFrom(this.keys);
                ability.conditionManager.conditions().forEach(Condition::keyEvent);
            }
        }
    }
}
