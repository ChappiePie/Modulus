package chappie.modulus.util;

import chappie.modulus.Modulus;
import chappie.modulus.common.ability.base.AbilityType;
import chappie.modulus.common.ability.base.Superpower;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class ModRegistries {

    public static final Holder<Attribute> FALL_RESISTANCE = registerAttribute("fall_resistance", new RangedAttribute("%s.fallResistance".formatted(Modulus.MODID), 0D, 0D, Double.MAX_VALUE));
    public static final Holder<Attribute> JUMP_BOOST = registerAttribute("jump_boost", new RangedAttribute("%s.jumpBoost".formatted(Modulus.MODID), 0D, 0D, Double.MAX_VALUE).setSyncable(true));
    public static final SoundEvent OPEN_BUTTON = registerSoundEvent("open_button");
    public static final SoundEvent CLOSE_BUTTON = registerSoundEvent("close_button");

    public static void init() {
        AbilityType.init();
        Superpower.init();
    }

    private static SoundEvent registerSoundEvent(String name) {
        return Registry.register(BuiltInRegistries.SOUND_EVENT, Modulus.id(name), SoundEvent.createVariableRangeEvent(Modulus.id(name)));
    }

    private static Holder<Attribute> registerAttribute(String name, Attribute attribute) {
        return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, Modulus.id(name), attribute);
    }
}
