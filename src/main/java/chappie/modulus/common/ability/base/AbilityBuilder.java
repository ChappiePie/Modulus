package chappie.modulus.common.ability.base;

import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.util.data.DataAccessor;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.compress.utils.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AbilityBuilder {

    public final String id;
    public final AbilityType type;
    final HashMap<String, List<Function<Ability, Condition>>> funcConditions = Maps.newHashMap();
    Component displayName = null;
    boolean hidden;
    List<Consumer<Ability>> additionalData = Lists.newArrayList();

    AbilityBuilder(String id, AbilityType type) {
        this.id = id;
        this.type = type;
    }

    public static AbilityBuilder of(String name, AbilityType type) {
        return new AbilityBuilder(name, type);
    }

    public AbilityBuilder hide() {
        this.hidden = true;
        return this;
    }

    public AbilityBuilder hidden(boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    public <T> AbilityBuilder change(DataAccessor<T> accessor, T value) {
        this.additionalData.add(a -> a.dataManager.set(accessor, value));
        return this;
    }

    public AbilityBuilder additionalData(Consumer<Ability> abilityConsumer) {
        this.additionalData.add(abilityConsumer);
        return this;
    }

    public AbilityBuilder condition(Function<Ability, Condition> condition, String... methods) {
        for (String method : methods) {
            List<Function<Ability, Condition>> conditions = this.funcConditions.containsKey(method) ? this.funcConditions.get(method) : Lists.newArrayList();
            conditions.add(condition);
            this.funcConditions.put(method, conditions);
        }
        return this;
    }

    public Ability build(LivingEntity livingEntity) {
        return this.type.create(livingEntity, this);
    }

    public Component displayName() {
        return this.displayName == null ? this.type.displayName() : this.displayName;
    }

    public static class ConditionManager {

        private final HashMap<String, List<Condition>> conditions = Maps.newHashMap();
        private final Ability ability;

        public ConditionManager(Ability ability) {
            this.ability = ability;
            for (Map.Entry<String, List<Function<Ability, Condition>>> e : ability.builder.funcConditions.entrySet()) {
                this.conditions.put(e.getKey(), e.getValue().stream().map(func -> func.apply(ability)).collect(Collectors.toList()));
            }
            for (Condition condition : this.conditions()) {
                condition.init();
            }
        }

        public boolean test(String method) {
            boolean b = true;
            List<Condition> conditions = this.conditionsFor(method);
            if (conditions != null) {
                for (Condition condition : conditions) {
                    if (!condition.get()) {
                        b = false;
                    }
                }
            }
            // to disable all removed abilities
            PowerCap cap = PowerCap.getCap(this.ability.entity);
            if (cap != null && (cap.getSuperpower() == null || !cap.getSuperpower().getBuilders().contains(this.ability.builder))) {
                return false;
            }
            return b;
        }

        public List<Condition> conditionsFor(String method) {
            return this.conditions.getOrDefault(method, List.of());
        }

        public List<Condition> conditions() {
            List<Condition> list = Lists.newArrayList();
            for (List<Condition> value : this.conditions.values()) {
                list.addAll(value);
            }
            return list;
        }

        public HashMap<String, List<Condition>> methodConditions() {
            return conditions;
        }

        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            for (Map.Entry<String, List<Condition>> e : this.conditions.entrySet()) {
                ListTag conditions = new ListTag();
                for (Condition condition : e.getValue()) {
                    conditions.add(condition.serializeNBT());
                }
                tag.put(e.getKey(), conditions);
            }
            return tag;
        }

        public void deserializeNBT(CompoundTag nbt) {
            for (String key : nbt.getAllKeys()) {
                ListTag conditions = nbt.getList(key, 10);
                for (int i = 0; i < conditions.size(); i++) {
                    List<Condition> c = this.conditions.get(key);
                    if (c != null && c.get(i) != null) {
                        c.get(i).deserializeNBT(conditions.getCompound(i));
                    }
                }
            }
        }
    }
}