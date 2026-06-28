package chappie.modulus.common.capability;

import chappie.modulus.client.ClientEvents;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.client.ClientSyncPowerCap;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import com.google.common.collect.Maps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class PowerCap implements INBTSerializable<CompoundTag> {

    private final LivingEntity livingEntity;
    private final Map<AbilityBuilder, Ability> abilities = Maps.newLinkedHashMap();
    private Superpower superpower;

    public PowerCap(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
    }

    @Nullable
    public static PowerCap getCap(Object provider) {
        if (provider instanceof LivingEntity entity) {
            return entity.getData(ModAttachments.POWER_CAP);
        }
        return null;
    }

    public Superpower getSuperpower() {
        return superpower;
    }

    public void setSuperpower(Superpower superpower) {
        this.superpower = superpower;
        this.abilities.values().forEach(value -> value.update(this.livingEntity, false));
        this.abilities.clear();
        if (superpower != null) {
            for (AbilityBuilder builder : superpower.getBuilders()) {
                this.abilities.put(builder, builder.build(this.livingEntity));
            }
        }
        this.syncToAll();
    }

    public Collection<Ability> getAbilities() {
        return this.abilities.values();
    }

    public Ability getAbility(String key) {
        for (Ability ability : this.abilities.values()) {
            if (ability.builder.id.equals(key)) {
                return ability;
            }
        }
        return null;
    }

    public void sync() {
        this.livingEntity.setData(ModAttachments.POWER_CAP, this);
    }

    public void syncToAll() {
        this.sync();
        if (!this.livingEntity.level().isClientSide()) {
            ModNetworking.sendToTrackingEntityAndSelf(new ClientSyncPowerCap(this.serializeNBT(this.livingEntity.level().registryAccess())), this.livingEntity);
        }
    }

    public void tick() {
        if (this.livingEntity == null) return;

        if (this.livingEntity.getCommandSenderWorld().isClientSide) {
            if (this.livingEntity instanceof Player player) {
                ClientEvents.playerTick(player);
            } else {
                for (Ability ability : CommonUtil.getAbilities(this.livingEntity)) {
                    if (ability instanceof IHasTimer iHasTimer) {
                        iHasTimer.timers().forEach(IHasTimer.Timer::update);
                    }
                }
            }
        } else {
            for (Ability ability : CommonUtil.getAbilities(this.livingEntity)) {
                if (ability instanceof IHasTimer iHasTimer) {
                    iHasTimer.timers().forEach(IHasTimer.Timer::update);
                }
            }
        }
        for (Ability ability : CommonUtil.getAbilities(this.livingEntity)) {
            ability.updateTick(this.livingEntity);
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag superpowerTag = new CompoundTag();
        if (this.superpower != null) {
            superpowerTag.putString("Id", Objects.requireNonNull(Superpower.REGISTRY.getKey(this.superpower)).toString());

            CompoundTag abilities = new CompoundTag();
            this.abilities.forEach((s, a) -> abilities.put(s.id, a.serializeNBT()));
            superpowerTag.put("Abilities", abilities);
        }
        tag.put("Superpower", superpowerTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        CompoundTag compoundTag = tag.getCompound("Superpower");
        this.abilities.clear();
        if (!compoundTag.getString("Id").isEmpty()) {
            Superpower superpower = Superpower.REGISTRY.get(ResourceLocation.tryParse(compoundTag.getString("Id")));
            this.superpower = superpower;
            if (superpower != null) {
                CompoundTag abilities = compoundTag.getCompound("Abilities");
                for (String key : abilities.getAllKeys()) {
                    CompoundTag nbt = abilities.getCompound(key);
                    var builder = superpower.getBuilderByName(key);
                    if (builder != null) {
                        Ability ability = builder.build(this.livingEntity);
                        ability.deserializeNBT(nbt);
                        this.abilities.put(builder, ability);
                    }
                }
            }
        } else {
            this.superpower = null;
        }
    }
}
