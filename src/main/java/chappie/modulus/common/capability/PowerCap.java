package chappie.modulus.common.capability;

import chappie.modulus.Modulus;
import chappie.modulus.client.ClientEvents;
import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.util.CommonUtil;
import chappie.modulus.util.IHasTimer;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.CommonTickingComponent;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

public class PowerCap implements AutoSyncedComponent, CommonTickingComponent, ComponentV3 {

    public static final ComponentKey<PowerCap> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(Modulus.id("powers"), PowerCap.class);
    private final LivingEntity livingEntity;
    private final Map<AbilityBuilder, Ability> abilities = Maps.newLinkedHashMap();
    private Superpower superpower;
    
    public PowerCap(LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
    }

    @Nullable
    public static PowerCap getCap(Object provider) {
        return KEY.maybeGet(provider).orElse(null);
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
        KEY.sync(this.livingEntity);
    }

    public void syncToAll() {
        this.sync();
        for (LivingEntity livingEntity : this.livingEntity.level().players()) {
            if (livingEntity instanceof ServerPlayer player && this.livingEntity != livingEntity) {
                KEY.sync(player);
            }
        }
    }

    @Override
    public void tick() {
        if (this.livingEntity.level().isClientSide()) {
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
    public void readData(ValueInput valueInput) {
        this.abilities.clear();
        CompoundTag superpowerTag = valueInput.read("Superpower", CompoundTag.CODEC).orElse(null);
        if (superpowerTag == null || !superpowerTag.contains("Id") || superpowerTag.getString("Id").orElse("").isEmpty()) {
            this.superpower = null;
            return;
        }

        ResourceLocation resourceLocation = ResourceLocation.tryParse(superpowerTag.getString("Id").orElse(""));
        Superpower superpower = resourceLocation != null ? Superpower.REGISTRY.getValue(resourceLocation) : null;
        if (superpower == null) {
            this.superpower = null;
            return;
        }

        this.superpower = superpower;
        CompoundTag abilityData = superpowerTag.getCompound("Abilities").orElseGet(CompoundTag::new);
        for (String key : abilityData.keySet()) {
            abilityData.getCompound(key).ifPresent(nbt -> {
                AbilityBuilder builder = superpower.getBuilderByName(key);
                if (builder == null) {
                    return;
                }

                Ability ability = builder.build(this.livingEntity);
                ability.deserializeNBT(nbt);
                this.abilities.put(builder, ability);
            });
        }
    }

    @Override
    public void writeData(ValueOutput valueOutput) {
        CompoundTag superpower = new CompoundTag();
        if (this.superpower != null) {
            superpower.putString("Id", Objects.requireNonNull(Superpower.REGISTRY.getKey(this.superpower)).toString());

            CompoundTag abilities = new CompoundTag();
            this.abilities.forEach((s, a) -> abilities.put(s.id, a.serializeNBT()));
            superpower.put("Abilities", abilities);
        }
        valueOutput.store("Superpower", CompoundTag.CODEC, superpower);
    }
}
