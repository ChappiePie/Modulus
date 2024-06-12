package chappie.modulus.util;

import chappie.modulus.Modulus;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, Modulus.MODID);

    public static final RegistryObject<Attribute> FALL_RESISTANCE = ATTRIBUTES.register("fall_resistance", () -> new RangedAttribute("%s.fallResistance".formatted(Modulus.MODID), 0D, 0D, Double.MAX_VALUE));
    public static final RegistryObject<Attribute> JUMP_BOOST = ATTRIBUTES.register("jump_boost", () -> new RangedAttribute("%s.jumpBoost".formatted(Modulus.MODID), 0D, 0D, Double.MAX_VALUE).setSyncable(true));
}
