package chappie.modulus.common;

import chappie.modulus.Modulus;
import chappie.modulus.common.capability.anim.PlayerAnimCap;
import chappie.modulus.util.events.RegisterPlayerControllerEvent;
import chappie.modulus.common.capability.PowerCap;
import chappie.modulus.common.capability.PowerCapProvider;
import chappie.modulus.common.capability.anim.PlayerAnimCapProvider;
import chappie.modulus.common.command.SuperpowerCommand;
import chappie.modulus.networking.ModNetworking;
import chappie.modulus.networking.client.ClientSyncPowerCap;
import chappie.modulus.util.ModAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class CommonEvents {

    @SubscribeEvent
    public void livingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        AttributeInstance jumpBoost = entity.getAttribute(ModAttributes.JUMP_BOOST.get());
        if (jumpBoost != null) {
            event.setDistance(event.getDistance() - (float) jumpBoost.getValue());
        }

        AttributeInstance fallResistance = entity.getAttribute(ModAttributes.FALL_RESISTANCE.get());
        if (fallResistance != null) {
            fallResistance.setBaseValue(event.getDistance());
            if (event.getDistance() > fallResistance.getValue()) {
                event.setDamageMultiplier(0);
            }
        }
    }

    @SubscribeEvent
    public void attachCap(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity entity) {
            event.addCapability(new ResourceLocation(Modulus.MODID, "powers"), new PowerCapProvider(entity));
        }
        if (event.getObject() instanceof Player player) {
            event.addCapability(new ResourceLocation(Modulus.MODID, "player_anim"), new PlayerAnimCapProvider(player));
            event.addCapability(new ResourceLocation(Modulus.MODID, "fp_player_anim"), new PlayerAnimCapProvider(player));
        }
    }

    @SubscribeEvent
    public void clonePlayer(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().reviveCaps();
            event.getEntity().getCapability(PowerCap.CAPABILITY).ifPresent(cap -> {
                event.getOriginal().getCapability(PowerCap.CAPABILITY).ifPresent(oldCap ->
                        cap.deserializeNBT(oldCap.serializeNBT()));
            });
            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public void onStartTracking(PlayerEvent.StartTracking event) {
        event.getTarget().getCapability(PowerCap.CAPABILITY).ifPresent(a -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                ModNetworking.INSTANCE.sendTo(new ClientSyncPowerCap(event.getTarget().getId(), a.serializeNBT()), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        });
    }

    @SubscribeEvent
    public void onJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            player.getCapability(PowerCap.CAPABILITY).ifPresent(PowerCap::syncToAll);
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        SuperpowerCommand.register(event.getDispatcher());
    }

    /*@SubscribeEvent
    public void livingTick(LivingEvent.LivingTickEvent e) {
        if (e.getEntity().isAlive() && e.getEntity() instanceof Player player) {
            PlayerAnimCap cap = PlayerAnimCap.getCap(player);
            if (cap != null) {
                if (player.isCrouching()) {
                    cap.triggerAnim("controller", "dab");
                }
            }
        }
    }

    @SubscribeEvent
    public void addAnimationControllers(RegisterPlayerControllerEvent e) {
        e.registerController(b -> b.name("controller").transitionTickTime(20).animationHandler((event) -> PlayState.CONTINUE)
                .animationFile(new ResourceLocation(Modulus.MODID, "animations/player.animation.json")), c -> {
            c.triggerableAnim("hello", RawAnimation.begin().then("hello", Animation.LoopType.PLAY_ONCE));
            c.triggerableAnim("injecting2", RawAnimation.begin().then("injecting2", Animation.LoopType.PLAY_ONCE));
            c.triggerableAnim("solar_beam_charge", RawAnimation.begin().then("solar_beam_charge", Animation.LoopType.PLAY_ONCE));
            c.triggerableAnim("axe_slam_right", RawAnimation.begin().then("axe_slam_right", Animation.LoopType.PLAY_ONCE));
            c.triggerableAnim("axe_swing_start", RawAnimation.begin().then("axe_swing_start", Animation.LoopType.PLAY_ONCE));
            c.triggerableAnim("dab", RawAnimation.begin().then("dab", Animation.LoopType.PLAY_ONCE));
            c.triggerableAnim("copy_player_rotations", RawAnimation.begin().then("copy_player_rotations", Animation.LoopType.PLAY_ONCE));
            c.triggerableAnim("player_rot_in_animation", RawAnimation.begin().then("player_rot_in_animation", Animation.LoopType.PLAY_ONCE));
            c.triggerableAnim("scaling", RawAnimation.begin().then("scaling", Animation.LoopType.PLAY_ONCE));
            c.triggerableAnim("scaling2", RawAnimation.begin().then("scaling2", Animation.LoopType.PLAY_ONCE));
        });
    }*/
}
