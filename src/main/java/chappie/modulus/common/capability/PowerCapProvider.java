package chappie.modulus.common.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class PowerCapProvider implements ICapabilitySerializable<CompoundTag> {
    private final LazyOptional<PowerCap> instance;

    public PowerCapProvider(LivingEntity livingEntity) {
        this.instance = LazyOptional.of(() -> new PowerCap(livingEntity));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == PowerCap.CAPABILITY ? this.instance.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.instance.orElseThrow(() -> new IllegalArgumentException("Power Capability must not be empty")).serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.instance.orElseThrow(() -> new IllegalArgumentException("Power Capability must not be empty!")).deserializeNBT(nbt);
    }
}
