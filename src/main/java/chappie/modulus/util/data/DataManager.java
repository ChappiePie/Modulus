package chappie.modulus.util.data;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.client.ClientSyncData;
import chappie.modulus.networking.server.ServerSetData;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;

import java.util.Map;

public class DataManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    public final Map<DataAccessor<?>, DataValue<?>> dataMap = Maps.newHashMap();
    private final Ability ability;

    public DataManager(Ability ability) {
        this.ability = ability;
    }

    public <T> DataManager define(DataAccessor<T> accessor, T initialValue) {
        return this.define(accessor, initialValue, true);
    }

    public <T> DataManager define(DataAccessor<T> accessor, T initialValue, boolean saveAfterRejoin) {
        if (!this.dataMap.containsKey(accessor)) {
            this.dataMap.put(accessor, new DataValue<>(accessor, initialValue, saveAfterRejoin));
        } else {
            LOGGER.error("Cannot define the data with id: %s".formatted(accessor.key()));
        }
        return this;
    }

    public <T> DataManager set(DataAccessor<T> accessor, T value) {
        DataValue<T> dataValue = this.getDataValue(accessor);
        Entity entity = this.ability.entity;
        if (dataValue.get() != value) {
            dataValue.set(value);
            this.ability.onDataUpdated(accessor);
            if (!entity.level().isClientSide) {
                ModNetworking.sendToTrackingEntityAndSelf(new ClientSyncData(entity.getId(), accessor.key(), this.ability.builder.id, dataValue.serialize(new CompoundTag(), true)), entity);
            }
        }
        return this;
    }

    public <T> DataManager setFromClient(DataAccessor<T> accessor, T value) {
        DataValue<T> dataValue = this.getDataValue(accessor);
        if (this.ability.entity.level().isClientSide && dataValue.get() != value) {
            ModNetworking.sendToServer(new ServerSetData(accessor.key(), this.ability.builder.id, dataValue.serialize(new CompoundTag(), value, true)));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> DataValue<T> getDataValue(DataAccessor<T> accessor) {
        return (DataValue<T>) this.dataMap.get(accessor);
    }

    public <T> T get(DataAccessor<T> accessor) {
        return this.getDataValue(accessor).get();
    }

    public DataAccessor<?> getAccessorById(String id) {
        for (DataAccessor<?> dataAccessor : this.dataMap.keySet()) {
            if (dataAccessor.key().equals(id)) {
                return dataAccessor;
            }
        }
        return null;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        this.dataMap.values().forEach((e) -> e.serialize(tag, false));
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.dataMap.values().forEach((e) -> e.deserialize(tag, false));
    }

    public static class DataValue<T> {
        private final DataAccessor<T> accessor;
        private final boolean save;
        private final T initialValue;
        private T value;

        DataValue(DataAccessor<T> accessor, T value, boolean save) {
            this.accessor = accessor;
            this.initialValue = value;
            this.value = value;
            this.save = save;
        }

        public void set(T value) {
            this.value = value;
        }

        public void reset() {
            this.value = this.initialValue;
        }

        public T get() {
            return value;
        }

        public T initial() {
            return this.initialValue;
        }

        public boolean saves() {
            return save;
        }

        public CompoundTag serialize(CompoundTag tag, boolean update) {
            return this.serialize(tag, this.value, update);
        }

        public CompoundTag serialize(CompoundTag tag, T value, boolean sync) {
            if (!this.save && !sync) {
                return tag;
            }
            tag.put(this.accessor.key(), this.accessor.serializer().toTag().apply(value));
            return tag;
        }

        public void deserialize(CompoundTag tag, boolean sync) {
            if (!this.save && !sync) {
                return;
            }
            this.set(this.accessor.serializer().fromTag().apply(tag, this.accessor.key()));
        }
    }
}
