package com.forgestove.create_cyber_goggles.mixin.goggles;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.box.PackageItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
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
		ItemStack stack,
		TooltipContext tooltipContext,
		List<Component> tooltipComponents,
		TooltipFlag tooltipFlag,
		CallbackInfo callbackInfo
	) {
		if (!CCGConfig.config.goggles.enhancedInfo) return;
		callbackInfo.cancel();
		super.appendHoverText(stack, tooltipContext, tooltipComponents, tooltipFlag);
		if (stack.has(AllDataComponents.PACKAGE_ADDRESS))
			tooltipComponents.add(Component.literal("→ " + stack.get(AllDataComponents.PACKAGE_ADDRESS)).withStyle(ChatFormatting.GOLD));
		if (!stack.has(AllDataComponents.PACKAGE_CONTENTS)) return;
		var contents = PackageItem.getContents(stack);
		for (var i = 0; i < contents.getSlots(); i++) {
			var itemstack = contents.getStackInSlot(i);
			if (itemstack.isEmpty()) continue;
			tooltipComponents.add(itemstack.getHoverName().copy().append(" x").append(String.valueOf(itemstack.getCount()))
										   .withStyle(ChatFormatting.GRAY));
		}
	}
}
