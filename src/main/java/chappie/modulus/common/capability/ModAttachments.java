package chappie.modulus.common.capability;

import chappie.modulus.Modulus;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Modulus.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PowerCap>> POWER_CAP =
        ATTACHMENT_TYPES.register("powers", () -> AttachmentType.serializable(holder -> {
            if (holder instanceof LivingEntity entity) {
                return new PowerCap(entity);
            }
            return new PowerCap(null);
        }).copyOnDeath().build());

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
    }
}
