package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.content.logistics.box.PackageItem;
import io.github.forgestove.create_cyber_goggles.CCG;
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
		ItemStack stack,
		Level level,
		List<Component> tooltipComponents,
		TooltipFlag tooltipFlag,
		CallbackInfo callbackInfo
	) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		callbackInfo.cancel();
		appendHoverText(stack, level, tooltipComponents, tooltipFlag);
		var compoundNbt = stack.getOrCreateTag();
		var address = compoundNbt.getString("Address");
		if (compoundNbt.contains("Address", Tag.TAG_STRING) && !address.isBlank())
			tooltipComponents.add(Component.literal("→ " + address).withStyle(ChatFormatting.GOLD));
		if (!compoundNbt.contains("Items", Tag.TAG_COMPOUND)) return;
		var contents = PackageItem.getContents(stack);
		for (var i = 0; i < contents.getSlots(); i++) {
			var itemstack = contents.getStackInSlot(i);
			if (itemstack.isEmpty()) continue;
			tooltipComponents.add(itemstack.getHoverName().copy().append(" x" + itemstack.getCount()).withStyle(ChatFormatting.GRAY));
		}
	}
}
