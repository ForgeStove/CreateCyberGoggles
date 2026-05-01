package io.github.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.*;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.kinetics.chainConveyor.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorInteractionHandler.*;
import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(ChainConveyorInteractionHandler.class)
public abstract class ChainConveyorInteractionHandlerMixin {
	@Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
	private static void isActive(CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.chainConveyor.alwaysAllowRidingChain) return;
		if (mc.player == null) {
			cir.setReturnValue(false);
			return;
		}
		var mainHandItem = mc.player.getMainHandItem();
		if (getBlock(ChainConveyorBlock.class) == null || !mc.player.isShiftKeyDown()
			&& !mainHandItem.getItem().equals(Items.CHAIN)
			&& !AllBlocks.CHAIN_CONVEYOR.isIn(mainHandItem)) cir.setReturnValue(true);
	}
	@WrapOperation(
		method = "onUse",
		at = @At(value = "INVOKE", target = "Lcom/simibubi/create/AllTags$AllItemTags;matches(Lnet/minecraft/world/item/ItemStack;)Z")
	)
	private static boolean onUse(AllItemTags instance, ItemStack stack, Operation<Boolean> original) {
		return !CCG.config.chainConveyor.alwaysAllowRidingChain && original.call(instance, stack);
	}
	@Inject(method = "onUse", at = @At("TAIL"))
	private static void injectTail(CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.chainConveyor.alwaysAllowRidingChain) return;
		if (mc.player == null) return;
		if (!mc.player.isShiftKeyDown()) {
			ChainConveyorRidingHandler.embark(selectedLift, selectedChainPosition, selectedConnection);
			return;
		}
		if (selectedConnection == null) return;
		var mainHandItem = mc.player.getMainHandItem();
		sendToServer(new ChainConveyorConnectionPacket(
			selectedLift,
			selectedLift.offset(selectedConnection),
			mainHandItem.isEmpty() ? AllItems.WRENCH.asStack() : mainHandItem,
			false
		));
	}
}
