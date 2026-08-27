package chappie.modulus.common.ability.base;

import chappie.modulus.api.IAbility;
import chappie.modulus.common.ability.base.condition.Condition;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.client.ClientSyncAbility;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.KeyMap;
import chappie.modulus.util.data.DataAccessor;
import chappie.modulus.util.data.DataManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Ability implements IAbility {

    public static final DataAccessor<Boolean> ENABLED = new DataAccessor<>("enabled", DataAccessor.DataSerializer.BOOLEAN);

    protected final LivingEntity entity;
    protected final AbilityBuilder builder;
    public final DataManager dataManager = new DataManager(this);
    public final KeyMap keys = new KeyMap();
    public final AbilityBuilder.ConditionManager conditionManager;
    public final List<AbilityClientProperties> clientProperties = new ArrayList<>();
    private final List<IHasTimer.Timer> timers = new ArrayList<>();
    private chappie.modulus.client.hud.AbilityHudProperties hudProperties;
    public int enabledTicks;
    private boolean wasEnabled;

    public Ability(LivingEntity entity, AbilityBuilder builder) {
        this.entity = entity;
        this.builder = builder;
        this.defineData();
        this.conditionManager = new AbilityBuilder.ConditionManager(this);
        if (builder.hudFactory() != null) {
            this.hudProperties = builder.hudFactory().apply(this);
        }
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            this.initializeClient(this.clientProperties::add);
        }
    }

    /**
     * Returns HUD properties for this ability, or null if none configured.
     */
    @org.jetbrains.annotations.Nullable
    public chappie.modulus.client.hud.AbilityHudProperties getHudProperties() {
        return this.hudProperties;
    }

    /**
     * Registers a timer that will be automatically ticked by the framework.
     * Call this in field initializers or defineData().
     */
    protected IHasTimer.Timer addTimer(Supplier<Integer> max, Supplier<Boolean> predicate) {
        IHasTimer.Timer timer = new IHasTimer.Timer(max, predicate);
        this.timers.add(timer);
        return timer;
    }

    /**
     * Registers a cooldown that will be automatically ticked by the framework.
     */
    protected IHasTimer.Cooldown addCooldown() {
        IHasTimer.Cooldown cooldown = new IHasTimer.Cooldown();
        this.timers.add(cooldown);
        return cooldown;
    }

    /**
     * Ticks all registered timers. Called by PowerCap.tick() automatically.
     */
    public void tickTimers() {
        for (IHasTimer.Timer timer : this.timers) {
            timer.update();
        }
    }

    /**
     * Returns all registered timers (read-only).
     */
    public List<IHasTimer.Timer> getTimers() {
        return this.timers;
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
        if (!entity.level().isClientSide()) {
            if (entity instanceof Player) {
                boolean c = this.conditionManager.test("enabling");
                boolean b = this.isEnabled();
                if (b != c) {
                    this.dataManager.set(ENABLED, c);
                }
            } else {
                if (entity.tickCount % 600 == 1) {
                    this.dataManager.set(ENABLED, !this.isEnabled());
                }
            }
        }

        boolean enabled = this.isEnabled();
        if (enabled && !this.wasEnabled) {
            this.onEnable(entity);
        } else if (!enabled && this.wasEnabled) {
            this.onDisable(entity);
        }
        this.wasEnabled = enabled;

        this.update(entity, enabled);
        this.conditionManager.conditions().forEach(Condition::update);
        if (enabled) {
            this.enabledTicks++;
        } else {
            this.enabledTicks = 0;
        }
    }

    public void update(LivingEntity entity, boolean enabled) {
    }

    /**
     * Called once when the ability transitions from disabled to enabled.
     * Override this instead of checking wasEnabled manually in update().
     */
    protected void onEnable(LivingEntity entity) {
    }

    /**
     * Called once when the ability transitions from enabled to disabled.
     */
    protected void onDisable(LivingEntity entity) {
    }

    /**
     * Called when the ability is removed (e.g. superpower changed).
     * Use this to clean up attributes, effects, or external state.
     */
    public void onRemove(LivingEntity entity) {
    }

    @Override
    public String getId() {
        return builder.id;
    }

    @Override
    public LivingEntity getEntity() {
        return entity;
    }

    @Override
    public AbilityBuilder getBuilder() {
        return builder;
    }

    @Override
    public int getEnabledTicks() {
        return enabledTicks;
    }

    @Override
    public boolean isEnabled() {
        return this.dataManager.get(ENABLED);
    }

    @Override
    public boolean isHidden() {
        return builder.hidden;
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
        this.dataManager.deserializeNBT(tag.getCompound("Data").get());
        this.conditionManager.deserializeNBT(tag.getCompound("Conditions").get());
    }

    public void sync(Entity entity) {
        if (entity instanceof ServerPlayer player) {
            ModNetworking.send(new ClientSyncAbility(player.getId(), this.builder.id, this.serializeNBT()), player);
        }
    }

    public void syncToAll(Entity entity) {
        if (!entity.level().isClientSide()) {
            ModNetworking.sendToTrackingEntityAndSelf(new ClientSyncAbility(entity.getId(), this.builder.id, this.serializeNBT()), entity);
        }
    }
}
