package chappie.modulus.common.ability.base;

import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.client.ClientSyncAbility;
import chappie.modulus.util.data.DataAccessor;
import chappie.modulus.util.data.DataManager;
import chappie.modulus.util.KeyMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Ability {

    public static final DataAccessor<Boolean> ENABLED = new DataAccessor<>("enabled", DataAccessor.DataSerializer.BOOLEAN);

    public final LivingEntity entity;
    public final AbilityBuilder builder;
    public final DataManager dataManager = new DataManager(this);
    public final KeyMap keys = new KeyMap();
    public final AbilityBuilder.ConditionManager conditionManager;
    public final List<AbilityClientProperties> clientProperties = new ArrayList<>();
    public int enabledTicks;

    public Ability(LivingEntity entity, AbilityBuilder builder) {
        this.entity = entity;
        this.builder = builder;
        this.defineData();
        this.conditionManager = new AbilityBuilder.ConditionManager(this);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            this.initializeClient(this.clientProperties::add);
        }
    }

    public void defineData() {
        this.dataManager.define(ENABLED, false);
    }

    public void onDataUpdated(DataAccessor<?> accessor) {
        if (accessor == ENABLED) {
            this.entity.refreshDimensions();
        }
    }

    public void initializeClient(Consumer<AbilityClientProperties> consumer) {
    }

    public void updateTick(LivingEntity entity) {
        if (!entity.level.isClientSide) {
            if (entity instanceof Player) {
                this.dataManager.set(ENABLED, this.conditionManager.test("enabling"));
            } else {
                if (entity.tickCount % 600 == 1) {
                    this.dataManager.set(ENABLED, !this.isEnabled());
                }
            }
        }
        this.update(entity, this.isEnabled());
        this.conditionManager.conditions().forEach(Condition::update);
        if (this.isEnabled()) {
            this.enabledTicks++;
        } else {
            this.enabledTicks = 0;
        }
    }

    public void update(LivingEntity entity, boolean enabled) {
    }

    public boolean isEnabled() {
        return this.dataManager.get(ENABLED);
    }

    public boolean isHidden() {
        return this.builder.hidden;
    }

    public void clientProperties(Consumer<AbilityClientProperties> consumer) {
        for (AbilityClientProperties clientProperty : this.clientProperties) {
            consumer.accept(clientProperty);
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("Data", this.dataManager.serializeNBT());
        tag.put("Conditions", this.conditionManager.serializeNBT());
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.dataManager.deserializeNBT(tag.getCompound("Data"));
        this.conditionManager.deserializeNBT(tag.getCompound("Conditions"));
    }

    public void sync(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            ModNetworking.INSTANCE.sendTo(new ClientSyncAbility(player.getId(), this.builder.id, this.serializeNBT()), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        }
    }

    public void syncToAll(Entity entity) {
        if (!entity.level.isClientSide) {
            ModNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), new ClientSyncAbility(entity.getId(), this.builder.id, this.serializeNBT()));
        }
    }
}
