package chappie.modulus.util;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.base.AbilityType;
import chappie.modulus.common.ability.base.Superpower;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;

public class ModRegistries {

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, Modulus.MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Modulus.MODID);

    public static final DeferredHolder<Attribute, Attribute> FALL_RESISTANCE = ATTRIBUTES.register("fall_resistance",
            () -> new RangedAttribute("attribute.modulus.fall_resistance", 0D, 0D, Double.MAX_VALUE));
    public static final DeferredHolder<Attribute, Attribute> JUMP_BOOST = ATTRIBUTES.register("jump_boost",
            () -> new RangedAttribute("attribute.modulus.jump_boost", 0D, 0D, Double.MAX_VALUE).setSyncable(true));

    public static final DeferredHolder<SoundEvent, SoundEvent> OPEN_BUTTON = SOUND_EVENTS.register("open_button",
            () -> SoundEvent.createVariableRangeEvent(Modulus.id("open_button")));
    public static final DeferredHolder<SoundEvent, SoundEvent> CLOSE_BUTTON = SOUND_EVENTS.register("close_button",
            () -> SoundEvent.createVariableRangeEvent(Modulus.id("close_button")));

    public static void init(IEventBus modEventBus) {
        ATTRIBUTES.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);

        modEventBus.addListener(ModRegistries::onNewRegistry);

        AbilityType.init();
        Superpower.init();
    }

    private static void onNewRegistry(NewRegistryEvent event) {
        event.register(AbilityType.REGISTRY);
        event.register(Superpower.REGISTRY);
    }
}
