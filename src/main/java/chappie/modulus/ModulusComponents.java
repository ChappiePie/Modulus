package chappie.modulus;

import chappie.modulus.common.capability.PowerCap;
import net.minecraft.world.entity.LivingEntity;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class ModulusComponents implements EntityComponentInitializer {
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(LivingEntity.class, PowerCap.KEY).respawnStrategy(RespawnCopyStrategy.ALWAYS_COPY).end(PowerCap::new);
    }
}