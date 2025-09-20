package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.*;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(value = DeployerTooltipBehaviour.class, remap = false)
public abstract class DeployerBlockEntityMixin extends KineticTooltipBehaviour<DeployerBlockEntity> {
	public DeployerBlockEntityMixin(DeployerBlockEntity be) {
		super(be);
	}
	@Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
	public void addToTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> returnable) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		super.addToTooltip(tooltip, isPlayerSneaking);
		var overflowItems = blockEntity.overflowItems;
		if (overflowItems.isEmpty()) {
			returnable.setReturnValue(false);
			return;
		}
		TooltipHelper.addHint(tooltip, "hint.full_deployer");
		for (var itemStack : overflowItems)
			CreateLang.builder()
				.add(Component.translatable(itemStack.getCreatorNamespace()).withStyle(ChatFormatting.GRAY))
				.add(CreateLang.text(" x" + itemStack.getCount()).style(ChatFormatting.GREEN))
				.forGoggles(tooltip);
		returnable.setReturnValue(true);
	}
	@Inject(
		method = "addToGoggleTooltip",
		at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/kinetics/deployer/DeployerBlockEntity;calculateStressApplied()F"),
		cancellable = true
	)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> returnable) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		returnable.setReturnValue(super.addToGoggleTooltip(tooltip, isPlayerSneaking));
	}
}
