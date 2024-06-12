package chappie.modulus.common.capability.anim;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class PlayerAnimCapProvider implements ICapabilitySerializable<CompoundTag> {
    private final LazyOptional<PlayerAnimCap> instance;

    public PlayerAnimCapProvider(Player player) {
        this.instance = LazyOptional.of(() -> new PlayerAnimCap(player));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == PlayerAnimCap.CAPABILITY ? this.instance.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.instance.orElseThrow(() -> new IllegalArgumentException("Player animation Capability must not be empty")).serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.instance.orElseThrow(() -> new IllegalArgumentException("Player animation Capability must not be empty!")).deserializeNBT(nbt);
    }
}
