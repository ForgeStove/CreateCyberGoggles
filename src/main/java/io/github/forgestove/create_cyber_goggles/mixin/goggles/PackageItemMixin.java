package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.logistics.box.PackageItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;
@Mixin(PackageItem.class)
public abstract class PackageItemMixin extends Item {
	public PackageItemMixin(Properties properties) {
		super(properties);
	}
	@SuppressWarnings("deprecation")
	@Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
	public void appendHoverText(
		ItemStack stack,
		TooltipContext tooltipContext,
		TooltipDisplay displayComponent,
		Consumer<Component> textConsumer,
		TooltipFlag type,
		CallbackInfo callbackInfo
	) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		callbackInfo.cancel();
		super.appendHoverText(stack, tooltipContext, displayComponent, textConsumer, type);
		if (stack.has(AllDataComponents.PACKAGE_ADDRESS))
			textConsumer.accept(Component.literal("→ " + stack.get(AllDataComponents.PACKAGE_ADDRESS)).withStyle(ChatFormatting.GOLD));
		if (!stack.has(AllDataComponents.PACKAGE_CONTENTS)) return;
		var contents = PackageItem.getContents(stack);
		for (var i = 0; i < contents.getContainerSize(); i++) {
			var itemstack = contents.getItem(i);
			if (itemstack.isEmpty()) continue;
			textConsumer.accept(itemstack.getHoverName().copy().append(" x" + itemstack.getCount()).withStyle(ChatFormatting.GRAY));
		}
	}
}
