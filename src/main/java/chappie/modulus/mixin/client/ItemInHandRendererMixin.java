package chappie.modulus.mixin.client;

import chappie.modulus.util.events.FirstPersonAdditionalHandCallback;
import chappie.playeranim.PlayerAnimationUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow protected abstract void renderPlayerArm(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HumanoidArm pSide);

    @WrapOperation(
            method = "renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/player/LocalPlayer;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    )
    private void onlyFlyIfAllowed(ItemInHandRenderer instance, AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, Operation<Void> original) {
        pMatrixStack.pushPose();
        AtomicBoolean renderArm = new AtomicBoolean(false);
        AtomicReference<Float> swingProgress = new AtomicReference<>(pSwingProgress);
        AtomicReference<Float> equippedProgress = new AtomicReference<>(pEquippedProgress);

        if (PlayerAnimationUtil.initialized()) {
            renderArm.set(PlayerAnimationUtil.rotationInFirst(instance, pPlayer, pPartialTicks, pPitch, pHand, swingProgress, pStack, equippedProgress, pMatrixStack, pBuffer, pCombinedLight));
        }

        FirstPersonAdditionalHandCallback.EVENT.invoker().event(new FirstPersonAdditionalHandCallback.FirstPersonAdditionalHandEvent(instance, renderArm, pPlayer, pPartialTicks, pPitch, pHand, pHand == InteractionHand.MAIN_HAND ? pPlayer.getMainArm() : pPlayer.getMainArm().getOpposite(), swingProgress, pStack, equippedProgress, pMatrixStack, pBuffer, pCombinedLight));

        pSwingProgress = swingProgress.get();
        pEquippedProgress = equippedProgress.get();

        if (renderArm.get() && !pPlayer.isScoping() && !pPlayer.isInvisible()) {
            this.renderPlayerArm(pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, pHand == InteractionHand.MAIN_HAND ? pPlayer.getMainArm() : pPlayer.getMainArm().getOpposite());
        } else {
            original.call(instance, pPlayer, pPartialTicks, pPitch, pHand, pSwingProgress, pStack, pEquippedProgress, pMatrixStack, pBuffer, pCombinedLight);
        }
        pMatrixStack.popPose();
    }
}

