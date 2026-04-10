package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.equipment.clipboard.ClipboardBlockItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.ClipboardUtil;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
	@Shadow
	protected abstract void renderOneHandedMap(
		PoseStack poseStack,
		MultiBufferSource buffer,
		int packedLight,
		float equippedProgress,
		HumanoidArm hand,
		float swingProgress,
		ItemStack stack
	);
	@Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
	private void renderClipboardLikeMap(
		AbstractClientPlayer player,
		float partialTicks,
		float pitch,
		InteractionHand hand,
		float swingProgress,
		ItemStack stack,
		float equippedProgress,
		PoseStack poseStack,
		MultiBufferSource buffer,
		int combinedLight,
		CallbackInfo ci
	) {
		if (player.isScoping()) return;
		if (!CCG.config.tooltip.clipboard) return;
		if (!(stack.getItem() instanceof ClipboardBlockItem)) return;
		var mainHand = hand == InteractionHand.MAIN_HAND;
		var arm = mainHand ? player.getMainArm() : player.getMainArm().getOpposite();
		poseStack.pushPose();
		renderOneHandedMap(poseStack, buffer, combinedLight, equippedProgress, arm, swingProgress, stack);
		poseStack.popPose();
		ci.cancel();
	}
	@Inject(method = "renderMap", at = @At("HEAD"), cancellable = true)
	private void renderClipboardPage(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ItemStack stack, CallbackInfo ci) {
		if (!CCG.config.tooltip.clipboard) return;
		if (!(stack.getItem() instanceof ClipboardBlockItem)) return;
		ci.cancel();
		ClipboardUtil.renderClipboardPage(poseStack, buffer, packedLight, stack);
	}
}
