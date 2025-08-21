package chappie.modulus.common.capability;

import chappie.modulus.Modulus;
import chappie.modulus.client.ClientEvents;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import com.google.common.collect.Maps;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class PowerCap implements AutoSyncedComponent, CommonTickingComponent, ComponentV3 {

    public static final ComponentKey<PowerCap> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(Modulus.id("powers"), PowerCap.class);

    @Nullable
    public static PowerCap getCap(Object provider) {
        return KEY.maybeGet(provider).orElse(null);
    }

    private final LivingEntity livingEntity;
    private Superpower superpower;
    private final Map<AbilityBuilder, Ability> abilities = Maps.newLinkedHashMap();

    public PowerCap(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
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
        KEY.sync(this.livingEntity);
    }

    public void syncToAll() {
        this.sync();
        for (LivingEntity livingEntity : this.livingEntity.getCommandSenderWorld().players()) {
            if (livingEntity instanceof ServerPlayer player && this.livingEntity != livingEntity) {
                KEY.sync(player);
            }
        }
    }

    @Override
    public void tick() {
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
    public void readFromNbt(CompoundTag tag) {
        CompoundTag compoundTag = tag.getCompound("Superpower");
        this.abilities.clear();
        if (!compoundTag.getString("Id").isEmpty()) {
            Superpower superpower = Superpower.REGISTRY.get(new ResourceLocation(compoundTag.getString("Id")));
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

    @Override
    public void writeToNbt(CompoundTag tag) {
        CompoundTag superpower = new CompoundTag();
        if (this.superpower != null) {
            superpower.putString("Id", Objects.requireNonNull(Superpower.REGISTRY.getKey(this.superpower)).toString());

            CompoundTag abilities = new CompoundTag();
            this.abilities.forEach((s, a) -> abilities.put(s.id, a.serializeNBT()));
            superpower.put("Abilities", abilities);
        }
        tag.put("Superpower", superpower);
    }
}
