package com.forgestove.create_cyber_goggles.mixin.armor;
import com.forgestove.create_cyber_goggles.config.Config;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.armor.CardboardArmorHandler;
import com.simibubi.create.content.kinetics.chainConveyor.ChainConveyorRidingHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.*;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(CardboardArmorHandler.class)
public abstract class CardboardArmorHandlerMixin {
	@Inject(method = "testForStealth", at = @At("HEAD"), remap = false, cancellable = true)
	private static void testForStealth(Entity entityIn, @NotNull CallbackInfoReturnable<Boolean> returnable) {
		if (ChainConveyorRidingHandler.ridingChainConveyor == null || !(entityIn instanceof LocalPlayer player)) return;
		if (!Config.data.chainConveyor.cardBoardedYourself
				|| player.getAbilities().flying
				|| !AllItems.CARDBOARD_HELMET.isIn(player.getItemBySlot(EquipmentSlot.HEAD))
				|| !AllItems.CARDBOARD_CHESTPLATE.isIn(player.getItemBySlot(EquipmentSlot.CHEST))
				|| !AllItems.CARDBOARD_LEGGINGS.isIn(player.getItemBySlot(EquipmentSlot.LEGS))
				|| !AllItems.CARDBOARD_BOOTS.isIn(player.getItemBySlot(EquipmentSlot.FEET))) return;
		returnable.setReturnValue(true);
	}
}
