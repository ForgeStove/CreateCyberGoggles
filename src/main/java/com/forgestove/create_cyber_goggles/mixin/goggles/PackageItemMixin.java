package com.forgestove.create_cyber_goggles.mixin.goggles;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(PackageItem.class)
public abstract class PackageItemMixin extends Item {
	public PackageItemMixin(Properties properties) {
		super(properties);
	}
	@Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
	private void appendHoverText(
		ItemStack pStack,
		Level pLevel,
		List<Component> pTooltipComponents,
		TooltipFlag pIsAdvanced,
		CallbackInfo callbackInfo
	) {
		if (!CCGConfig.get().goggles.enhancedInfo) return;
		callbackInfo.cancel();
		super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
		var compoundNbt = pStack.getOrCreateTag();
		var address = compoundNbt.getString("Address");
		if (compoundNbt.contains("Address", Tag.TAG_STRING) && !address.isBlank())
			pTooltipComponents.add(Component.literal("→ " + address).withStyle(ChatFormatting.GOLD));
		if (!compoundNbt.contains("Items", Tag.TAG_COMPOUND)) return;
		var contents = PackageItem.getContents(pStack);
		for (var i = 0; i < contents.getSlots(); i++) {
			var itemstack = contents.getStackInSlot(i);
			if (itemstack.isEmpty()) continue;
			pTooltipComponents.add(itemstack.getHoverName().copy().append(" x").append(String.valueOf(itemstack.getCount()))
											.withStyle(ChatFormatting.GRAY));
		}
	}
}
