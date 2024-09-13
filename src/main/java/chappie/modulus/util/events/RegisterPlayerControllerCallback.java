package chappie.modulus.util.events;

import chappie.modulus.client.model.anim.PlayerGeoModel;
import chappie.modulus.common.capability.anim.PlayerAnimCap;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.GeckoLibException;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.core.animation.Animation;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationProcessor;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.BoneAnimation;
import software.bernie.geckolib.core.keyframe.event.data.CustomInstructionKeyframeData;
import software.bernie.geckolib.core.keyframe.event.data.ParticleKeyframeData;
import software.bernie.geckolib.core.keyframe.event.data.SoundKeyframeData;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.loading.object.BakedAnimations;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public interface RegisterPlayerControllerCallback {
    Event<RegisterPlayerControllerCallback> EVENT = EventFactory.createArrayBacked(RegisterPlayerControllerCallback.class,
            (listeners) -> (event) -> {
                for (RegisterPlayerControllerCallback listener : listeners) {
                    listener.event(event);
                }
            });

    void event(RegisterPlayerControllerCallback.RegisterPlayerControllerEvent event);

    /**
     * Registration controllers for player animations
     */
    record RegisterPlayerControllerEvent(PlayerAnimCap capability, Player player,
                                         List<RegisterPlayerControllerCallback.PlayerAnimationController> controllers) {

        public void registerControllers(Consumer<Builder> consumer, Consumer<RegisterPlayerControllerCallback.PlayerAnimationController> controllerConsumer) {
            this.registerController(consumer, controllerConsumer);
            this.registerController((b) -> {
                consumer.accept(b);
                b.name(b.name + "_first_person");
                b.animationFile(b.animationFile.withPath(b.animationFile.getPath().replace(".animation.json", "_first_person.animation.json")));
            }, controllerConsumer);
        }

        public void registerController(Consumer<Builder> consumer, Consumer<RegisterPlayerControllerCallback.PlayerAnimationController> controllerConsumer) {
            Builder builder = new Builder(this.capability);
            consumer.accept(builder);
            RegisterPlayerControllerCallback.PlayerAnimationController animationController = builder.build();
            controllerConsumer.accept(animationController);
            this.controllers.add(animationController);
        }

        public static class Builder {
            protected final PlayerAnimCap animatable;
            protected ResourceLocation animationFile;
            protected String name = "base_controller";
            protected int transitionTickTime;
            protected AnimationController.AnimationStateHandler<PlayerAnimCap> animationHandler = (p) -> PlayState.CONTINUE;

            Builder(PlayerAnimCap animatable) {
                this.animatable = animatable;
            }

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder animationFile(ResourceLocation animationFile) {
                this.animationFile = animationFile;
                return this;
            }

            public Builder transitionTickTime(int transitionTickTime) {
                this.transitionTickTime = transitionTickTime;
                return this;
            }

            public Builder animationHandler(AnimationController.AnimationStateHandler<PlayerAnimCap> animationHandler) {
                this.animationHandler = animationHandler;
                return this;
            }

            public RegisterPlayerControllerCallback.PlayerAnimationController build() {
                return new RegisterPlayerControllerCallback.PlayerAnimationController(this.animatable, this.animationFile, this.name, this.transitionTickTime, this.animationHandler);
            }
        }

    }

    class PlayerAnimationController extends AnimationController<PlayerAnimCap> {
        private final ResourceLocation animationFile;

        public PlayerAnimationController(PlayerAnimCap animatable, ResourceLocation animationFile, String name, int transitionTickTime, AnimationStateHandler<PlayerAnimCap> animationHandler) {
            super(animatable, name, transitionTickTime, animationHandler);
            this.animationFile = animationFile;
            this.receiveTriggeredAnimations();
        }

        public ResourceLocation getAnimationFile(PlayerAnimCap cap) {
            if (this.lastModel instanceof PlayerGeoModel model && this.animationFile == null) {
                return model.getAnimationResource(cap);
            }
            return this.animationFile;
        }

        @Override
        public void setAnimation(RawAnimation rawAnimation) {
            if (rawAnimation == null || rawAnimation.getAnimationStages().isEmpty()) {
                stop();
                return;
            }

            if (this.needsAnimationReload || !rawAnimation.equals(this.currentRawAnimation)) {
                if (this.lastModel != null) {
                    Queue<AnimationProcessor.QueuedAnimation> animations = this.buildAnimationQueue(this.animatable, rawAnimation);

                    if (animations != null) {
                        this.animationQueue = animations;
                        this.currentRawAnimation = rawAnimation;
                        this.shouldResetTick = true;
                        this.animationState = State.TRANSITIONING;
                        this.justStartedTransition = true;
                        this.needsAnimationReload = false;

                        return;
                    }
                }

                stop();
            }
        }

        public Queue<AnimationProcessor.QueuedAnimation> buildAnimationQueue(PlayerAnimCap animatable, RawAnimation rawAnimation) {
            LinkedList<AnimationProcessor.QueuedAnimation> animations = new LinkedList<>();
            boolean error = false;

            for (RawAnimation.Stage stage : rawAnimation.getAnimationStages()) {
                Animation animation;

                ResourceLocation location = this.getAnimationFile(animatable);
                BakedAnimations bakedAnimations = GeckoLibCache.getBakedAnimations().get(location);
                if (stage.animationName().equals("internal.wait"))
                    if (bakedAnimations != null && bakedAnimations.getAnimation(stage.animationName()) != null) {
                        Animation animation1 = bakedAnimations.getAnimation(stage.animationName());
                        assert animation1 != null;
                        animation = new Animation(stage.animationName(), stage.additionalTicks(), stage.loopType(), animation1.boneAnimations(), animation1.keyFrames());
                    } else {
                        animation = new Animation("internal.wait", stage.additionalTicks(), Animation.LoopType.PLAY_ONCE, new BoneAnimation[0], new Animation.Keyframes(new SoundKeyframeData[0], new ParticleKeyframeData[0], new CustomInstructionKeyframeData[0]));
                    }
                else {

                    if (bakedAnimations == null)
                        throw new GeckoLibException(location, "Unable to find animation.");

                    animation = bakedAnimations.getAnimation(stage.animationName());
                }

                if (animation == null) {
                    System.out.println("Unable to find animation: " + stage.animationName() + " for " + animatable.getClass().getSimpleName());

                    error = true;
                } else {
                    animations.add(new AnimationProcessor.QueuedAnimation(animation, stage.loopType()));
                }
            }

            return error ? null : animations;
        }
    }
}
