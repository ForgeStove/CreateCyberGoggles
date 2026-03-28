package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlock;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
@Mixin(value = RedstoneRequesterBlock.class, remap = false)
public abstract class RedstoneRequesterBlockMixin {
	@Inject(method = "appendRequesterTooltip", at = @At("HEAD"), cancellable = true)
	private static void appendRequesterTooltip(ItemStack stack, List<Component> tooltip, CallbackInfo ci) {
		if (!CCG.config.tooltip.redstoneRequester) return;
		if (!stack.hasTag()) return;
		var compoundnbt = stack.getTag();
		if (compoundnbt == null || !compoundnbt.contains("EncodedRequest", 10)) return;
		var contents = PackageOrderWithCrafts.read(compoundnbt.getCompound("EncodedRequest"));
		var stacks = new ArrayList<ItemStack>();
		contents.stacks().forEach(bigStack -> stacks.add(bigStack.stack.copyWithCount(bigStack.count)));
		CCGLang.itemList(stacks, 9).addTo(tooltip);
		CreateLang.translate("logistically_linked.tooltip_clear").style(ChatFormatting.GRAY).addTo(tooltip);
		ci.cancel();
	}
}
