package chappie.modulus.common.capability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.client.ClientSyncPowerCap;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public class PowerCap {

    public static Capability<PowerCap> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    private final LivingEntity livingEntity;
    private Superpower superpower;
    private final Map<AbilityBuilder, Ability> abilities = Maps.newLinkedHashMap();

    public PowerCap(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
    }

    @Nullable
    public static PowerCap getCap(Entity entity) {
        return entity.getCapability(PowerCap.CAPABILITY).orElse(null);
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

    public Superpower getSuperpower() {
        return superpower;
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
        if (this.livingEntity instanceof ServerPlayer player) {
            ModNetworking.INSTANCE.send(new ClientSyncPowerCap(this.livingEntity.getId(), this.serializeNBT()), player.connection.getConnection());
        }
    }

    public void syncToAll() {
        this.sync();
        for (LivingEntity livingEntity : this.livingEntity.getCommandSenderWorld().players()) {
            if (livingEntity instanceof ServerPlayer player) {
                ModNetworking.INSTANCE.send(new ClientSyncPowerCap(this.livingEntity.getId(), this.serializeNBT()), player.connection.getConnection());
            }
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag superpower = new CompoundTag();
        if (this.superpower != null) {
            superpower.putString("Id", Superpower.REGISTRY.get().getKey(this.superpower).toString());

            CompoundTag abilities = new CompoundTag();
            this.abilities.forEach((s, a) -> abilities.put(s.id, a.serializeNBT()));
            superpower.put("Abilities", abilities);
        }
        tag.put("Superpower", superpower);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        CompoundTag compoundTag = tag.getCompound("Superpower");
        this.abilities.clear();
        if (!compoundTag.getString("Id").isEmpty()) {
            Superpower superpower = Superpower.REGISTRY.get().getValue(new ResourceLocation(compoundTag.getString("Id")));
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
