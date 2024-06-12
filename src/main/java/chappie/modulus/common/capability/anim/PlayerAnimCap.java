package chappie.modulus.common.capability.anim;

import chappie.modulus.client.model.anim.FPPlayerGeoModel;
import chappie.modulus.client.model.anim.PlayerGeoModel;
import chappie.modulus.util.events.RegisterPlayerControllerEvent;
import chappie.modulus.networking.client.ClientTriggerPlayerAnim;
import chappie.modulus.networking.ModNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.network.PacketDistributor;
import org.apache.commons.compress.utils.Lists;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;

public class PlayerAnimCap implements GeoAnimatable {

    public static Capability<PlayerAnimCap> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    public final Player player;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this, true);
    private final PlayerGeoModel modelProvider = new PlayerGeoModel();

    private final FPPlayerGeoModel fpModelProvider = new FPPlayerGeoModel();

    public PlayerAnimCap(Player player) {
        this.player = player;
    }

    @Nullable
    public static PlayerAnimCap getCap(Entity entity) {
        return entity.getCapability(PlayerAnimCap.CAPABILITY).orElse(null);
    }

    public void triggerAnim(@Nullable String controllerName, String animName) {
        this.triggerAnim(controllerName, true, animName);
        this.triggerAnim(controllerName, false, animName);
    }

    public void triggerAnim(@Nullable String controllerName, boolean firstPerson, String animName) {
        this.registerNewPlayerControllers();
        if (this.player.getLevel().isClientSide()) {
            var controller = getController(controllerName, firstPerson);
            if (controller != null) {
                controller.tryTriggerAnimation(animName);
            }
        } else {
            ModNetworking.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.player), new ClientTriggerPlayerAnim(this.player.getId(), controllerName, firstPerson, animName));
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
        List<RegisterPlayerControllerEvent.PlayerAnimationController> animationControllers = Lists.newArrayList();
        MinecraftForge.EVENT_BUS.post(new RegisterPlayerControllerEvent(this, this.player, animationControllers));
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

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.registerNewPlayerControllers();
    }
}
