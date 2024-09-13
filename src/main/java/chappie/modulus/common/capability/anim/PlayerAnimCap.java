package chappie.modulus.common.capability.anim;

import chappie.modulus.Modulus;
import chappie.modulus.client.model.anim.FPPlayerGeoModel;
import chappie.modulus.client.model.anim.PlayerGeoModel;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.client.ClientTriggerPlayerAnim;
import chappie.modulus.util.events.RegisterPlayerControllerCallback;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class PlayerAnimCap implements GeoAnimatable, AutoSyncedComponent, ComponentV3 {

    public static final ComponentKey<PlayerAnimCap> KEY = ComponentRegistryV3.INSTANCE.getOrCreate(Modulus.id("player_anim"), PlayerAnimCap.class);

    public static PlayerAnimCap getCap(Object provider) {
        return KEY.get(provider);
    }

    public final Player player;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this, true);
    private final PlayerGeoModel modelProvider = new PlayerGeoModel();

    private final FPPlayerGeoModel fpModelProvider = new FPPlayerGeoModel();

    public PlayerAnimCap(Player player) {
        this.player = player;
    }

    public void triggerAnim(@Nullable String controllerName, String animName) {
        this.triggerAnim(controllerName, true, animName);
        this.triggerAnim(controllerName, false, animName);
    }

    public void triggerAnim(@Nullable String controllerName, boolean firstPerson, String animName) {
        this.registerNewPlayerControllers();
        if (this.player.getCommandSenderWorld().isClientSide()) {
            var controller = getController(controllerName, firstPerson);
            if (controller != null) {
                controller.tryTriggerAnimation(animName);
            }
        } else {
            ModNetworking.sendToTrackingEntityAndSelf(new ClientTriggerPlayerAnim(this.player.getId(), controllerName, firstPerson, animName), this.player);
        }
    }

    public PlayerGeoModel getAnimatedModel() {
        return modelProvider;
    }

    public FPPlayerGeoModel getFPAnimatedModel() {
        return this.fpModelProvider;
    }

    public AnimationController<?> getController(String controllerName, boolean firstPerson) {
        int hash = this.player.getUUID().hashCode();
        if (firstPerson) {
            hash += "first_person".hashCode();
        }
        return getAnimatableInstanceCache().getManagerForId(hash).getAnimationControllers().get(controllerName + (firstPerson ? "_first_person" : ""));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    public void registerNewPlayerControllers() {
        List<RegisterPlayerControllerCallback.PlayerAnimationController> animationControllers = Lists.newArrayList();
        RegisterPlayerControllerCallback.EVENT.invoker().event(new RegisterPlayerControllerCallback.RegisterPlayerControllerEvent(this, this.player, animationControllers));
        for (AnimationController<PlayerAnimCap> controller : animationControllers) {
            if (controller.getName().contains("_first_person")) {
                AnimatableManager<PlayerAnimCap> manager = this.cache.getManagerForId(this.player.getUUID().hashCode() + "first_person".hashCode());
                if (!manager.getAnimationControllers().containsKey(controller.getName())) {
                    manager.addController(controller);
                }
            } else {
                AnimatableManager<PlayerAnimCap> manager = this.cache.getManagerForId(this.player.getUUID().hashCode());
                if (!manager.getAnimationControllers().containsKey(controller.getName())) {
                    manager.addController(controller);
                }
            }
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getTick(Object object) {
        return ((Entity) object).tickCount + Minecraft.getInstance().getFrameTime();
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        this.registerNewPlayerControllers();
    }

    @Override
    public void writeToNbt(CompoundTag tag) {

    }
}
