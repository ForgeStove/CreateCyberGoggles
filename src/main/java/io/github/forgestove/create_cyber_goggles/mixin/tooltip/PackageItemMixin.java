package io.github.forgestove.create_cyber_goggles.mixin.tooltip;
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
		TooltipContext context,
		TooltipDisplay tooltipDisplay,
		Consumer<Component> tooltipAdder,
		TooltipFlag flag,
		CallbackInfo ci
	) {
		if (!CCG.config.tooltip.packageItem) return;
		ci.cancel();
		super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
		if (stack.has(AllDataComponents.PACKAGE_ADDRESS))
			tooltipAdder.accept(Component.literal("-> " + stack.get(AllDataComponents.PACKAGE_ADDRESS)).withStyle(ChatFormatting.GOLD));
	}
}
