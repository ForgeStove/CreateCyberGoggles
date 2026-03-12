package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.content.logistics.filter.ListFilterItem;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
@Mixin(value = ListFilterItem.class, remap = false)
public abstract class ListFilterItemMixin implements Self<ListFilterItem> {
	@Inject(method = "makeSummary", at = @At("HEAD"), cancellable = true)
	private void makeSummary(ItemStack filter, CallbackInfoReturnable<List<Component>> cir) {
		if (!CCG.config.tooltip.listFilter) return;
		var blacklist = filter.getOrCreateTag().getBoolean("Blacklist");
		List<Component> list = new ArrayList<>();
		list.add((
			blacklist ? CreateLang.translateDirect("gui.filter.deny_list") : CreateLang.translateDirect("gui.filter.allow_list")
		).withStyle(ChatFormatting.GOLD));
		var respectNBT = filter.getOrCreateTag().getBoolean("RespectNBT");
		list.add(CreateLang.translateDirect(respectNBT ? "gui.filter.respect_data" : "gui.filter.ignore_data")
			.withStyle(ChatFormatting.GOLD));
		var added = false;
		var items = new ArrayList<ItemStack>();
		var filterItems = self().getFilterItemHandler(filter);
		for (var i = 0; i < filterItems.getSlots(); i++) {
			var stack = filterItems.getStackInSlot(i);
			items.add(stack);
			if (!stack.isEmpty()) added = true;
		}
		if (!added) {
			cir.setReturnValue(Collections.emptyList());
			return;
		}
		CCGLang.itemList(items, 9).addTo(list.size(), list);
		cir.setReturnValue(list);
	}
}
