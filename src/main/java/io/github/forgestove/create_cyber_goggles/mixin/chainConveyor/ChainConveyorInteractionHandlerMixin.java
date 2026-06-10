package io.github.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.zurrtum.create.*;
import com.zurrtum.create.client.content.kinetics.chainConveyor.*;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlock;
import com.zurrtum.create.infrastructure.packet.c2s.ChainConveyorConnectionPacket;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(ChainConveyorInteractionHandler.class)
public abstract class ChainConveyorInteractionHandlerMixin {
	@Shadow public static BlockPos selectedConnection, selectedLift;
	@Shadow public static float selectedChainPosition;
	@Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
	private static void isActive(CallbackInfoReturnable<Boolean> returnable) {
		if (!CCG.config.chainConveyor.alwaysAllowRidingChain) return;
		if (mc.player == null) {
			returnable.setReturnValue(false);
			return;
		}
		var mainHandItem = mc.player.getMainHandItem();
		if (getBlock(ChainConveyorBlock.class) == null || !mc.player.isShiftKeyDown()
			&& !mainHandItem.getItem().equals(Items.IRON_CHAIN)
			&& !mainHandItem.is(AllBlocks.CHAIN_CONVEYOR.asItem())) returnable.setReturnValue(true);
	}
	@WrapOperation(
		method = "onUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z")
	)
	private static boolean onUse(ItemStack instance, TagKey<Item> tag, Operation<Boolean> original) {
		return !CCG.config.chainConveyor.alwaysAllowRidingChain && original.call(instance, tag);
	}
	@Inject(method = "onUse", at = @At("TAIL"))
	private static void injectTail(CallbackInfoReturnable<Boolean> returnable) {
		if (!CCG.config.chainConveyor.alwaysAllowRidingChain) return;
		if (mc.player == null) return;
		if (!mc.player.isShiftKeyDown()) {
			ChainConveyorRidingHandler.embark(mc, selectedLift, selectedChainPosition, selectedConnection);
			return;
		}
		if (selectedConnection == null) return;
		var mainHandItem = mc.player.getMainHandItem();
		sendToServer(new ChainConveyorConnectionPacket(
			selectedLift,
			selectedLift.offset(selectedConnection),
			mainHandItem.isEmpty() ? AllItems.WRENCH.getDefaultInstance() : mainHandItem,
			false
		));
	}
}
