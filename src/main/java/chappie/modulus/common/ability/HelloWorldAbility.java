package chappie.modulus.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.LivingEntity;

public class HelloWorldAbility extends Ability {
    public HelloWorldAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void update(LivingEntity entity, boolean enabled) {
        super.update(entity, enabled);
        if (enabled && entity instanceof ServerPlayer player && player.getServer() != null) {
            PlayerList playerlist = player.getServer().getPlayerList();
            playerlist.broadcastChatMessage(PlayerChatMessage.unsigned(player.getUUID(), "Hello World!"), player, ChatType.bind(ChatType.SAY_COMMAND, player));
        }
    }
}
