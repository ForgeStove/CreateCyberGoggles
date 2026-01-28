package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.*;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(DeployerTooltipBehaviour.class)
public abstract class DeployerBlockEntityMixin extends KineticTooltipBehaviour<DeployerBlockEntity> {
	public DeployerBlockEntityMixin(DeployerBlockEntity be) {
		super(be);
	}
	@Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
	public void addToTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		var overflowItems = blockEntity.overflowItems;
		if (overflowItems.isEmpty()) {
			cir.setReturnValue(false);
			return;
		}
		super.addToTooltip(tooltip, isPlayerSneaking);
		TooltipHelper.addHint(tooltip, "hint.full_deployer");
		overflowItems.forEach(itemStack -> CCGLang.item(itemStack).forGoggles(tooltip));
		cir.setReturnValue(true);
	}
	@Inject(
		method = "addToGoggleTooltip",
		at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/kinetics/deployer/DeployerBlockEntity;calculateStressApplied()F"),
		cancellable = true
	)
	public void addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		cir.setReturnValue(true);
	}
}
