package io.github.forgestove.create_cyber_goggles.mixin.chainConveyor;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.zurrtum.create.*;
import com.zurrtum.create.client.content.kinetics.chainConveyor.*;
import com.zurrtum.create.content.kinetics.chainConveyor.ChainConveyorBlock;
import com.zurrtum.create.infrastructure.packet.c2s.ChainConveyorConnectionPacket;
import io.github.forgestove.create_cyber_goggles.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value = ChainConveyorInteractionHandler.class, remap = false)
public abstract class ChainConveyorInteractionHandlerMixin {
	@Shadow public static BlockPos selectedConnection, selectedLift;
	@Shadow public static float selectedChainPosition;
	@Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
	private static void isActive(CallbackInfoReturnable<Boolean> returnable) {
		if (!CCG.CONFIG.chainConveyor.alwaysAllowRiding) return;
		returnable.setReturnValue(false);
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null) return;
		var mainHandItem = player.getMainHandItem();
		if (Common.getB() instanceof ChainConveyorBlock && (
			player.isShiftKeyDown() || mainHandItem.is(Items.CHAIN) || mainHandItem.is(AllBlocks.CHAIN_CONVEYOR.asItem())
		)) return;
		returnable.setReturnValue(true);
	}
	@WrapOperation(
		method = "onUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z")
	)
	private static boolean onUse(ItemStack instance, TagKey<Item> tag, Operation<Boolean> original) {
		return !CCG.CONFIG.chainConveyor.alwaysAllowRiding && original.call(instance, tag);
	}
	@Inject(method = "onUse", at = @At("TAIL"))
	private static void injectTail(CallbackInfoReturnable<Boolean> returnable) {
		if (!CCG.CONFIG.chainConveyor.alwaysAllowRiding) return;
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null) return;
		if (!player.isShiftKeyDown()) {
			ChainConveyorRidingHandler.embark(mc, selectedLift, selectedChainPosition, selectedConnection);
			return;
		}
		if (selectedConnection == null) return;
		var mainHandItem = player.getMainHandItem();
		player.connection.send(new ChainConveyorConnectionPacket(
			selectedLift,
			selectedLift.offset(selectedConnection),
			mainHandItem.isEmpty() ? AllItems.WRENCH.getDefaultInstance() : mainHandItem,
			false
		));
	}
}
