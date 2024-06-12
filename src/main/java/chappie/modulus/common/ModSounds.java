package chappie.modulus.common;

import chappie.modulus.Modulus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Modulus.MODID);

    public static final SoundEvent YA = register("ya");
    public static final SoundEvent NET = register("net");
    public static final SoundEvent BUTTON = register("button");

    private static SoundEvent register(String name) {
        SoundEvent soundEvent = SoundEvent.createVariableRangeEvent(new ResourceLocation(Modulus.MODID, name));
        SOUNDS.register(name, () -> soundEvent);
        return soundEvent;
    }
}
