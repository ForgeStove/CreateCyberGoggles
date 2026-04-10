package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.box.PackageItem;
import io.github.forgestove.create_cyber_goggles.CCG;
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
		if (!CCG.config.tooltip.packageItem) return;
		ci.cancel();
		super.appendHoverText(stack, tooltipContext, tooltipComponents, tooltipFlag);
		if (stack.has(AllDataComponents.PACKAGE_ADDRESS))
			tooltipComponents.add(Component.literal("-> " + stack.get(AllDataComponents.PACKAGE_ADDRESS)).withStyle(ChatFormatting.GOLD));
	}
}
