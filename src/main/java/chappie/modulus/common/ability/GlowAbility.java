package chappie.modulus.common.ability;

import chappie.modulus.common.ability.base.Ability;
import chappie.modulus.common.ability.base.AbilityBuilder;
import chappie.modulus.common.ability.base.AbilityClientProperties;
import chappie.modulus.util.IHasTimer;
import chappie.modulus.util.data.CommonAccessors;
import chappie.modulus.util.model.IHasModelProperties;
import chappie.modulus.util.model.ModelProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;

import java.awt.*;
import java.util.function.Consumer;

/**
 * Generic glow effect ability that renders a glowing overlay on a model part.
 * Subclass to customize which part glows, texture, and render style.
 *
 * <p>By default, renders a glow on the "head" part using the configured color and texture.
 *
 * <pre>{@code
 * AbilityBuilder.of("glow_eyes", AbilityType.GLOW)
 *     .change(CommonAccessors.COLOR, Color.RED)
 * }</pre>
 */
public class GlowAbility extends Ability {

    public IHasTimer.Timer glowTimer = addTimer(() -> 4, this::isEnabled);

    public GlowAbility(LivingEntity entity, AbilityBuilder builder) {
        super(entity, builder);
    }

    @Override
    public void defineData() {
        super.defineData();
        this.dataManager.define(CommonAccessors.COLOR, Color.RED);
    }

    /**
     * Returns the texture used for the glow effect.
     * Override to provide a custom texture.
     */
    protected Identifier getGlowTexture() {
        return Identifier.withDefaultNamespace("textures/misc/enchanted_glint_entity.png");
    }

    /**
     * Returns the model part name to apply glow to (default: "head").
     */
    protected String getGlowPart() {
        return "head";
    }

    @Override
    public void initializeClient(Consumer<AbilityClientProperties> consumer) {
        super.initializeClient(consumer);
        consumer.accept(new GlowClientProperties(this));
    }

    /**
     * Default glow renderer. Renders a single glow pass on the configured model part.
     * Subclass GlowAbility and override initializeClient to provide custom rendering.
     */
    public static class GlowClientProperties implements AbilityClientProperties {

        private final GlowAbility ability;
        private HumanoidModel<?> cachedModel;

        public GlowClientProperties(GlowAbility ability) {
            this.ability = ability;
        }

        protected HumanoidModel<?> getModel() {
            if (cachedModel == null) {
                cachedModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER));
            }
            return cachedModel;
        }

        @Override
        public void render(LivingEntityRenderer<? extends LivingEntity, ? extends LivingEntityRenderState, ? extends EntityModel<?>> renderer, PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector nodeCollector, int packedLightIn, LivingEntity entity, ModelProperties modelProperties) {
            String part = ability.getGlowPart();
            if (!modelProperties.root().hasChild(part)) return;

            Color color = ability.dataManager.get(CommonAccessors.COLOR);
            float red = color.getRed() / 255F, green = color.getGreen() / 255F, blue = color.getBlue() / 255F;
            float alpha = ability.glowTimer.value(modelProperties.partialTicks());
            if (alpha <= 0) return;

            poseStack.pushPose();
            modelProperties.root().getChild(part).translateAndRotate(poseStack);
            float scale = 1.03125F;
            poseStack.scale(scale, scale, scale);

            Identifier texture = ability.getGlowTexture();
            nodeCollector.submitCustomGeometry(poseStack, RenderTypes.beaconBeam(texture, true), (pose, consumer) -> {
                PoseStack renderStack = new PoseStack();
                renderStack.last().pose().set(pose.pose());
                renderStack.last().normal().set(pose.normal());
                ModelPart modelPart = ((IHasModelProperties) this.getModel()).modulus$modelProperties().root().getChild(part);
                modelPart.render(renderStack, consumer, packedLightIn, OverlayTexture.NO_OVERLAY,
                        ARGB.colorFromFloat(alpha, red, green, blue));
            });

            poseStack.popPose();
        }
    }
}
