package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.logistics.box.PackageItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
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
		TooltipDisplay tooltipDisplay,
		Consumer<Component> tooltipComponents,
		TooltipFlag tooltipFlag,
		CallbackInfo ci
	) {
		if (!CCG.config.goggles.enhancedInfo) return;
		ci.cancel();
		super.appendHoverText(stack, tooltipContext, tooltipDisplay, tooltipComponents, tooltipFlag);
		if (stack.has(AllDataComponents.PACKAGE_ADDRESS))
			tooltipComponents.accept(Component.literal("→ " + stack.get(AllDataComponents.PACKAGE_ADDRESS)).withStyle(ChatFormatting.GOLD));
		if (!stack.has(AllDataComponents.PACKAGE_CONTENTS)) return;
		var contents = PackageItem.getContents(stack);
		for (var i = 0; i < contents.getContainerSize(); i++) {
			var itemstack = contents.getItem(i);
			if (itemstack.isEmpty()) continue;
			tooltipComponents.accept(CCGLang.item(itemstack).component());
		}
	}
}
