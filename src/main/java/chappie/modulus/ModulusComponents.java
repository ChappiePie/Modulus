package chappie.modulus;

import chappie.modulus.common.capability.PowerCap;
import chappie.playeranim.PlayerAnimationUtil;
import chappie.playeranim.capability.PlayerAnimCap;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class ModulusComponents implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(LivingEntity.class, PowerCap.KEY).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(PowerCap::new);
        if (PlayerAnimationUtil.initialized()) {
            registry.beginRegistration(Player.class, PlayerAnimCap.KEY).respawnStrategy(RespawnCopyStrategy.NEVER_COPY).end(PlayerAnimCap::new);
        }
    }
}