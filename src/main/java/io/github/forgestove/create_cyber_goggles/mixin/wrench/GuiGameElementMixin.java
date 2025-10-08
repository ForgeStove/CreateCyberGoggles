package io.github.forgestove.create_cyber_goggles.mixin.wrench;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.element.GuiGameElement.GuiRenderBuilder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value = GuiGameElement.class, remap = false)
public abstract class GuiGameElementMixin {
	@Inject(
		method = "of(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BlockEntity;)"
			+ "Lnet/createmod/catnip/gui/element/GuiGameElement$GuiRenderBuilder;", at = @At("HEAD"), cancellable = true
	)
	private static void of(BlockState state, BlockEntity blockEntity, CallbackInfoReturnable<GuiRenderBuilder> returnable) {
		if (!CCG.CONFIG.wrench.fixRotationMenu) return;
		if (blockEntity != null) return;
		returnable.setReturnValue(GuiGameElement.of(state));
	}
}
