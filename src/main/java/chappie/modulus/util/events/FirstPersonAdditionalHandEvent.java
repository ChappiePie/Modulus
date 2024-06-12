package chappie.modulus.util.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Prerender of arm in first person, here you can modify swing progress, equip progress, and enable/disable rendering arm
 */
public class FirstPersonAdditionalHandEvent extends LivingEvent {
    private final ItemInHandRenderer instance;
    private final float pPartialTicks;
    private final float pPitch;
    private final InteractionHand pHand;
    private final HumanoidArm pArm;
    private final AtomicReference<Float> swingProgress;
    private final ItemStack pStack;
    private final AtomicReference<Float> equippedProgress;
    private final PoseStack pMatrixStack;
    private final MultiBufferSource pBuffer;
    private final int pCombinedLight;

    private final AtomicBoolean renderArm;

    public FirstPersonAdditionalHandEvent(ItemInHandRenderer instance, AtomicBoolean renderArm, AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, HumanoidArm pArm, AtomicReference<Float> swingProgress, ItemStack pStack, AtomicReference<Float> equippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight) {
        super(pPlayer);
        this.instance = instance;
        this.renderArm = renderArm;
        this.pPartialTicks = pPartialTicks;
        this.pPitch = pPitch;
        this.pHand = pHand;
        this.pArm = pArm;
        this.swingProgress = swingProgress;
        this.pStack = pStack;
        this.equippedProgress = equippedProgress;
        this.pMatrixStack = pMatrixStack;
        this.pBuffer = pBuffer;
        this.pCombinedLight = pCombinedLight;
    }

    public ItemInHandRenderer instance() {
        return instance;
    }

    @Override
    public AbstractClientPlayer getEntity() {
        return (AbstractClientPlayer) super.getEntity();
    }

    public float partialTicks() {
        return pPartialTicks;
    }

    public AtomicBoolean renderArm() {
        return renderArm;
    }

    public float pitch() {
        return pPitch;
    }

    public InteractionHand hand() {
        return pHand;
    }

    public HumanoidArm arm() {
        return pArm;
    }

    public AtomicReference<Float> swingProgress() {
        return swingProgress;
    }

    public ItemStack pStack() {
        return pStack;
    }

    public AtomicReference<Float> equippedProgress() {
        return equippedProgress;
    }

    public PoseStack poseStack() {
        return pMatrixStack;
    }

    public MultiBufferSource bufferSource() {
        return pBuffer;
    }

    public int combinedLight() {
        return pCombinedLight;
    }
}
