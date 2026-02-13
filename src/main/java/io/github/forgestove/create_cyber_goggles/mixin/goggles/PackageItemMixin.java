package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.box.PackageItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
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
	public void appendHoverText(
		ItemStack stack,
		TooltipContext tooltipContext,
		List<Component> tooltipComponents,
		TooltipFlag tooltipFlag,
		CallbackInfo ci
	) {
		if (!CCG.config.goggles.enhancedInfo) return;
		ci.cancel();
		super.appendHoverText(stack, tooltipContext, tooltipComponents, tooltipFlag);
		if (stack.has(AllDataComponents.PACKAGE_ADDRESS))
			tooltipComponents.add(Component.literal("→ " + stack.get(AllDataComponents.PACKAGE_ADDRESS)).withStyle(ChatFormatting.GOLD));
		if (!stack.has(AllDataComponents.PACKAGE_CONTENTS)) return;
		var contents = PackageItem.getContents(stack);
		for (var i = 0; i < contents.getSlots(); i++) {
			var itemstack = contents.getStackInSlot(i);
			if (itemstack.isEmpty()) continue;
			CCGLang.item(itemstack).addTo(tooltipComponents);
		}
	}
}
